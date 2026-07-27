### Sharding avanzado en DLT-Lab

Se convierte el sharding basico en un laboratorio de transacciones cross-shard con estados, quorum, validadores, timeouts y fallos.

#### Componentes

```text
Shard
  - UTXO pool local
  - UTXOs bloqueados
  - recibos consumidos
  - validadores asociados

ShardValidator
  - online/offline
  - honesto/malicioso
  - voto educativo para bloquear o confirmar

CrossShardSession
  - CREATED
  - SOURCE_LOCKED
  - RECEIPT_CREATED
  - RECEIPT_DELIVERED
  - DESTINATION_PREPARED
  - COMMITTED
  - TIMED_OUT
  - ABORTED
  - FAILED_VALIDATION
```

#### Flujo atomico educativo

```text
1. beginAtomicTransfer()
   El shard origen valida que el UTXO existe, que el monto es valido y que hay quorum.
   Luego bloquea el UTXO, crea el recibo y registra ambas transiciones.

2. commitAtomicTransfer()
   ShardManager delega el commit a AtomicCommitProtocol.
   El protocolo entrega el recibo, prepara un CommitPlan y captura un LedgerSnapshot.
   Si confirma, aplica debito, cambio, credito y decision terminal.
   Si ocurre una excepcion, rollback restaura el estado previo.

3. advanceRound()
   Avanza la ronda logica y expira transferencias no terminales que pasaron su timeout.

4. abortAtomicTransfer()
   Permite abortar manualmente una sesion no terminal y liberar el UTXO origen.
```

#### Maquina de estados

Las transiciones principales son:

```text
CREATED
  -> SOURCE_LOCKED
  -> RECEIPT_CREATED
  -> RECEIPT_DELIVERED
  -> DESTINATION_PREPARED
  -> COMMITTED
```

Los estados `COMMITTED`, `TIMED_OUT`, `ABORTED` y `FAILED_VALIDATION` son terminales e irreversibles.

Cada sesión conserva eventos con tiempo lógico, acción, estado anterior, estado siguiente y razón.

#### Casos incluidos en la demo

```text
COMMITTED          Transferencia exitosa de shard 0 a shard 1.
TIMED_OUT          Transferencia que no llega a confirmarse antes del timeout.
FAILED_VALIDATION  Transferencia cuyo shard destino no alcanza quorum.
```

#### Invariantes relacionadas

```text
- No se aceptan recibos duplicados.
- Una sesion terminal no debe dejar UTXOs bloqueados.
- El timeout libera el UTXO origen.
- El commit consume el UTXO origen y crea valor en destino.
- Un estado terminal no puede cambiar a otro estado.
```

#### Reportes generados

```text
reports/sharding_rounds.csv  Metricas por ronda logica.
reports/shards.txt           Estado ASCII de shards y sesiones.
reports/shards.dot           Grafo DOT de shards y transferencias.
```

#### Arquitectura del protocolo

```text
ShardManager
  -> shards, validadores, sesiones, reloj y metricas

AtomicCommitProtocol
  -> begin, deliverReceipt, commit, abort y timeout
  -> prepareCommit, applyCommit y rollback

LedgerSnapshot
  -> UTXOs, bloqueo, recibo y checkpoint de sesion
```

#### Limitacion intencional

Este no es un protocolo cross-shard productivo. Es una maqueta para visualizar atomicidad, bloqueo, recibos, quorum, fallos y rollback. La Fase 3 no implementa red de mensajes, concurrencia multisesion ni conformidad automática con TLA+ y Alloy.
