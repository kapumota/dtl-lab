# Sharding avanzado en DLT-Lab

La fase 5 convierte el sharding basico en un laboratorio de transacciones cross-shard con estados, quorum, validadores, timeouts y fallos.

## Componentes

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
  - PENDING
  - COMMITTED
  - TIMED_OUT
  - ABORTED
  - FAILED_VALIDATION
```

## Flujo atomico educativo

```text
1. beginAtomicTransfer()
   El shard origen valida que el UTXO existe, que el monto es valido y que hay quorum.
   Luego bloquea el UTXO y crea un recibo.

2. commitAtomicTransfer()
   El shard destino valida el recibo y confirma si alcanza quorum.
   Si confirma, el UTXO origen se consume y el destino recibe un nuevo UTXO.

3. advanceRound()
   Avanza la ronda logica y expira transferencias pendientes que pasaron su timeout.

4. abortAtomicTransfer()
   Permite abortar manualmente una sesion pendiente y liberar el UTXO origen.
```

## Casos incluidos en la demo

```text
COMMITTED          Transferencia exitosa de shard 0 a shard 1.
TIMED_OUT          Transferencia que no llega a confirmarse antes del timeout.
FAILED_VALIDATION  Transferencia cuyo shard destino no alcanza quorum.
```

## Invariantes relacionadas

```text
- No se aceptan recibos duplicados.
- Una sesion terminal no debe dejar UTXOs bloqueados.
- El timeout libera el UTXO origen.
- El commit consume el UTXO origen y crea valor en destino.
```

## Reportes generados

```text
reports/sharding_rounds.csv  Metricas por ronda logica.
reports/shards.txt           Estado ASCII de shards y sesiones.
reports/shards.dot           Grafo DOT de shards y transferencias.
```

## Limitacion intencional

Este no es un protocolo cross-shard productivo. Es una maqueta pedagogica para visualizar atomicidad, bloqueo, recibos, quorum y fallos. Una siguiente fase podria modelar two-phase commit formal, validadores Byzantine, pruebas criptograficas de inclusion y especificaciones TLA+/Alloy.
