### Función de abstracción Java-TLA+

#### Propósito

La Fase 7B implementa una proyección determinista desde `TraceExecution` hacia una secuencia de estados y acciones con el vocabulario de `CrossShardCommit.tla`.

La función consume directamente los objetos de trazas definidos en la Fase 7A. No modifica el esquema JSONL, no vuelve a ejecutar el protocolo y no analiza texto libre para inventar datos ausentes.

El resultado todavía no afirma conformidad. La Fase 7C usará TLC para decidir si cada paso abstracto pertenece a `Next`.

#### Archivos principales

- `src/main/java/dltlab/conformance/AbstractAction.java`;
- `src/main/java/dltlab/conformance/AbstractProtocolState.java`;
- `src/main/java/dltlab/conformance/AbstractTrace.java`;
- `src/main/java/dltlab/conformance/AbstractTraceStep.java`;
- `src/main/java/dltlab/conformance/JavaToTlaActionMapper.java`;
- `src/main/java/dltlab/conformance/JavaToTlaStateMapper.java`;
- `src/test/java/dltlab/conformance/TraceAbstractionTest.java`.

#### Entrada

La entrada es una instancia inmutable de `TraceExecution` con:

- escenario;
- seed;
- cantidad de shards;
- quorum;
- hash de la traza de simulación;
- eventos concretos ordenados;
- estados finales por transferencia.

La función no incorpora un parser JSONL. El parser podrá agregarse cuando una fase posterior necesite consumir trazas almacenadas. La Fase 7B trabaja sobre el mismo contrato tipado que produce `TraceRecorder`.

#### Estado inicial

Todas las transferencias presentes en `finalStates` existen desde el estado abstracto inicial, como requiere `Init`:

- `status = Pending`;
- `terminalStatus = None`;
- `locked = FALSE`;
- `receiptOwner = None`;
- `receiptUseCount = 0`;
- `destinationCredit = FALSE`;
- `fundsReleased = FALSE`;
- `votes = {}`;
- `messages = {}`.

La primera decisión terminal se conserva y no se sobrescribe aunque un evento posterior cambie el estado observado.

La topología se obtiene de los metadatos estructurados de los eventos. La función rechaza cambios de `sourceShard`, `targetShard`, `sourceUtxoKey`, `receiptId` o `amount` dentro de una misma transferencia.

#### Proyección de estados

| Estado Java | Estado abstracto |
|---|---|
| `CREATED` | `Pending` |
| `SOURCE_LOCKED` | `Locked` |
| `RECEIPT_CREATED` | `Locked` |
| `RECEIPT_DELIVERED` | `Locked` |
| `DESTINATION_PREPARED` | `Prepared` |
| `COMMITTED` | `Committed` |
| `ABORTED` | `Aborted` |
| `TIMED_OUT` | `Aborted` |
| `FAILED_VALIDATION` | `Aborted` |

`FAILED_VALIDATION` se proyecta por su efecto terminal. TLC determinará en la Fase 7C si el contexto previo admite esa decisión mediante la acción abstracta elegida.

#### Proyección de acciones

| Acción Java u observación | Acción abstracta |
|---|---|
| `CREATE_SESSION` | `Stutter` |
| `LOCK_SOURCE` | `LockTransfer` |
| `CREATE_RECEIPT` | `Stutter` |
| `DELIVER_RECEIPT` | `Stutter` |
| `PREPARE_DESTINATION` | `ConsumeReceipt` y votos canónicos |
| `COMMIT_DESTINATION` | `CommitTransfer` |
| `ABORT_TRANSFER` | `TimeoutTransfer` por sus efectos |
| `EXPIRE_TRANSFER` | `TimeoutTransfer` |
| `FAIL_VALIDATION` | `TimeoutTransfer` por sus efectos |
| observación de simulación | `Stutter` |

La proyección no verifica las guardas de esas acciones. Por ejemplo, un `TimeoutTransfer` producido desde un estado no permitido seguirá presente en la traza abstracta y podrá ser rechazado por TLC.

#### Expansión de preparación y votos

Java conserva `targetApprovals` como un conteo, mientras TLA+ representa `votes[t]` como un conjunto de validadores.

Por ello, un evento `PREPARE_DESTINATION` se expande de forma determinista:

1. `ConsumeReceipt(t, 1)`;
2. `CastVote(t, v1)`;
3. `CastVote(t, v2)`;
4. una acción adicional por cada aprobación concreta restante.

Los identificadores `v1`, `v2` y siguientes son canónicos y sintéticos. No se presentan como identidades criptográficas reales. La cantidad de votos sí conserva el conteo observado por Java.

Cada paso expandido registra:

- `concreteStep` de procedencia;
- `expansionIndex` dentro del evento;
- indicador `synthetic`;
- justificación en español.

#### Mensajes de recibo

La Fase 7A no registra una identidad estructurada para cada copia de mensaje ni un campo booleano de retraso. Esos datos aparecen como observaciones de simulación, pero no forman un contrato suficiente para reconstruir cada elemento de `messages` sin analizar texto libre.

La Fase 7B adopta una abstracción canónica y declarada:

- `LockTransfer` crea la copia lista número `1`;
- `ConsumeReceipt` consume esa copia;
- `TimeoutTransfer` retira los mensajes de la transferencia;
- pérdida, retraso, duplicación, entrega rechazada y disponibilidad de shards se proyectan como `Stutter`.

Esta decisión conserva el comportamiento del protocolo sin atribuir a la traza información que no contiene. `ReleaseDelayedReceipt` permanece en el vocabulario abstracto, pero no es emitida por el mapeador actual.

#### Variables abstractas

`AbstractProtocolState` conserva:

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

Los shards se mantienen como enteros Java. La Fase 7C será responsable de serializarlos como átomos TLA+ estables, sin cambiar su identidad.

#### Separación de responsabilidades

La Fase 7B realiza:

- proyección de eventos;
- expansión determinista de votos;
- construcción de estados abstractos;
- validación de identidad y topología;
- verificación de que el estado final proyectado corresponde al estado final Java.

La Fase 7B no realiza:

- evaluación de `Init` o `Next`;
- ejecución de TLC;
- clasificación de conformidad;
- generación de contraejemplos;
- reparación de trazas;
- modificación de TLA+ o Alloy.

#### Gate de Fase 7B

La fase queda cerrada cuando:

- los diez escenarios producen la misma abstracción para la misma seed;
- toda observación concreta genera al menos un paso abstracto;
- las identidades de transferencia y shard permanecen estables;
- la preparación expande exactamente el conteo de votos observado;
- commit, timeout y fallo de quorum producen el estado abstracto esperado;
- los eventos de red se registran como `Stutter`;
- no se agregan resultados TLC ni decisiones de conformidad;
- las pruebas anteriores permanecen en verde.

#### Límite científico

La función implementada es una función de abstracción propuesta y ejecutable. Su existencia no demuestra que todas las trazas producidas por Java sean aceptadas por TLA+.

La aceptación o el rechazo corresponde a la Fase 7C mediante replay formal con TLC.
