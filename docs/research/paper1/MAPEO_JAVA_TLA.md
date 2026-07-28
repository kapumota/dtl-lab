### Mapeo ejecutable entre Java y TLA+

#### Propósito

Este documento describe la función de abstracción implementada en la Fase 7B. La función proyecta objetos `TraceExecution` de la Fase 7A en acciones y estados con el vocabulario de `CrossShardCommit.tla`.

Todavía no constituye un checker de conformidad ni una prueba de refinamiento. La validez de cada transición será evaluada por TLC en la Fase 7C.

#### Implementación

El mapeo se implementa mediante:

- `JavaToTlaActionMapper` para acciones;
- `JavaToTlaStateMapper` para estados y secuencias;
- `AbstractAction` para el vocabulario TLA+;
- `AbstractProtocolState` para las variables abstractas;
- `AbstractTraceStep` para conservar procedencia y expansión;
- `AbstractTrace` para agrupar la proyección completa.

El contrato concreto `TraceEvent` no se modifica.

#### Correspondencia de estados

| Estado Java | Estado abstracto | Observación |
|---|---|---|
| `CREATED` | `status[t] = "Pending"` | la sesión ya existe en `Init` |
| `SOURCE_LOCKED` | `status[t] = "Locked"` | origen bloqueado |
| `RECEIPT_CREATED` | `status[t] = "Locked"` | detalle agrupado en `LockTransfer` |
| `RECEIPT_DELIVERED` | `status[t] = "Locked"` | entrega concreta omitida |
| `DESTINATION_PREPARED` | `status[t] = "Prepared"` | recibo consumido |
| `COMMITTED` | `status[t] = "Committed"` | decisión terminal de commit |
| `ABORTED` | `status[t] = "Aborted"` | decisión terminal sin commit |
| `TIMED_OUT` | `status[t] = "Aborted"` | timeout con liberación |
| `FAILED_VALIDATION` | `status[t] = "Aborted"` | efecto terminal proyectado |

`terminalStatus[t]` toma `Committed` o `Aborted` al producirse la primera acción terminal abstracta y no se sobrescribe después.

#### Correspondencia de variables

| Evidencia Java | Variable abstracta |
|---|---|
| `transferId` | índice `t` de `Transfers` |
| `sourceShard` | `sourceShard[t]` |
| `targetShard` | `targetShard[t]` |
| `LOCK_SOURCE` | `locked[t] = TRUE` |
| consumo abstracto | `receiptOwner[t] = targetShard[t]` |
| consumo abstracto | `receiptUseCount[t] = 1` |
| preparación destino | `destinationCredit[t] = TRUE` |
| decisión terminal sin commit | `fundsReleased[t] = TRUE` |
| copia canónica de recibo | `messages` |
| `targetApprovals` | cardinalidad de `votes[t]` |
| primera decisión terminal | `terminalStatus[t]` |

La función conserva los shards como enteros. La conversión a átomos como `s0` y `s1` corresponde al generador de replay de la Fase 7C.

#### Correspondencia de acciones

| Evento concreto | Acción abstracta |
|---|---|
| `CREATE_SESSION` | `Stutter` |
| `LOCK_SOURCE` | `LockTransfer(t)` |
| `CREATE_RECEIPT` | `Stutter` |
| `DELIVER_RECEIPT` | `Stutter` |
| `PREPARE_DESTINATION` | `ConsumeReceipt(t, 1)` y `CastVote` |
| `COMMIT_DESTINATION` | `CommitTransfer(t)` |
| `ABORT_TRANSFER` | `TimeoutTransfer(t)` por sus efectos |
| `EXPIRE_TRANSFER` | `TimeoutTransfer(t)` |
| `FAIL_VALIDATION` | `TimeoutTransfer(t)` por sus efectos |
| evento de red o planificador | `Stutter` |

La función no comprueba si la acción abstracta está habilitada. Esa separación evita duplicar las guardas de `Next` en Java.

#### Expansión de acciones

Una acción Java puede producir más de una acción abstracta.

`PREPARE_DESTINATION` se expande en:

1. una acción `ConsumeReceipt`;
2. una acción `CastVote` por cada aprobación observada.

Los votos se identifican de forma canónica como `v1`, `v2` y siguientes. Su identidad es sintética, pero su cantidad coincide con `targetApprovals`.

#### Mensajes y fallos de red

La abstracción actual usa una copia canónica lista con identificador `1`.

Los eventos `DROP_MESSAGE`, `DELAY_MESSAGE`, `DUPLICATE_MESSAGE`, `SHARD_OFFLINE`, `SHARD_ONLINE` y entregas rechazadas se proyectan como `Stutter` porque Fase 7A no conserva copia y retraso como campos estructurados independientes.

No se analiza el campo `detail` para reconstruir mensajes. Esta restricción evita depender de cadenas de texto y evita inventar datos formales.

#### Identidad y topología

Para una misma transferencia deben permanecer constantes:

- `transferId`;
- `sourceShard`;
- `targetShard`;
- `sourceUtxoKey`;
- `receiptId`;
- `amount`.

La función rechaza una traza que modifique cualquiera de esos campos dentro de la sesión.

#### Estado inicial y estado final

Todas las transferencias de `finalStates` se crean en el estado abstracto inicial `Pending`.

Al finalizar, la función comprueba que cada estado Java tenga la proyección esperada. Esta comprobación valida completitud del mapeo, no conformidad con `Next`.

#### Detalles omitidos por la abstracción

- criptografía;
- estructura completa del UTXO;
- valor de cambio;
- contenido criptográfico del recibo;
- snapshot y rollback interno;
- excepciones concretas;
- nombres reales de validadores;
- identidad de copias de red no estructuradas;
- causa concreta de una decisión terminal sin commit.

La omisión es explícita y permanece visible en la documentación y en la justificación de cada `AbstractAction`.

#### Restricción de redacción

Hasta cerrar la Fase 7 se usarán expresiones como `función de abstracción propuesta`, `traza abstracta ejecutable` y `conformidad acotada pendiente`.

No se usarán `refinamiento demostrado`, `equivalencia Java-TLA+` ni `verificación completa de la implementación`.
