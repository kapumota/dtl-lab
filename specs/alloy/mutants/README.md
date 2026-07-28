### Mutantes Alloy

#### Propósito

Cada modelo introduce una transición defectuosa. El pipeline registra una propiedad objetivo y exige un contraejemplo para esa propiedad.

#### Catálogo

| Mutante | Propiedad objetivo |
|---|---|
| `NoReplayProtection.als` | `NoReceiptReplay` |
| `CreditBeforeReceipt.als` | `DestinationCreditRequiresValidReceipt` |
| `CommitAfterAbort.als` | `TerminalStateIrreversibility` |
| `TimeoutWithoutRelease.als` | `EventuallyReleasedAfterTimeout` |
| `QuorumBypass.als` | `QuorumRequired` |

Un modelo puede producir contraejemplos adicionales. Esas violaciones se registran, pero no reemplazan la propiedad objetivo.
