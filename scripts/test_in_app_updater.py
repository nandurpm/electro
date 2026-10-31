"""Regression checks for the safe browser-based Android updater."""
import unittest
from pathlib import Path
from tree_sitter import Language, Parser
import tree_sitter_kotlin

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "app/src/main/java/com/electrokit/app/MainActivity.kt"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
POLICY = ROOT / "app/src/main/java/com/electrokit/app/update/UpdatePolicy.kt"


class InAppUpdaterRegressionTests(unittest.TestCase):
    """Catch unsafe sideload-install regressions and malformed update-dialog merges."""

    def test_main_activity_has_no_kotlin_parse_errors(self):
        src = MAIN.read_bytes()
        tree = Parser(Language(tree_sitter_kotlin.language())).parse(src)
        self.assertFalse(tree.root_node.has_error, "MainActivity.kt has an invalid merge or Kotlin grammar")

    def test_single_update_dialog_preserves_both_update_policies(self):
        source = MAIN.read_text()
        dialog = source[source.index('val release = pending'):source.index('if (checkStatus != null)')]
        self.assertEqual(dialog.count('onDismissRequest = '), 1)
        self.assertEqual(dialog.count('confirmButton = '), 1)
        self.assertEqual(dialog.count('dismissButton = '), 1)
        self.assertIn('UpdatePriority.REQUIRED_FEATURE', dialog)
        self.assertIn('openReleasePage(release)', dialog)
        self.assertIn('Open Release', dialog)
        self.assertIn('if (!required) TextButton', dialog)
        self.assertIn('onDismissRequest = { if (!required) pending = null }', dialog)
        self.assertIn('dismissOnBackPress = !required, dismissOnClickOutside = !required', dialog)
        self.assertNotIn('downloadInsideApp', dialog)
        self.assertNotIn('Install Update', dialog)
        self.assertNotIn('LinearProgressIndicator', dialog)

    def test_sideload_installer_capability_is_absent(self):
        source = MAIN.read_text()
        manifest = MANIFEST.read_text()
        forbidden = (
            'REQUEST_INSTALL_PACKAGES',
            'FileProvider',
            'application/vnd.android.package-archive',
            'canRequestPackageInstalls',
            'ACTION_MANAGE_UNKNOWN_APP_SOURCES',
            'ApkUpdateDownloader',
        )
        for token in forbidden:
            self.assertNotIn(token, source)
            self.assertNotIn(token, manifest)
        self.assertIn('android.permission.INTERNET', manifest)

    def test_feature_releases_are_required_and_patches_optional(self):
        source = POLICY.read_text()
        self.assertIn('UpdatePriority.REQUIRED_FEATURE', source)
        self.assertIn('UpdatePriority.OPTIONAL_PATCH', source)


if __name__ == "__main__":
    unittest.main()
