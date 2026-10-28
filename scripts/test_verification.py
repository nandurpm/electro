"""Regression fixtures reject the stale-version evidence reported during release review."""
import json
from pathlib import Path
import tempfile
import unittest

import check_verification


class VerificationTests(unittest.TestCase):
    """Exercise known mismatches independently of Gradle or the actual private signing key."""
    def setUp(self):
        self.folder = tempfile.TemporaryDirectory()
        self.addCleanup(self.folder.cleanup)
        self.root = Path(self.folder.name)
        (self.root / 'app').mkdir()
        (self.root / 'verification').mkdir()
        (self.root / 'signing').mkdir()
        (self.root / 'app/build.gradle.kts').write_text('versionName = "3.0"\nversionCode = 14\n')
        (self.root / 'signing/release-certificate.sha256').write_text('a' * 64)
        self.summary = {'version': '3.0', 'version_code': 14, 'signing': {'status': 'unsigned'}}
        self.write_summary()
        (self.root / 'verification/apk-badging.txt').write_text("package: name='com.electrokit.app' versionCode='14' versionName='3.0'\n")

    def write_summary(self):
        """Write a fixture summary so each assertion tests a distinct evidence mismatch."""
        (self.root / 'verification/summary.json').write_text(json.dumps(self.summary))

    def test_current_unsigned_evidence_is_explicitly_allowed_for_preparation(self):
        check_verification.validate(self.root)

    def test_old_summary_is_rejected(self):
        self.summary.update(version='2.0.3', version_code=12)
        self.write_summary()
        with self.assertRaisesRegex(AssertionError, 'Stale verification summary'):
            check_verification.validate(self.root)

    def test_old_apk_report_is_rejected(self):
        (self.root / 'verification/apk-badging.txt').write_text("package: name='com.electrokit.app' versionCode='12' versionName='2.0.3'\n")
        with self.assertRaisesRegex(AssertionError, 'Stale APK badging'):
            check_verification.validate(self.root)

    def test_unsigned_evidence_cannot_publish(self):
        with self.assertRaisesRegex(AssertionError, 'signed APK is required'):
            check_verification.validate(self.root, require_signed=True)

    def test_active_signed_certificate_is_accepted(self):
        self.summary['signing'] = {'status': 'verified_release', 'certificate_sha256': 'a' * 64}
        self.write_summary()
        (self.root / 'verification/apk-signature.txt').write_text('Signer #1 certificate SHA-256 digest: ' + 'a' * 64)
        check_verification.validate(self.root, require_signed=True)

    def test_different_signed_certificate_is_rejected(self):
        self.summary['signing'] = {'status': 'verified_release', 'certificate_sha256': 'b' * 64}
        self.write_summary()
        (self.root / 'verification/apk-signature.txt').write_text('Signer #1 certificate SHA-256 digest: ' + 'b' * 64)
        with self.assertRaisesRegex(AssertionError, 'certificate mismatch'):
            check_verification.validate(self.root, require_signed=True)


if __name__ == '__main__':
    unittest.main()
