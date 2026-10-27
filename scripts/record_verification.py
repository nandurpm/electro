"""Replace stale verification with actual current APK, tests, lint and validator outputs.

Unsigned builds are explicitly labeled and cannot pass --require-signed. The original
certificate identity is stored separately from generated evidence.
"""
import argparse
from collections import Counter
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET

from check_verification import validate

ROOT = Path(__file__).resolve().parents[1]


def run(command, expected=(0,)):
    """Capture public tool output without echoing inherited Java option values."""
    env = os.environ.copy()
    if Path(command[0]).name == 'apksigner':
        env.pop('JAVA_TOOL_OPTIONS', None)
    result = subprocess.run(command, cwd=ROOT, env=env, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if result.returncode not in expected:
        raise RuntimeError(f'{command[0]} failed with exit {result.returncode}:\n{result.stdout}')
    return result


def record(apk, build_log, variants, require_signed=False):
    """Stage all evidence before replacing the existing snapshot; stop on failed validators."""
    apk = apk.resolve()
    sdk = Path(os.environ['ANDROID_HOME']) / 'build-tools/35.0.0'
    source = (ROOT / 'app/build.gradle.kts').read_text()
    version = re.search(r'versionName\s*=\s*"([^"]+)"', source)[1]
    code = int(re.search(r'versionCode\s*=\s*(\d+)', source)[1])
    log = build_log.read_text()
    assert 'BUILD SUCCESSFUL' in log and 'BUILD FAILED' not in log, 'Build log is not a successful run'
    expected = (ROOT / 'signing/original-release-certificate.sha256').read_text().strip()
    with tempfile.TemporaryDirectory(prefix='electro-verification-', dir=ROOT.parent) as temp:
        stage = Path(temp)
        badging = run([str(sdk / 'aapt'), 'dump', 'badging', str(apk)]).stdout
        assert f"versionName='{version}'" in badging and f"versionCode='{code}'" in badging, 'APK/source version mismatch'
        (stage / 'apk-badging.txt').write_text(badging)
        (stage / 'apk-permissions.txt').write_text(run([str(sdk / 'aapt'), 'dump', 'permissions', str(apk)]).stdout)
        signature = run([str(sdk / 'apksigner'), 'verify', '--verbose', '--print-certs', str(apk)], expected=(0, 1))
        digest = re.search(r'^Signer #1 certificate SHA-256 digest: (\w+)$', signature.stdout, re.M)
        signed = signature.returncode == 0
        if signed:
            assert digest and digest[1] == expected, 'Refusing an APK signed with a different identity'
        if require_signed:
            assert signed, 'No original-key signature; do not publish this APK'
        (stage / 'apk-signature.txt').write_text(('UNSIGNED RELEASE — not production ready\n' if not signed else '') + signature.stdout)
        tests, lint = {}, {}
        for variant in variants:
            reports = ROOT / f'app/build/test-results/test{variant.title()}UnitTest'
            xmls = sorted(reports.glob('TEST-*.xml'))
            assert xmls, f'No executed {variant} tests'
            roots = [ET.parse(path).getroot() for path in xmls]
            counts = {key: sum(int(root.attrib.get(key, 0)) for root in roots) for key in ('tests', 'failures', 'errors', 'skipped')}
            assert counts['tests'] > 0 and not counts['failures'] and not counts['errors'], counts
            tests[variant] = counts
            target = stage / f'tests-{variant}'
            target.mkdir()
            for path in xmls:
                shutil.copy2(path, target / path.name)
            for suffix in ('xml', 'html'):
                path = ROOT / f'app/build/reports/lint-results-{variant}.{suffix}'
                assert path.is_file(), f'Missing current lint report: {path}'
                shutil.copy2(path, stage / path.name)
            text = ROOT / f'app/build/intermediates/lint_intermediate_text_report/{variant}/lintReport{variant.title()}/lint-results-{variant}.txt'
            if text.exists():
                shutil.copy2(text, stage / f'lint-results-{variant}.txt')
            issues = ET.parse(stage / f'lint-results-{variant}.xml').getroot().findall('issue')
            errors = sum(item.attrib['severity'] in ('Error', 'Fatal') for item in issues)
            assert not errors, 'Lint errors remain'
            warnings = [item for item in issues if item.attrib['severity'] == 'Warning']
            lint[variant] = {'errors': errors, 'warnings': len(warnings), 'warning_ids': dict(Counter(item.attrib['id'] for item in warnings))}
        checks = {
            'comments.txt': ['scripts/check_comments.py'],
            'assets.txt': ['scripts/validate_assets.py'],
            'engineering.txt': ['scripts/validate_engineering.py'],
            'library.txt': ['scripts/validate_library.py'],
            'contrast.txt': ['scripts/check_contrast.py'],
            'python-tests.txt': ['-m', 'unittest', 'discover', '-s', 'scripts', '-p', 'test_*.py'],
        }
        for name, command in checks.items():
            (stage / name).write_text(run([sys.executable, *command]).stdout)
        pages = json.loads((ROOT / 'app/src/main/assets/pages.json').read_text())
        modules = json.loads((ROOT / 'app/src/main/assets/modules.json').read_text())
        urls = sorted({url for page in pages.values() for url in page['sources']} | {part['source'] for parts in modules.values() for part in parts})
        (stage / 'reference-links.json').write_text(json.dumps({'status': 'not_rechecked', 'note': 'No fresh external reachability or manufacturer-content verification is claimed.', 'urls': urls}, indent=2) + '\n')
        public_log = '\n'.join(line for line in log.splitlines() if not line.startswith('Picked up JAVA_TOOL_OPTIONS:')) + '\n'
        (stage / 'build-output.txt').write_text(public_log)
        summary = {
            'version': version, 'version_code': code, 'application_id': 'com.electrokit.app',
            'generated_at_utc': datetime.now(timezone.utc).isoformat(),
            'source_commit': run(['git', 'rev-parse', 'HEAD']).stdout.strip(),
            'tests': tests, 'lint': lint, 'artifact': str(apk.relative_to(ROOT)),
            'apk_sha256': hashlib.sha256(apk.read_bytes()).hexdigest(), 'apk_bytes': apk.stat().st_size,
            'signing': {'status': 'verified_original_release' if signed else 'unsigned', 'certificate_sha256': digest[1] if digest else None, 'expected_certificate_sha256': expected},
            'release_publishable': signed, 'physical_device_tested': False, 'emulator_tested': False,
            'internet_permission': 'android.permission.INTERNET' in badging,
            'external_references_rechecked': False,
        }
        assert not summary['internet_permission']
        (stage / 'summary.json').write_text(json.dumps(summary, indent=2) + '\n')
        evidence = ROOT / 'verification'
        backup = Path(temp) / 'previous'
        evidence.rename(backup)
        try:
            shutil.copytree(stage, evidence, ignore=shutil.ignore_patterns('previous'))
            validate(apk=apk, require_signed=require_signed)
        except BaseException:
            shutil.rmtree(evidence, ignore_errors=True)
            backup.rename(evidence)
            raise
    print(f'Generated current {version}/{code} evidence; signed={signed}; tests={tests}')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk', type=Path, required=True)
    parser.add_argument('--build-log', type=Path, required=True)
    parser.add_argument('--variants', nargs='+', choices=('debug', 'release'), default=['debug', 'release'])
    parser.add_argument('--require-signed', action='store_true')
    args = parser.parse_args()
    record(args.apk, args.build_log, args.variants, args.require_signed)
