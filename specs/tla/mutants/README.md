### Mutantes TLA+

#### Propósito

Cada módulo conserva el modelo multisesión y activa una alteración mediante `MutationMode`.

#### Catálogo

| Mutante | Propiedad objetivo |
|---|---|
| `NoReplayProtection.tla` | `NoReceiptReplay` |
| `CreditBeforeReceipt.tla` | `DestinationCreditRequiresValidReceipt` |
| `CommitAfterAbort.tla` | `TerminalStateIrreversibility` |
| `TimeoutWithoutRelease.tla` | `EventuallyReleasedAfterTimeout` |
| `QuorumBypass.tla` | `QuorumRequired` |
