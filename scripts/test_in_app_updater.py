"""Regression checks for browserless Android updater and conflict-free main Activity."""
import re
import unittest
from pathlib import Path
from tree_sitter import Language, Parser
import tree_sitter_kotlin

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "app/src/main/java/com/electrokit/app/MainActivity.kt"
DOWNLOADER = ROOT / "app/src/main/java/com/electrokit/app/update/ApkUpdateDownloader.kt"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
POLICY = ROOT / "app/src/main/java/com/electrokit/app/update/UpdatePolicy.kt"


class InAppUpdaterRegressionTests(unittest.TestCase):
    """Catch bad merges, unsafe browser handoffs and missing native update controls."""

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
        self.assertIn('if (!required)', dialog)
        self.assertIn('downloadInsideApp(release)', dialog)
        self.assertIn('Cancel download', dialog)
        self.assertIn('LinearProgressIndicator', dialog)
        self.assertNotIn('Uri.parse(release.apkUrl)', dialog)
        self.assertNotIn('CATEGORY_BROWSABLE', dialog)

    def test_file_provider_and_apk_verification_are_present(self):
        source = MAIN.read_text()
        dl = DOWNLOADER.read_text()
        manifest = MANIFEST.read_text()
        self.assertIn('FileProvider.getUriForFile(', source)
        self.assertIn('Intent.FLAG_GRANT_READ_URI_PERMISSION', source)
        self.assertIn('android.permission.REQUEST_INSTALL_PACKAGES', manifest)
        self.assertIn('android:exported="false"', manifest)
        self.assertIn('validateInstalledIdentity(context, destination, release.version)', dl)
        self.assertIn('candidateSignatures', dl)
        self.assertIn('withContext(Dispatchers.Main) { onProgress(100) }', dl)

    def test_feature_releases_are_required_and_patches_optional(self):
        source = POLICY.read_text()
        self.assertIn('UpdatePriority.REQUIRED_FEATURE', source)
        self.assertIn('UpdatePriority.OPTIONAL_PATCH', source)


if __name__ == "__main__":
    unittest.main()
