"""Reject a release whose source, version tag or trusted-main ancestry disagree."""
import argparse
from pathlib import Path
import re
import subprocess


def verify(root: Path, tag: str) -> str:
    """Return the immutable release commit, rejecting all mismatched or untrusted sources."""
    if not re.fullmatch(r'v(?:0|[1-9][0-9]{0,4})\.(?:0|[1-9][0-9]{0,4})\.(?:0|[1-9][0-9]{0,4})', tag):
        raise ValueError('An explicit canonical version tag is required')
    text = (root / 'app/build.gradle.kts').read_text()
    version = re.search(r'versionName\s*=\s*"([^"]+)"', text)[1]
    if tag != 'v' + version:
        raise ValueError('Requested tag does not match app version')

    def git(*args):
        return subprocess.check_output(['git', '-C', str(root), *args], text=True).strip()

    head = git('rev-parse', 'HEAD')
    if head != git('rev-parse', f'refs/tags/{tag}^{{commit}}'):
        raise ValueError('Source commit does not match requested tag')
    if subprocess.run(['git', '-C', str(root), 'merge-base', '--is-ancestor', head,
                       'refs/remotes/origin/main'], check=False).returncode != 0:
        raise ValueError('Release source is not on trusted main history')
    return head


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('tag')
    args = parser.parse_args()
    print('Verified release source:', verify(Path.cwd(), args.tag))
