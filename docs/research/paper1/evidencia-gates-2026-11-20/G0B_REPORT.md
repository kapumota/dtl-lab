# TLC V2.2 reproduction

**Scope:** finite TLA+ checks only; not historical Java or Alloy.

Results: **52/52**

| Case | Status | Distinct states |
|---|---|---:|
| `correct_shared` | PASS | 7035 |
| `correct_disjoint` | PASS | 13924 |
| `live_shared` | PASS | 7035 |
| `reach_ReachCommit` | PASS | 299 |
| `reach_ReachAbort` | PASS | 30 |
| `reach_ReachContention` | PASS | 31 |
| `reach_ReachPartialApplication` | PASS | 153 |
| `iso_LocalTimeoutAbort_Atomicity` | PASS | 541 |
| `iso_LocalTimeoutAbort_NoDoubleSpend` | PASS | 15611 |
| `iso_LocalTimeoutAbort_NoDoubleCredit` | PASS | 15611 |
| `iso_LocalTimeoutAbort_NoReceiptReplay` | PASS | 15611 |
| `iso_LocalTimeoutAbort_QuorumRequired` | PASS | 15611 |
| `iso_LocalTimeoutAbort_Conservation` | PASS | 541 |
| `iso_LocalTimeoutAbort_DecisionStable` | PASS | 15611 |
| `iso_LocalTimeoutAbort_TerminalStable` | PASS | 15611 |
| `type_LocalTimeoutAbort` | PASS | 15611 |
| `iso_QuorumBypass_Atomicity` | PASS | 9275 |
| `iso_QuorumBypass_NoDoubleSpend` | PASS | 9275 |
| `iso_QuorumBypass_NoDoubleCredit` | PASS | 9275 |
| `iso_QuorumBypass_NoReceiptReplay` | PASS | 9275 |
| `iso_QuorumBypass_QuorumRequired` | PASS | 12 |
| `iso_QuorumBypass_Conservation` | PASS | 9275 |
| `iso_QuorumBypass_DecisionStable` | PASS | 9275 |
| `iso_QuorumBypass_TerminalStable` | PASS | 9275 |
| `type_QuorumBypass` | PASS | 9275 |
| `iso_ReceiptReplay_Atomicity` | PASS | 8155 |
| `iso_ReceiptReplay_NoDoubleSpend` | PASS | 8155 |
| `iso_ReceiptReplay_NoDoubleCredit` | PASS | 574 |
| `iso_ReceiptReplay_NoReceiptReplay` | PASS | 313 |
| `iso_ReceiptReplay_QuorumRequired` | PASS | 8155 |
| `iso_ReceiptReplay_Conservation` | PASS | 1043 |
| `iso_ReceiptReplay_DecisionStable` | PASS | 8155 |
| `iso_ReceiptReplay_TerminalStable` | PASS | 313 |
| `type_ReceiptReplay` | PASS | 8155 |
| `iso_LockWithoutCheck_Atomicity` | PASS | 25113 |
| `iso_LockWithoutCheck_NoDoubleSpend` | PASS | 17336 |
| `iso_LockWithoutCheck_NoDoubleCredit` | PASS | 25113 |
| `iso_LockWithoutCheck_NoReceiptReplay` | PASS | 25113 |
| `iso_LockWithoutCheck_QuorumRequired` | PASS | 25113 |
| `iso_LockWithoutCheck_Conservation` | PASS | 25113 |
| `iso_LockWithoutCheck_DecisionStable` | PASS | 25113 |
| `iso_LockWithoutCheck_TerminalStable` | PASS | 25113 |
| `type_LockWithoutCheck` | PASS | 25113 |
| `iso_DecisionFlip_Atomicity` | PASS | 552 |
| `iso_DecisionFlip_NoDoubleSpend` | PASS | 9675 |
| `iso_DecisionFlip_NoDoubleCredit` | PASS | 9675 |
| `iso_DecisionFlip_NoReceiptReplay` | PASS | 9675 |
| `iso_DecisionFlip_QuorumRequired` | PASS | 9675 |
| `iso_DecisionFlip_Conservation` | PASS | 552 |
| `iso_DecisionFlip_DecisionStable` | PASS | 152 |
| `iso_DecisionFlip_TerminalStable` | PASS | 9675 |
| `type_DecisionFlip` | PASS | 9675 |

Counterexamples to explicitly negated reachability predicates and designated mutant properties count as PASS.
TypeOK for each mutant is independently checked. No source-to-model refinement or full-paper reproducibility is claimed.
TLC is invoked with -deadlock, which DISABLES deadlock checking.
