### Propiedades de seguridad

#### Criterio de evidencia

Una propiedad se presenta como verificada solo cuando tiene definición no ambigua, correspondencia con el protocolo, ejecución reproducible, configuración identificable y bounds explícitos.

#### Matriz de evidencia

| Propiedad | Java runtime | TLA+ | Alloy | Mutante | Estado |
|---|---|---|---|---|---|
| `NoReceiptReplay` | protección y ataque de replay | invariante | assertion | `NoReplayProtection` | verificada dentro de bounds |
| `DestinationCreditRequiresValidReceipt` | validación de recibo y commit | invariante | assertion | `CreditBeforeReceipt` | verificada dentro de bounds |
| `DecisionConsistency` | estados terminales y rollback | invariante | assertion | efecto adicional de `CommitAfterAbort` | verificada dentro de bounds |
| `NoValueLossAtTermination` | commit, timeout y rollback | invariante | assertion | efecto adicional de `CommitAfterAbort` y `TimeoutWithoutRelease` | verificada dentro de bounds |
| `TerminalStateIrreversibility` | tabla de transiciones terminales | invariante con `terminalStatus` | assertion entre estados consecutivos | `CommitAfterAbort` | verificada dentro de bounds |
| `QuorumRequired` | comprobación de quorum | invariante | assertion | `QuorumBypass` | verificada dentro de bounds |
| `EventuallyReleasedAfterTimeout` | timeout libera el origen | invariante de estado | assertion de estado | `TimeoutWithoutRelease` | verificada como safety acotada |
| `UniqueSessionIdentity` | rechazo de identificadores repetidos | no modelada | no modelada | no disponible | evidencia runtime |

#### Alias históricos

Los nombres históricos se conservan como compatibilidad conceptual:

- `NoDoubleMint` combina replay y crédito válido;
- `NoValueLoss` referencia `NoValueLossAtTermination`;
- `AtomicCommit` referencia consistencia de decisión;
- `TimeoutReleasesFunds` referencia la invariante acotada de liberación.

Los reportes científicos usan los nombres explícitos de la matriz.

#### Irreversibilidad terminal

TLA+ registra la primera decisión terminal en `terminalStatus`. Una transferencia no puede cambiar de `Committed` a `Aborted`, de `Aborted` a `Committed` ni volver a un estado no terminal sin violar la invariante.

Alloy compara estados consecutivos y exige conservar el estado terminal.

#### Limitación

La ausencia de contraejemplos se limita a los bounds y configuraciones ejecutados. No constituye una prueba general para cualquier cantidad de shards, transferencias, mensajes o validadores.
