### Mapeo conceptual entre Java y TLA+

#### Propósito

Este documento define el contrato conceptual que deberá usar la Fase 7 para construir la función de abstracción. Todavía no constituye un checker de conformidad ni una prueba de refinamiento.

#### Correspondencia de estados

| Estado Java | Estado TLA+ | Observación |
|---|---|---|
| `CREATED` | `status[t] = "Pending"` | sesión creada sin bloqueo |
| `SOURCE_LOCKED` | `status[t] = "Locked"` | origen bloqueado |
| `RECEIPT_CREATED` | `status[t] = "Locked"` | detalle concreto incluido en `LockTransfer` |
| `RECEIPT_DELIVERED` | `status[t] = "Locked"` | mensaje disponible antes del consumo |
| `DESTINATION_PREPARED` | `status[t] = "Prepared"` | recibo consumido y destino preparado |
| `COMMITTED` | `status[t] = "Committed"` | decisión terminal de commit |
| `ABORTED` | `status[t] = "Aborted"` | decisión terminal de abort |
| `TIMED_OUT` | `status[t] = "Aborted"` | la causa se conserva en la acción `TimeoutTransfer` |
| `FAILED_VALIDATION` | `status[t] = "Aborted"` | abstracción de una decisión no commit |

#### Correspondencia de variables

| Evidencia Java | Variable TLA+ |
|---|---|
| identificador de sesión | índice `t` de `Transfers` |
| shard origen | `sourceShard[t]` |
| shard destino | `targetShard[t]` |
| UTXO origen bloqueado | `locked[t]` |
| propiedad del recibo | `receiptOwner[t]` |
| recibo consumido | `receiptUseCount[t]` |
| crédito destino observable | `destinationCredit[t]` |
| fondos devueltos al origen | `fundsReleased[t]` |
| mensajes de red | `messages` |
| validadores que votaron | `votes[t]` |
| primera decisión terminal | `terminalStatus[t]` |

#### Correspondencia de acciones

| Evento o grupo Java | Acción TLA+ |
|---|---|
| crear sesión | `Init` o detalle previo no observable |
| bloquear origen y crear recibo | `LockTransfer` |
| hacer disponible un recibo retrasado | `ReleaseDelayedReceipt` |
| validar y consumir recibo | `ConsumeReceipt` |
| registrar voto | `CastVote` |
| completar commit | `CommitTransfer` |
| expirar y liberar origen | `TimeoutTransfer` |
| evento concreto sin cambio abstracto | `Stutter` |

#### Reglas de abstracción

La futura función deberá:

1. conservar `transferId` como índice estable;
2. proyectar varios estados Java al mismo estado abstracto cuando TLA+ omite detalles;
3. representar eventos omitidos mediante `Stutter` o agrupación documentada;
4. conservar orden lógico, seed, escenario y shards;
5. separar acción observada de estado abstracto resultante;
6. rechazar cambios de identidad o topología dentro de una sesión.

#### Detalles omitidos por TLA+

- criptografía;
- estructura completa del UTXO;
- monto y cambio;
- contenido completo del recibo;
- snapshot y rollback interno;
- excepciones concretas;
- nombres de clases Java.

La omisión debe ser explícita y no puede usarse para aceptar una transición abstracta inválida.

#### Restricción de redacción

Hasta cerrar la Fase 7 se usarán expresiones como `mapeo conceptual`, `función de abstracción propuesta` y `conformidad acotada pendiente`.

No se usarán `refinamiento demostrado`, `equivalencia Java-TLA+` ni `verificación completa de la implementación`.
