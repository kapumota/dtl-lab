### Matriz de trazabilidad inicial

#### Propósito

Esta matriz relaciona las propiedades científicas esperadas con el código, las especificaciones formales y las brechas que deben cerrarse.

#### Matriz

| Propiedad o elemento | Evidencia Java actual | Evidencia formal actual | Brecha identificada | Fase prevista |
|---|---|---|---|---|
| Estado de sesión | nueve estados en `CrossShardStatus` y tabla ejecutable en `TransitionTable` | variables booleanas y estado abstracto | falta trasladar los estados Java al modelo formal multisesión | Fase 6 |
| Historial de sesión | `ProtocolEvent` y `CrossShardSession.events()` | no existe traza formal importable | falta formato común y checker de conformidad | Fase 7 |
| Bloqueo de origen | `markSourceLocked` y `AtomicCommitProtocol.begin` | `LockOrigin` en TLA+ | falta modelar varias sesiones y conflictos concurrentes | Fases 4 y 6 |
| Creación y entrega de recibo | `markReceiptCreated` y `markReceiptDelivered` | `CreateReceipt` abstracto | falta una red explícita con entrega, retraso y duplicación | Fases 4 y 6 |
| Consumo de recibo | `AtomicCommitProtocol.commitReceipt` y `applyCommit` | `receiptUseCount` y `NoReceiptReplay` | falta estudiar consumo entre múltiples shards y sesiones | Fases 4 y 6 |
| Protección runtime contra replay | `CrossShardReplayAttack` y `NoReceiptReplayInvariant` | `NoReceiptReplay` en TLA+ y Alloy | falta relacionar el ataque con trazas adversariales y mutantes | Fases 4 y 6 |
| Conservación de valor | `CommitPlan`, `LedgerSnapshot`, `applyCommit` y `rollback` | `NoValueLoss` y `TimeoutReleasesFunds` | rollback probado con fallos controlados; falta exploración multisesión | Fases 4 y 6 |
| Decisión atómica | `AtomicCommitProtocol`, snapshot restaurable y decisión terminal única | `AtomicCommit` | falta comprobar interleavings concurrentes y relacionarlos con el modelo formal | Fases 4 y 6 |
| Irreversibilidad terminal | ausencia de salidas terminales en `TransitionTable` y pruebas específicas | propiedad no separada | falta agregar `TerminalStateIrreversibility` al modelo formal | Fase 6 |
| Timeout | `EXPIRE_TRANSFER` solo desde estados con bloqueo | `TimeoutOrigin` y `TimeoutReleasesFunds` | falta formalizar liveness y fairness | Fase 6 |
| Quorum | validación en origen y destino dentro de `AtomicCommitProtocol` | no existe una propiedad formal específica | falta `QuorumRequired` en TLA+ y Alloy | Fase 6 |
| Fallos de red | disponibilidad de validadores y shard offline | no existe una red explícita de mensajes | faltan retraso, pérdida, duplicación y reordenamiento | Fases 4 y 6 |
| Model checking ejecutado | ejecución opcional de TLC en `run_formal_checks.sh` | modelos presentes | el perfil actual puede finalizar sin TLC y no automatiza Alloy | Fase 5 |
| Conformidad Java-TLA+ | eventos Java ordenados por sesión | modelo abstracto | falta función de abstracción, serialización y checker | Fase 7 |
| Reproducibilidad científica | pruebas sin dependencias externas y validación general | configuración TLA+ básica | faltan manifiestos, versiones fijadas y resultados estructurados | Fases 5 y 8 |

#### Evidencia agregada en la Fase 2

- `CrossShardSession` inicia en `CREATED` y conserva un historial inmutable.
- `TransitionTable` relaciona estado anterior, acción y estado siguiente.
- `InvalidTransitionException` informa la transición rechazada.
- `CrossShardStatus.isTerminal` centraliza la clasificación de estados terminales.
- `ShardManager` registra las transiciones intermedias sin cambiar sus métodos públicos.
- `NoStuckCrossShardInvariant` ignora todos los estados no terminales mediante `isTerminal`.
- tres suites verifican recorridos permitidos, irreversibilidad terminal y transiciones inválidas.

#### Evidencia agregada en la Fase 3

- `ShardManager` delega inicio, entrega, commit, abort y timeout.
- `CommitPlan` separa cálculo de mutaciones y aplicación.
- `LedgerSnapshot` conserva ledger, bloqueo, recibo y checkpoint de sesión.
- `rollback` restaura el estado aunque el fallo ocurra después del crédito destino.
- `ProtocolContext.FailurePoint` hace reproducibles cuatro fallos intermedios.
- `AtomicCommitRollbackTest` comprueba restauración del origen, eliminación del crédito, recibo y estado.
- `AtomicCommitProtocolTest` comprueba compatibilidad y una única decisión terminal.

#### Regla de actualización

Cada Pull Request del Paper 1 deberá actualizar esta matriz cuando:

- agregue o cambie una propiedad;
- cambie una transición Java;
- modifique TLA+ o Alloy;
- agregue un ataque o mutante;
- agregue resultados o herramientas de verificación.

#### Aportes documentales de la Fase 1

- `PROTOCOLO.md` fija precondiciones, flujos y garantías esperadas.
- `MAQUINA_DE_ESTADOS.md` definió los estados intermedios implementados en la Fase 2.
- `PROPIEDADES_DE_SEGURIDAD.md` define diez propiedades y su nivel de evidencia.
- `PROPIEDADES_DE_VIVACIDAD.md` separa invariantes de estado y propiedades temporales.
- `MODELO_DE_FALLOS.md` delimita fallos de red, validadores e implementación.
- `MAPEO_JAVA_TLA.md` registra correspondencias conceptuales y brechas de conformidad.
