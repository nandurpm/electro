"""Regression checks for documentation coverage and source-derived palette evidence."""
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch
import check_comments
import check_contrast

class QualityGateTests(unittest.TestCase):
    """Prove that the gates depend on source content, including negative fixtures."""
    def test_undocumented_function_is_rejected(self):
        # Declaration-shaped text inside strings cannot document or hide a real function.
        with tempfile.TemporaryDirectory(dir=check_comments.ROOT) as directory:
            path=Path(directory)/'Fixture.kt'
            path.write_text('val ignored = "fun fake() {}"\nfun missing() {}\n')
            count,failures=check_comments.audit(path)
            self.assertGreaterEqual(count,2)
            self.assertTrue(any('function_declaration missing' in f for f in failures))
    def test_annotated_function_and_constructor_property_docs_pass(self):
        # Annotations belong to the declaration; constructor fields can use class @property docs.
        with tempfile.TemporaryDirectory(dir=check_comments.ROOT) as directory:
            path=Path(directory)/'Fixture.kt'
            path.write_text('/** Owner. @property value Stored scalar. */\nclass Owner(val value: Int) {\n/** Mutation. @return Unit; no errors. */\n@Deprecated("fixture")\nfun mutate() {}\n}\n')
            count,failures=check_comments.audit(path)
            self.assertEqual(3,count)
            self.assertEqual([],failures)
    def test_source_token_changes_change_contrast_report(self):
        # A hard-coded duplicate colour list would incorrectly leave the report unchanged.
        source=(check_contrast.ROOT/'app/src/main/java/com/electrokit/app/ui/theme/Theme.kt').read_text()
        original=check_contrast.report()
        with patch.object(Path,'read_text',return_value=source.replace('0xFF'+check_contrast.palette(source, 'Dark')['primary'],'0xFFFFFFFF')):
            self.assertNotEqual(original,check_contrast.report())
    def test_committed_contrast_evidence_is_current(self):
        # Stale numbers are rejected even if old and new palettes both pass 4.5:1.
        self.assertEqual((check_contrast.ROOT/'scripts/contrast-baseline.txt').read_text(),check_contrast.report())
if __name__=='__main__':unittest.main()
