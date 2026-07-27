### Mapeo conceptual entre Java y TLA+

#### Propósito

Este documento registra la correspondencia conceptual actual entre la implementación Java y `CrossShardCommit.tla`. No constituye todavía una prueba de refinamiento ni un checker de conformidad.

#### Alcance del mapeo

El mapeo incluye:

- estado de una sesión;
- bloqueo y débito del origen;
- creación y consumo del recibo;
- crédito del destino;
- commit;
- abort;
- timeout;
- liberación de fondos.

#### Correspondencia de estado

| Concepto | Evidencia Java | Evidencia TLA+ actual | Observación |
|---|---|---|---|
| Sesión iniciada | `CrossShardStatus.CREATED` y evento `CREATE_SESSION` | estado posterior a `Init` | TLA+ no tiene identificadores de múltiples sesiones |
| Origen bloqueado o debitado | `SOURCE_LOCKED`, `Shard.lockUtxo` y eliminación posterior del UTXO | `originDebited` | TLA+ combina bloqueo y débito en una variable abstracta |
| Recibo creado | `RECEIPT_CREATED` y acción `CREATE_RECEIPT` | `receiptCreated` | Java conserva identidad, contenido y evento del recibo |
| Recibo entregado y consumido | `RECEIPT_DELIVERED`, `markReceiptConsumed` y `DESTINATION_PREPARED` | `receiptUseCount` | TLA+ no separa entrega, validación y preparación |
| Destino acreditado | adición de UTXO al pool destino | `destinationCredited` | Java conserva monto, receptor e identificador sintético |
| Commit | `CrossShardStatus.COMMITTED` | `committed` | Java usa un enum y TLA+ una variable booleana |
| Abort | `CrossShardStatus.ABORTED` | `aborted` | Java distingue también timeout y fallo de validación |
| Timeout | `CrossShardStatus.TIMED_OUT` y ronda límite | `expired` y acción `TimeoutOrigin` | el reloj y la ronda no están modelados explícitamente en TLA+ |
| Fondos liberados | `unlockUtxo` sin eliminar el UTXO | `fundsReleased` | falta identidad explícita del UTXO en TLA+ |

#### Correspondencia de acciones

| Acción TLA+ actual | Operación Java aproximada | Diferencia principal |
|---|---|---|
| `LockOrigin` | `beginAtomicTransfer`, `LOCK_SOURCE` y `CREATE_RECEIPT` | Java separa los estados, pero conserva una llamada coordinadora |
| `CommitDestination` | `DELIVER_RECEIPT`, `PREPARE_DESTINATION` y `COMMIT_DESTINATION` | Java separa eventos, pero el modelo TLA+ conserva una acción agregada |
| `TimeoutOrigin` | `advanceRound`, `expireTimedOutSessions` y `timeoutSession` | Java usa rondas lógicas y TLA+ solo una transición habilitada |
| `Stutter` | ausencia de acción observable | Java no registra stutter como evento |

#### Abstracciones actuales

El modelo TLA+ omite deliberadamente:

- criptografía;
- estructura completa de UTXO;
- identidad de validadores;
- contenido del recibo;
- monto y cambio;
- múltiples shards;
- múltiples transferencias;
- red de mensajes;
- excepciones de implementación;
- estados intermedios de la sesión.

#### Brechas para conformidad

Para construir conformidad Java-TLA+ se necesitarán:

1. identificadores estables de transferencia y recibo en el modelo;
2. múltiples sesiones representadas mediante funciones;
3. serialización estable de los eventos Java ya implementados;
4. una función de abstracción de estado Java a estado TLA+;
5. un checker que valide cada transición observable;
6. reglas para omitir detalles concretos no modelados;
7. escenarios válidos e inválidos de prueba.

#### Función de abstracción preliminar

La futura función de abstracción podrá adoptar la forma:

```text
abstractState(javaState) -> tlaState
```

Ejemplos preliminares:

```text
session.status == COMMITTED
    -> committed[transfer] = TRUE

receiptId in target.consumedReceipts
    -> receiptUseCount[receipt] = 1

sourceUtxo not in source.lockedUtxos
and session.status == TIMED_OUT
    -> fundsReleased[transfer] = TRUE
```

Estas reglas son preliminares y deberán revisarse después de la extracción del protocolo de la Fase 3 y de la ampliación formal de la Fase 6.

#### Restricción de redacción científica

Hasta completar la Fase 7, la documentación y el paper deben usar expresiones como:

- alineación conceptual;
- correspondencia preliminar;
- mapeo de estados;
- conformidad propuesta.

No deben usar:

- implementación formalmente refinada;
- prueba de refinamiento;
- conformidad demostrada;
- equivalencia Java-TLA+.
