"""Exercise release provenance with real local tags and diverging commits."""
from pathlib import Path
import subprocess
import tempfile
import unittest
from verify_release_source import verify


class ReleaseSourceTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.git('init', '-q')
        self.git('config', 'user.name', 'Fixture')
        self.git('config', 'user.email', 'fixture@example.invalid')
        (self.root / 'app').mkdir()
        (self.root / 'app/build.gradle.kts').write_text('versionName = "4.5.3"\n')
        self.git('add', '.')
        self.git('commit', '-qm', 'fixture')
        self.git('tag', 'v4.5.3')
        self.git('update-ref', 'refs/remotes/origin/main', 'HEAD')

    def git(self, *args):
        return subprocess.check_output(['git', '-C', str(self.root), *args], text=True).strip()

    def test_exact_tagged_main_commit_passes(self):
        self.assertEqual(self.git('rev-parse', 'HEAD'), verify(self.root, 'v4.5.3'))

    def test_mismatched_app_version_is_rejected(self):
        with self.assertRaises(ValueError):
            verify(self.root, 'v4.5.2')

    def test_tagged_source_cannot_follow_a_newer_head(self):
        (self.root / 'other').write_text('new commit')
        self.git('add', '.')
        self.git('commit', '-qm', 'advance')
        self.git('update-ref', 'refs/remotes/origin/main', 'HEAD')
        with self.assertRaises(ValueError):
            verify(self.root, 'v4.5.3')

    def test_off_main_tag_is_rejected(self):
        old = self.git('rev-parse', 'HEAD')
        (self.root / 'other').write_text('untrusted branch commit')
        self.git('add', '.')
        self.git('commit', '-qm', 'branch only')
        self.git('tag', '-f', 'v4.5.3')
        self.git('update-ref', 'refs/remotes/origin/main', old)
        with self.assertRaises(ValueError):
            verify(self.root, 'v4.5.3')
