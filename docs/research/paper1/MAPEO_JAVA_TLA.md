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
| Sesión iniciada | entrada en `sessions` con estado `PENDING` | estado posterior a `Init` y `LockOrigin` | TLA+ no tiene identificadores de múltiples sesiones |
| Origen bloqueado o debitado | `Shard.lockUtxo` y eliminación posterior del UTXO | `originDebited` | TLA+ combina bloqueo y débito en una variable abstracta |
| Recibo creado | `createReceipt` | `receiptCreated` | Java conserva identidad y contenido del recibo |
| Recibo consumido | `markReceiptConsumed` | `receiptUseCount` | TLA+ modela un contador acotado para una sola sesión |
| Destino acreditado | adición de UTXO al pool destino | `destinationCredited` | Java conserva monto, receptor e identificador sintético |
| Commit | `CrossShardStatus.COMMITTED` | `committed` | Java usa un enum y TLA+ una variable booleana |
| Abort | `CrossShardStatus.ABORTED` | `aborted` | Java distingue también timeout y fallo de validación |
| Timeout | `CrossShardStatus.TIMED_OUT` y ronda límite | `expired` y acción `TimeoutOrigin` | el reloj y la ronda no están modelados explícitamente en TLA+ |
| Fondos liberados | `unlockUtxo` sin eliminar el UTXO | `fundsReleased` | falta identidad explícita del UTXO en TLA+ |

#### Correspondencia de acciones

| Acción TLA+ actual | Operación Java aproximada | Diferencia principal |
|---|---|---|
| `LockOrigin` | `beginAtomicTransfer` | Java valida transferencia, quorum, bloqueo, recibo y sesión en una misma operación |
| `CommitDestination` | `commitAtomicTransfer` | Java ejecuta quorum, consumo, débito, crédito, cambio y estado final |
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
3. una máquina de estados explícita en Java;
4. un formato de traza por acción;
5. una función de abstracción de estado Java a estado TLA+;
6. un checker que valide cada transición observable;
7. reglas para omitir detalles concretos no modelados;
8. escenarios válidos e inválidos de prueba.

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

Estas reglas son preliminares y deberán revisarse después del refactor de la Fase 2 y la extracción del protocolo de la Fase 3.

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
