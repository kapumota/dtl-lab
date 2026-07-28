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

El modelo representa varias transferencias mediante funciones indexadas por sesión. Las variables principales son `status`, `sourceShard`, `targetShard`, `locked`, `receiptOwner`, `receiptUseCount`, `destinationCredit`, `fundsReleased`, `messages` y `votes`.

Las configuraciones válidas cubren una, dos y tres transferencias, recibos duplicados, mensajes retrasados y carreras de timeout.

#### Modelos Alloy

```text
specs/alloy/CrossShardCommit.als
specs/alloy/scopes/
specs/alloy/mutants/
```

Alloy usa `open util/ordering[State]` para representar una secuencia finita de estados. El modelo incluye `State`, `Transfer`, `Receipt`, `Shard`, `Validator` y `Message`.

#### Propiedades

- `NoReceiptReplay`;
- `DestinationCreditRequiresValidReceipt`;
- `DecisionConsistency`;
- `EventuallyReleasedAfterTimeout`;
- `QuorumRequired`.

`EventuallyReleasedAfterTimeout` se interpreta en esta fase como una garantía acotada de que todo estado `Aborted` observado ya liberó los fondos. La vivacidad no acotada y los supuestos de fairness permanecen fuera del alcance.

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

El perfil científico ejecuta:

- seis configuraciones válidas TLA+;
- cinco mutantes TLA+;
- un modelo válido Alloy;
- cinco mutantes Alloy.

El perfil falla si una configuración válida produce una violación, si un mutante no produce la violación esperada, si falta un reporte o si no se almacena el contraejemplo.

#### Mutantes científicos

| Mutante | Propiedad esperada |
|---|---|
| `NoReplayProtection` | `NoReceiptReplay` |
| `CreditBeforeReceipt` | `DestinationCreditRequiresValidReceipt` |
| `CommitAfterAbort` | `DecisionConsistency` |
| `TimeoutWithoutRelease` | `EventuallyReleasedAfterTimeout` |
| `QuorumBypass` | `QuorumRequired` |

Los mutantes están versionados en TLA+ y Alloy. Ya no se generan copias temporales mediante sustituciones de texto.

#### Resultados estructurados

```text
results/formal/
├── tool_versions.txt
├── environment.json
├── tla_runs.csv
├── alloy_runs.csv
├── mutant_matrix.csv
├── execution_manifest.json
├── logs/
└── counterexamples/
```

TLC registra estados generados, estados distintos, profundidad, tiempo, memoria y propiedad violada. Alloy registra solver, alcance, contraejemplos, duración, tiempo total y memoria.

#### Integración continua

El workflow `.github/workflows/formal-verification.yml` instala las versiones fijadas, ejecuta la matriz completa y publica `results/formal/` como artefacto.

#### Relación con Java

Los modelos representan el protocolo abstracto. No demuestran por sí solos conformidad con la implementación Java. La función de abstracción y el checker de trazas corresponden a la Fase 7.

#### Limitación

Un resultado válido significa que no se encontró un contraejemplo dentro de las configuraciones y bounds declarados. No significa verificación ilimitada, prueba de vivacidad general ni equivalencia automática con toda la implementación.
