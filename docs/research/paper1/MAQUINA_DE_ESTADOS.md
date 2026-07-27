### Máquina de estados del protocolo cross-shard

#### Propósito

Este documento separa la máquina de estados observada en el baseline de la máquina de estados objetivo del Paper 1. Esta distinción evita presentar como implementados estados que todavía no existen en Java.

#### Máquina de estados del baseline

`CrossShardStatus` contiene actualmente:

```text
PENDING
COMMITTED
ABORTED
TIMED_OUT
FAILED_VALIDATION
```

Las transiciones observables son:

```text
PENDING -> COMMITTED
PENDING -> ABORTED
PENDING -> TIMED_OUT
PENDING -> FAILED_VALIDATION
```

Los cuatro estados de salida son terminales.

#### Estado inicial del baseline

Una sesión se crea como `PENDING` después de construir el recibo. Cuando el shard origen no alcanza quorum, también se crea una sesión y se cambia inmediatamente a `FAILED_VALIDATION`.

#### Transiciones actuales

| Acción Java | Precondición principal | Estado inicial | Estado final |
|---|---|---|---|
| `beginAtomicTransfer` | transferencia válida y quorum en origen | no existe sesión | `PENDING` |
| `commitAtomicTransfer` | sesión pendiente, no vencida, quorum en destino y recibo no consumido | `PENDING` | `COMMITTED` |
| `abortAtomicTransfer` | sesión existente y no terminal | `PENDING` | `ABORTED` |
| `timeoutSession` | sesión existente, no terminal y timeout superado | `PENDING` | `TIMED_OUT` |
| `markFailedValidation` | fallo de quorum o recibo ya consumido | `PENDING` o creación inicial | `FAILED_VALIDATION` |

#### Estados terminales

Una sesión es terminal cuando su estado es:

- `COMMITTED`;
- `ABORTED`;
- `TIMED_OUT`;
- `FAILED_VALIDATION`.

Los métodos de commit y abort rechazan una sesión terminal mediante `isTerminal`.

#### Transiciones prohibidas en el contrato

Aunque el baseline no usa una tabla explícita de transiciones, el contrato del Paper 1 prohíbe:

```text
COMMITTED -> ABORTED
COMMITTED -> TIMED_OUT
ABORTED -> COMMITTED
ABORTED -> TIMED_OUT
TIMED_OUT -> COMMITTED
TIMED_OUT -> ABORTED
FAILED_VALIDATION -> COMMITTED
FAILED_VALIDATION -> ABORTED
```

También se prohíben:

- doble commit;
- doble abort;
- timeout repetido;
- commit de una sesión inexistente;
- commit después del vencimiento;
- consumo repetido del mismo recibo.

#### Máquina de estados objetivo

La Fase 2 podrá ampliar el estado agregado `PENDING` a estados intermedios explícitos:

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

Esta ampliación todavía no está implementada. Su finalidad será hacer observables las transiciones necesarias para trazas, model checking y conformidad Java-TLA+.

#### Tabla objetivo preliminar

| Estado actual | Acción | Estado siguiente | Condición |
|---|---|---|---|
| `CREATED` | `lockSource` | `SOURCE_LOCKED` | UTXO válido, libre y quorum en origen |
| `SOURCE_LOCKED` | `createReceipt` | `RECEIPT_CREATED` | bloqueo confirmado |
| `RECEIPT_CREATED` | `deliverReceipt` | `RECEIPT_DELIVERED` | mensaje entregado antes del timeout |
| `RECEIPT_DELIVERED` | `prepareDestination` | `DESTINATION_PREPARED` | recibo válido y quorum en destino |
| `DESTINATION_PREPARED` | `commitDestination` | `COMMITTED` | débito y crédito aplicados atómicamente |
| cualquier estado no terminal permitido | `abortTransfer` | `ABORTED` | condición de abort válida |
| cualquier estado no terminal bloqueado | `expireTransfer` | `TIMED_OUT` | timeout alcanzado y fondos liberados |
| cualquier estado no terminal permitido | `failValidation` | `FAILED_VALIDATION` | validación no recuperable |

#### Invariantes de transición

La futura máquina de estados deberá cumplir:

1. una sesión tiene exactamente un estado;
2. una sesión terminal no cambia de estado;
3. `COMMITTED` requiere crédito en destino y débito definitivo en origen;
4. `ABORTED` y `TIMED_OUT` requieren que el UTXO origen no permanezca bloqueado;
5. `FAILED_VALIDATION` no puede dejar un crédito en destino;
6. un recibo consumido no puede volver a habilitar una transición de commit;
7. toda transición debe registrar acción, instante lógico, estado anterior y estado posterior.

#### Relación con TLA+ y Alloy

El modelo TLA+ actual representa el protocolo mediante variables booleanas y tres acciones principales. El modelo Alloy actual representa atributos de una transferencia, pero no una secuencia ordenada de estados.

La Fase 6 deberá adaptar ambos modelos a la máquina de estados objetivo sin cambiar las garantías definidas aquí.
