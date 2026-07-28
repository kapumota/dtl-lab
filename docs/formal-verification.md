### Verificación formal de commit cross-shard

#### Propósito

DLT-Lab combina TLA+ y Alloy para estudiar un protocolo cross-shard concurrente y acotado. La verificación formal complementa las pruebas Java, los escenarios deterministas y las invariantes runtime.

#### Modelos TLA+

```text
specs/tla/CrossShardCommit.tla
specs/tla/CrossShardCommit.cfg
specs/tla/configs/
specs/tla/mutants/
```

El modelo representa varias transferencias mediante funciones indexadas por sesión. Las variables principales son `status`, `terminalStatus`, `sourceShard`, `targetShard`, `locked`, `receiptOwner`, `receiptUseCount`, `destinationCredit`, `fundsReleased`, `messages` y `votes`.

`terminalStatus` registra la primera decisión terminal abstracta. Su valor permite verificar que una transferencia no abandona `Committed` o `Aborted` después de alcanzar esa decisión.

Las configuraciones válidas cubren una, dos y tres transferencias, recibos duplicados, mensajes retrasados y carreras de timeout.

#### Modelos Alloy

```text
specs/alloy/CrossShardCommit.als
specs/alloy/scopes/
specs/alloy/mutants/
```

Alloy usa `open util/ordering[State]` para representar una secuencia finita de estados. El modelo incluye `State`, `Transfer`, `Receipt`, `Shard`, `Validator` y `Message`.

#### Propiedades ejecutadas

- `NoReceiptReplay`;
- `DestinationCreditRequiresValidReceipt`;
- `DecisionConsistency`;
- `NoValueLossAtTermination`;
- `TerminalStateIrreversibility`;
- `EventuallyReleasedAfterTimeout`;
- `QuorumRequired`.

`EventuallyReleasedAfterTimeout` se interpreta como una garantía acotada de estado: todo estado `Aborted` observado ya liberó los fondos. La vivacidad temporal general y los supuestos de fairness permanecen fuera del alcance del baseline.

#### Perfil educativo

```bash
make validate
```

Este perfil valida estructura y convenciones sin descargar herramientas formales. Su resultado no debe describirse como model checking ejecutado.

#### Perfil científico

```bash
bash scripts/formal/install_tla_tools.sh
bash scripts/formal/install_alloy.sh
make formal-research
```

El perfil científico ejecuta seis configuraciones válidas TLA+, cinco mutantes TLA+, un modelo válido Alloy y cinco mutantes Alloy.

#### Propiedad objetivo de mutantes

| Mutante | Propiedad objetivo |
|---|---|
| `NoReplayProtection` | `NoReceiptReplay` |
| `CreditBeforeReceipt` | `DestinationCreditRequiresValidReceipt` |
| `CommitAfterAbort` | `TerminalStateIrreversibility` |
| `TimeoutWithoutRelease` | `EventuallyReleasedAfterTimeout` |
| `QuorumBypass` | `QuorumRequired` |

Un mutante Alloy solo cumple su expectativa cuando la propiedad objetivo aparece entre las propiedades violadas. Otras violaciones se registran como evidencia adicional y no sustituyen al objetivo.

#### Resultados estructurados

```text
results/formal/tool_versions.txt
results/formal/environment.json
results/formal/tla_runs.csv
results/formal/alloy_runs.csv
results/formal/mutant_matrix.csv
results/formal/execution_manifest.json
results/formal/logs/
results/formal/counterexamples/
```

TLC registra estados generados, estados distintos, profundidad, tiempo, memoria y propiedad violada. Alloy registra solver, alcance, propiedad objetivo, propiedades violadas, contraejemplos, duración, tiempo total y memoria.

El manifiesto distingue `source_commit`, `checked_out_commit` y `source_ref`. Esta separación evita confundir el commit de la rama con el commit sintético usado por un evento `pull_request`.

#### Relación con Java

Los modelos representan el protocolo abstracto. No demuestran por sí solos conformidad con la implementación Java. La exportación de trazas, la función de abstracción y el checker corresponden a la Fase 7.

#### Limitación

Un resultado válido significa que no se encontró un contraejemplo dentro de las configuraciones y bounds declarados. No significa verificación ilimitada, prueba de vivacidad general ni equivalencia automática con toda la implementación.
