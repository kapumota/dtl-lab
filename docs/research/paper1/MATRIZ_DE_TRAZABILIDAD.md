### Matriz de trazabilidad inicial

#### Propósito

Esta matriz relaciona las propiedades científicas esperadas con el código, las especificaciones formales y las brechas que deben cerrarse.

#### Matriz

| Propiedad o elemento | Evidencia Java actual | Evidencia formal actual | Brecha identificada | Fase prevista |
|---|---|---|---|---|
| Estado de sesión | nueve estados en `CrossShardStatus` y tabla ejecutable en `TransitionTable` | variables booleanas y estado abstracto | falta trasladar los estados Java al modelo formal multisesión | Fase 6 |
| Historial de sesión | `ProtocolEvent` y `CrossShardSession.events()` | no existe traza formal importable | falta formato común y checker de conformidad | Fase 7 |
| Bloqueo de origen | `markSourceLocked`, `AtomicCommitProtocol.begin` y escenario `S06` | `LockOrigin` en TLA+ | Java explora conflictos deterministas; falta trasladarlos al modelo formal | Fase 6 |
| Creación y entrega de recibo | `NetworkMessage`, `NetworkFaultModel`, `SEND_RECEIPT` y `DELIVER_RECEIPT` | `CreateReceipt` abstracto | red Java explícita; falta representación formal de mensajes | Fase 6 |
| Consumo de recibo | `applyCommit`, `DuplicateReceiptModel` y escenario `S03` | `receiptUseCount` y `NoReceiptReplay` | duplicación probada en Java; falta exploración formal multisesión | Fase 6 |
| Protección runtime contra replay | ataque existente, invariante y traza determinista de `S03` | `NoReceiptReplay` en TLA+ y Alloy | falta relación automática entre traza Java y modelo | Fases 6 y 7 |
| Conservación de valor | rollback de Fase 3 y escenarios concurrentes `S06`, `S07` y `S10` | `NoValueLoss` y `TimeoutReleasesFunds` | exploración Java acotada; falta model checking multisesión | Fase 6 |
| Decisión atómica | `AtomicCommitProtocol` y carrera determinista `S05` | `AtomicCommit` | interleavings Java probados; falta equivalencia con acciones formales | Fases 6 y 7 |
| Irreversibilidad terminal | ausencia de salidas terminales en `TransitionTable` y pruebas específicas | propiedad no separada | falta agregar `TerminalStateIrreversibility` al modelo formal | Fase 6 |
| Timeout | `EXPIRE_TRANSFER` solo desde estados con bloqueo | `TimeoutOrigin` y `TimeoutReleasesFunds` | falta formalizar liveness y fairness | Fase 6 |
| Quorum | validación en origen y destino dentro de `AtomicCommitProtocol` | no existe una propiedad formal específica | falta `QuorumRequired` en TLA+ y Alloy | Fase 6 |
| Fallos de red | seis `NetworkFaultModel`, mensajes y escenarios `S02` a `S05` | no existe una red explícita de mensajes | falta incorporar red y fairness al modelo formal | Fase 6 |
| Model checking ejecutado | `make formal-research`, parsers y workflow obligatorio | TLC y Alloy ejecutados con versiones fijadas | falta ampliar el modelo a múltiples sesiones y fallos de red | Fase 6 |
| Conformidad Java-TLA+ | eventos Java ordenados por sesión | modelo abstracto | falta función de abstracción, serialización y checker | Fase 7 |
| Reproducibilidad científica | seeds explícitas y herramientas formales fijadas | CSV, JSON, logs, versiones y ambiente | falta congelar la matriz experimental y preparar el snapshot del envío | Fase 8 |

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

#### Evidencia agregada en la Fase 4

- `SimulationClock` y `EventScheduler` eliminan dependencias del reloj físico y de threads.
- `EventQueue` ordena por ronda, prioridad y secuencia.
- `DeterministicRandom` conserva una secuencia estable para cada seed.
- seis modelos de red cubren entrega normal, pérdida, retraso, duplicación, reordenamiento y carrera de timeout.
- `ScenarioCatalog` implementa `S01` a `S10`.
- `SimulationDeterminismTest` compara trazas y hashes de ejecuciones repetidas.
- `SimulationScenarioMatrixTest` ejecuta 100 seeds por escenario en el runner reducido.
- `run_simulation_matrix.sh` permite ejecutar 1000 o más seeds por escenario localmente.


#### Evidencia agregada en la Fase 5

- el perfil educativo comprueba estructura sin afirmar ejecución formal;
- el perfil científico falla si falta TLC o Alloy;
- TLA+ Tools 1.7.4 y Alloy 6.2.0 están fijados;
- TLC registra estados generados, estados distintos, profundidad, tiempo y memoria;
- Alloy registra solver, alcance, contraejemplos, tiempo y memoria;
- cada propiedad genera una fila estructurada;
- controles negativos temporales comprueban que el pipeline detecta violaciones;
- `formal-verification.yml` publica `results/formal/`;
- los mutantes científicos y el modelo multisesión permanecen asignados a la Fase 6.

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
