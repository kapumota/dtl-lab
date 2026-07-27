### Arquitectura del protocolo atómico Java

#### Propósito

La Fase 3 separa la lógica del protocolo cross-shard de la administración general de shards. `ShardManager` conserva topología, validadores, reloj lógico, consultas y métricas. `AtomicCommitProtocol` concentra las operaciones que cambian el estado de una transferencia.

#### Componentes

```text
ShardManager
  -> administra shards y validadores
  -> conserva el registro de sesiones
  -> avanza el reloj lógico
  -> captura métricas agregadas
  -> delega operaciones al protocolo

CrossShardProtocol
  -> define begin
  -> define deliverReceipt
  -> define commit
  -> define abort
  -> define timeout

AtomicCommitProtocol
  -> valida precondiciones
  -> crea recibos
  -> construye CommitPlan
  -> aplica mutaciones
  -> ejecuta rollback

ProtocolContext
  -> entrega acceso controlado a shards y sesiones
  -> entrega quorum y tiempo lógico
  -> permite inyección reproducible de fallos
```

#### Separación del commit

El commit se divide en cuatro etapas:

```text
prepareCommit
validate
applyCommit
rollback
```

`prepareCommit` calcula las mutaciones antes de tocar el ledger. El resultado es un `CommitPlan` que contiene:

- sesión;
- shard origen;
- shard destino;
- UTXO origen;
- UTXO de cambio opcional;
- UTXO destino;
- aprobaciones del destino;
- `LedgerSnapshot` previo.

#### Snapshot restaurable

`LedgerSnapshot` conserva:

- presencia y salida del UTXO origen;
- estado del bloqueo del origen;
- estado de consumo del recibo;
- presencia previa del UTXO de cambio;
- presencia previa del UTXO destino;
- checkpoint completo de `CrossShardSession`.

El checkpoint de sesión incluye estado, razón, aprobaciones, tiempo lógico, secuencia y eventos. El rollback restaura exactamente ese checkpoint y no agrega una decisión terminal falsa.

#### Orden de aplicación

La aplicación normal sigue este orden:

1. registrar `DESTINATION_PREPARED`;
2. consumir el recibo;
3. retirar el UTXO origen;
4. liberar el bloqueo;
5. crear el cambio cuando corresponda;
6. crear el UTXO destino;
7. registrar `COMMITTED`.

Si una operación lanza una excepción, el protocolo ejecuta `rollback` antes de propagar `ProtocolException`.

#### Orden de rollback

El rollback restaura:

1. UTXO destino;
2. UTXO de cambio;
3. UTXO origen;
4. estado de consumo del recibo;
5. bloqueo del origen;
6. checkpoint de sesión.

La restauración usa el snapshot previo y no presupone que todas las mutaciones hayan llegado a ejecutarse.

#### Puntos de fallo reproducibles

`ProtocolContext.FailurePoint` permite probar:

```text
AFTER_RECEIPT_CONSUMED
AFTER_SOURCE_DEBIT
DURING_TARGET_CREDIT
AFTER_TARGET_CREDIT
```

Estos puntos solo se activan mediante un inyector explícito. La configuración normal utiliza un inyector vacío.

#### Compatibilidad

`ShardManager` conserva las firmas anteriores:

```java
beginAtomicTransfer(CrossShardTransfer transfer, int timeoutRounds)
commitAtomicTransfer(String transferId)
abortAtomicTransfer(String transferId, String reason)
lockAndCreateReceipt(CrossShardTransfer transfer)
commitReceipt(Receipt receipt)
```

También expone `getProtocol()` y `deliverAtomicReceipt()` para usar la API separada sin romper consumidores anteriores.

#### Límites de la Fase 3

La Fase 3 demuestra rollback mediante pruebas ejecutables y puntos de fallo controlados. Todavía no demuestra atomicidad para todos los interleavings concurrentes ni reemplaza el modelo formal. Permanecen pendientes:

- red de mensajes;
- concurrencia multisesión;
- scheduler determinista;
- model checking ampliado;
- conformidad automática Java-TLA+.
