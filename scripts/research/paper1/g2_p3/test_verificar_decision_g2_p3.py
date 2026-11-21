import tempfile
import unittest
from pathlib import Path

from verificar_decision_g2_p3 import (
    MODULE, check_witnesses, classify_tlc, generate,
)


class G2P3ContractTests(unittest.TestCase):
    def test_safety_accepts_only_clean_run(self):
        valid = 'Model checking completed. No error has been found.\nFinished in 01s'
        self.assertTrue(classify_tlc(valid, 0, 'safety'))
        self.assertFalse(classify_tlc(valid, 12, 'safety'))

    def test_coverage_requires_named_invariant(self):
        valid = 'Error: Invariant ReachDebited is violated.\nFinished in 01s'
        self.assertTrue(classify_tlc(valid, 12, 'coverage'))
        self.assertFalse(classify_tlc(valid.replace('ReachDebited', 'Other'), 12, 'coverage'))
        self.assertFalse(classify_tlc(valid, 0, 'coverage'))

    def test_module_contains_real_history_observer(self):
        self.assertIn('seenDebited[t] \\/ srcState\'[t] = "Debited"', MODULE)
        self.assertIn('EXTENDS MC', MODULE)
        self.assertIn('NoReturnAfterDebit', MODULE)
        self.assertIn('ReachDebited', MODULE)

    def test_witnesses_reject_invented_decision(self):
        with tempfile.TemporaryDirectory() as folder:
            dst = Path(folder) / 'testigos.tsv'
            dst.write_text('scenario\tresult\tfailure_point\tpartial_debit\tdecision_witness\t'
                           'saw_debit\tsaw_credit\trollback_exact\tstages\n'
                           'normal\tTRACE_COMMIT_VALIDADA\tNONE\ttrue\ttrue\ttrue\ttrue\tfalse\t'
                           'BEGIN,SOURCE_DEBITED,DESTINATION_CREDITED,COMMIT_COMPLETED\n',
                           encoding='utf-8')
            with self.assertRaises(ValueError):
                check_witnesses(dst)

    def test_generate_does_not_change_source(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            model = root / 'src'
            model.mkdir()
            (model / 'CrossShardCommitV2.tla').write_text('MODEL', encoding='utf-8')
            (model / 'MC.tla').write_text('MC', encoding='utf-8')
            (model / 'correct_shared.cfg').write_text(
                'CONSTANTS\nQuorum = 2\nMutation = "Correct"\n'
                'SPECIFICATION Spec\nINVARIANT TypeOK\n', encoding='utf-8')
            generate(root / 'out', model)
            self.assertEqual((model / 'CrossShardCommitV2.tla').read_text(), 'MODEL')
            self.assertIn('INVARIANT ReachDebited',
                          (root / 'out/G2P3Coverage.cfg').read_text())


if __name__ == '__main__':
    unittest.main(verbosity=2)
