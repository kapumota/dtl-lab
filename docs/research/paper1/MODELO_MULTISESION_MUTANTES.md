### Modelo multisesión y mutantes científicos

#### Objetivo

La Fase 6 representa sesiones concurrentes, mensajes, recibos, votos, commit y timeout dentro de configuraciones acotadas. La Fase 6.1 completa el conjunto de propiedades y endurece la trazabilidad de mutantes y resultados.

#### Modelo TLA+

Variables principales:

- `status`;
- `terminalStatus`;
- `sourceShard`;
- `targetShard`;
- `locked`;
- `receiptOwner`;
- `receiptUseCount`;
- `destinationCredit`;
- `fundsReleased`;
- `messages`;
- `votes`.

`terminalStatus` conserva la primera decisión terminal y permite detectar transiciones posteriores incompatibles.

#### Configuraciones válidas

- `2shards-1transfer.cfg`;
- `2shards-2transfers.cfg`;
- `3shards-3transfers.cfg`;
- `duplicate-receipt.cfg`;
- `delayed-message.cfg`;
- `timeout-race.cfg`.

#### Propiedades

| Propiedad | Interpretación acotada |
|---|---|
| `NoReceiptReplay` | un recibo no se consume más de una vez |
| `DestinationCreditRequiresValidReceipt` | todo crédito requiere recibo válido |
| `DecisionConsistency` | commit no conserva liberación de abort |
| `NoValueLossAtTermination` | un estado terminal conserva el valor |
| `TerminalStateIrreversibility` | la primera decisión terminal no cambia |
| `EventuallyReleasedAfterTimeout` | todo estado abortado observado liberó fondos |
| `QuorumRequired` | todo commit observado reúne quorum |

#### Catálogo de mutantes

| Mutante | Propiedad objetivo |
|---|---|
| `NoReplayProtection` | `NoReceiptReplay` |
| `CreditBeforeReceipt` | `DestinationCreditRequiresValidReceipt` |
| `CommitAfterAbort` | `TerminalStateIrreversibility` |
| `TimeoutWithoutRelease` | `EventuallyReleasedAfterTimeout` |
| `QuorumBypass` | `QuorumRequired` |

Un mutante puede violar propiedades adicionales. El pipeline exige que la propiedad objetivo aparezca entre las violaciones detectadas.

#### Evidencia

El perfil científico conserva métricas TLC, recibos Alloy, propiedades objetivo, propiedades violadas, matriz de mutantes, resúmenes JSON, contraejemplos y manifiesto con procedencia.

#### Limitación

Las propiedades se evalúan dentro de bounds explícitos. La fase no demuestra refinamiento Java-TLA+, fairness general ni verificación ilimitada.
