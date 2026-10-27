"""Reject release evidence whose version, artifact or signing status is inconsistent."""
import argparse
import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def validate(root=ROOT, apk=None, require_signed=False):
    """Compare committed evidence with source pins and optionally the actual built APK."""
    gradle = (root / 'app/build.gradle.kts').read_text()
    version = re.search(r'versionName\s*=\s*"([^"]+)"', gradle)[1]
    code = int(re.search(r'versionCode\s*=\s*(\d+)', gradle)[1])
    evidence = root / 'verification'
    summary = json.loads((evidence / 'summary.json').read_text())
    assert (summary['version'], summary['version_code']) == (version, code), 'Stale verification summary version'
    badging = (evidence / 'apk-badging.txt').read_text()
    assert f"versionName='{version}'" in badging and f"versionCode='{code}'" in badging, 'Stale APK badging version'
    assert "name='com.electrokit.app'" in badging, 'Unexpected application identity'
    signed = summary['signing']['status'] == 'verified_original_release'
    expected = (root / 'signing/original-release-certificate.sha256').read_text().strip()
    if signed:
        signature = (evidence / 'apk-signature.txt').read_text()
        assert f'Signer #1 certificate SHA-256 digest: {expected}' in signature, 'Original release certificate mismatch'
        assert summary['signing']['certificate_sha256'] == expected, 'Summary certificate mismatch'
    if require_signed:
        assert signed, 'Original-key signed APK is required; current evidence is unsigned'
    if apk:
        assert hashlib.sha256(Path(apk).read_bytes()).hexdigest() == summary['apk_sha256'], 'APK differs from verification evidence'
        assert Path(apk).stat().st_size == summary['apk_bytes'], 'APK size differs from verification evidence'
    print(f"PASS: current {version}/{code} evidence; signing={summary['signing']['status']}.")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk', type=Path)
    parser.add_argument('--require-signed', action='store_true')
    args = parser.parse_args()
    validate(apk=args.apk, require_signed=args.require_signed)
