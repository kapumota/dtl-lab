### Máquina de estados del protocolo cross-shard

#### Propósito

Este documento describe la máquina de estados Java implementada en la Fase 2 y distingue sus garantías runtime de las propiedades formales que se verificarán en fases posteriores.

#### Estados implementados

`CrossShardStatus` contiene:

```text
CREATED
SOURCE_LOCKED
RECEIPT_CREATED
RECEIPT_DELIVERED
DESTINATION_PREPARED
COMMITTED
ABORTED
TIMED_OUT
FAILED_VALIDATION
```

Los primeros cinco estados son no terminales. Los cuatro estados restantes son terminales.

#### Estado inicial

Una sesión se crea en `CREATED`. El constructor registra un evento inicial con la acción `CREATE_SESSION`, el tiempo lógico de la ronda inicial y un estado anterior nulo.

El constructor no afirma que el UTXO ya fue bloqueado. `ShardManager.beginAtomicTransfer` registra después las acciones reales:

```text
CREATED
  -> SOURCE_LOCKED
  -> RECEIPT_CREATED
```

Cuando el shard origen no alcanza quorum, la sesión cambia directamente:

```text
CREATED -> FAILED_VALIDATION
```

#### Recorrido de commit

El recorrido exitoso completo es:

```text
CREATED
  -> SOURCE_LOCKED
  -> RECEIPT_CREATED
  -> RECEIPT_DELIVERED
  -> DESTINATION_PREPARED
  -> COMMITTED
```

`AtomicCommitProtocol.commit` registra la entrega cuando es necesaria. `prepareCommit` valida el destino y `applyCommit` registra preparación y commit. Si la aplicación falla, el checkpoint restaura la sesión a `RECEIPT_DELIVERED`.

#### Tabla de transiciones principales

| Estado actual | Acción | Estado siguiente | Condición runtime |
|---|---|---|---|
| `CREATED` | `LOCK_SOURCE` | `SOURCE_LOCKED` | UTXO bloqueado con quorum en origen |
| `SOURCE_LOCKED` | `CREATE_RECEIPT` | `RECEIPT_CREATED` | recibo asociado a la sesión |
| `RECEIPT_CREATED` | `DELIVER_RECEIPT` | `RECEIPT_DELIVERED` | intento de procesamiento en destino |
| `RECEIPT_DELIVERED` | `PREPARE_DESTINATION` | `DESTINATION_PREPARED` | quorum y recibo aceptados en destino |
| `DESTINATION_PREPARED` | `COMMIT_DESTINATION` | `COMMITTED` | aplicación del débito y crédito completada |

#### Transiciones de salida

`ABORT_TRANSFER` y `FAIL_VALIDATION` se permiten desde cualquier estado no terminal.

`EXPIRE_TRANSFER` se permite desde:

```text
SOURCE_LOCKED
RECEIPT_CREATED
RECEIPT_DELIVERED
DESTINATION_PREPARED
```

No se permite timeout desde `CREATED`, porque todavía no existe un bloqueo que liberar.

#### Estados terminales

Una sesión es terminal cuando su estado es:

- `COMMITTED`;
- `ABORTED`;
- `TIMED_OUT`;
- `FAILED_VALIDATION`.

`CrossShardStatus.isTerminal` concentra esta clasificación. `CrossShardSession.isTerminal` delega en el estado y evita repetir comparaciones distribuidas.

Ningún estado terminal tiene transiciones salientes en `TransitionTable`.

#### Transiciones prohibidas

La tabla rechaza, entre otras, las siguientes operaciones:

```text
COMMITTED -> ABORTED
TIMED_OUT -> COMMITTED
ABORTED -> FAILED_VALIDATION
FAILED_VALIDATION -> SOURCE_LOCKED
CREATED -> COMMITTED
CREATED -> TIMED_OUT
CREATED -> RECEIPT_CREATED
```

También se rechazan:

- doble commit;
- doble abort;
- timeout repetido;
- acción que no corresponde con el cambio de estado;
- tiempo lógico menor que el último tiempo registrado;
- razón nula o vacía;
- recibo asociado a una transferencia diferente.

#### Historial de eventos

Cada transición produce un `ProtocolEvent` inmutable con:

```text
sequence
logicalTime
transferId
action
previousStatus
nextStatus
reason
```

La secuencia inicia en cero con `CREATE_SESSION`. La lista expuesta por `events()` es inmutable y mantiene el orden de ejecución.

`stateVersion()` representa la cantidad de transiciones posteriores a la creación de la sesión.


#### Checkpoint y rollback de sesión

`CrossShardSession.checkpoint()` captura los campos mutables y la lista de eventos. `restore()` recupera ese estado cuando `AtomicCommitProtocol.rollback` revierte un commit interrumpido.

El rollback no crea una transición inversa en `TransitionTable`. Restaura el checkpoint previo porque representa la cancelación de una aplicación incompleta, no una nueva decisión del protocolo.

#### Compatibilidad con la API anterior

Se conservan las firmas:

```java
markCommitted(int approvals, int validators)
markAborted(String reason)
markTimedOut(String reason)
markFailedValidation(String reason)
```

Estas firmas delegan en la máquina de estados y usan el último tiempo lógico conocido. Las sobrecargas con tiempo explícito son utilizadas por `AtomicCommitProtocol`.

El comportamiento externo de `ShardManager` se conserva:

- `commitAtomicTransfer` sigue devolviendo `true` o `false`;
- `abortAtomicTransfer` sigue devolviendo `true` o `false`;
- los estados terminales conservan sus nombres anteriores;
- las métricas agregan todos los estados no terminales como transferencias pendientes.

#### Pruebas agregadas

La Fase 2 incorpora:

```text
CrossShardStateMachineTest
TerminalStateTest
InvalidTransitionTest
```

Las pruebas se ejecutan sin dependencias externas mediante `scripts/run_tests.sh`.

#### Limitaciones conservadas

La Fase 2 no resuelve todavía:

- scheduler de mensajes;
- retraso, duplicación o reordenamiento de red;
- conformidad automática Java-TLA+;
- model checking multisesión.

La Fase 3 ya implementa rollback de cambios parciales. Las responsabilidades restantes pertenecen a las fases 4, 6 y 7.

#### Relación con TLA+ y Alloy

TLA+ y Alloy todavía representan el modelo anterior. Esta fase no modifica las especificaciones para evitar mezclar la implementación Java con la ampliación formal multisesión prevista para la Fase 6.
