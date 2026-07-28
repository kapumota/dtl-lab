### Modelo multisesión y mutantes científicos

#### Objetivo

La Fase 6 reemplaza el modelo de una sola transferencia por modelos acotados que representan varias sesiones concurrentes, mensajes, recibos, votos, commit y timeout.

El propósito no es afirmar verificación ilimitada. El objetivo es explorar interleavings dentro de configuraciones y bounds explícitos, y conservar defectos detectados mediante mutantes versionados.

#### Modelo TLA+

`CrossShardCommit.tla` usa funciones indexadas por transferencia para evitar variables globales compartidas entre sesiones.

Variables principales:

- `status`;
- `sourceShard`;
- `targetShard`;
- `locked`;
- `receiptOwner`;
- `receiptUseCount`;
- `destinationCredit`;
- `fundsReleased`;
- `messages`;
- `votes`.

Configuraciones válidas:

- `2shards-1transfer.cfg`;
- `2shards-2transfers.cfg`;
- `3shards-3transfers.cfg`;
- `duplicate-receipt.cfg`;
- `delayed-message.cfg`;
- `timeout-race.cfg`.

#### Modelo Alloy

El modelo Alloy usa estados ordenados mediante:

```alloy
open util/ordering[State]
```

Firmas principales:

- `State`;
- `Transfer`;
- `Receipt`;
- `Shard`;
- `Validator`;
- `Message`.

Cada transición relaciona un estado con su sucesor. Los checks usan bounds declarados en `specs/alloy/scopes/bounds.json`.

#### Propiedades

| Propiedad | Interpretación acotada |
|---|---|
| `NoReceiptReplay` | un recibo no se consume más de una vez |
| `DestinationCreditRequiresValidReceipt` | todo crédito requiere un recibo válido y consumido |
| `DecisionConsistency` | una sesión comprometida no conserva liberación de abort |
| `EventuallyReleasedAfterTimeout` | todo estado abortado observado ya liberó los fondos |
| `QuorumRequired` | todo commit observado reúne el quorum declarado |

La propiedad `EventuallyReleasedAfterTimeout` es una aproximación de estado para esta fase. La vivacidad temporal general requiere fairness y un tratamiento posterior.

#### Catálogo de mutantes

| Mutante | Alteración | Propiedad esperada |
|---|---|---|
| `NoReplayProtection` | permite consumir nuevamente un recibo | `NoReceiptReplay` |
| `CreditBeforeReceipt` | acredita antes de consumir un recibo | `DestinationCreditRequiresValidReceipt` |
| `CommitAfterAbort` | permite commit después de abort | `DecisionConsistency` |
| `TimeoutWithoutRelease` | aborta sin liberar fondos | `EventuallyReleasedAfterTimeout` |
| `QuorumBypass` | permite commit sin quorum | `QuorumRequired` |

Cada mutante existe como archivo TLA+ y Alloy. La matriz de ejecución exige al menos un contraejemplo por herramienta y mutante.

#### Evidencia generada

El perfil científico conserva:

- métricas TLC;
- recibos Alloy;
- matriz de mutantes;
- resúmenes JSON;
- contraejemplos TLC en texto;
- soluciones Alloy en JSON;
- manifiesto de ejecución.

#### Criterio de cierre

La fase se considera cerrada cuando:

1. las seis configuraciones TLA+ válidas no producen violaciones;
2. el modelo Alloy válido no produce contraejemplos;
3. cada mutante TLA+ viola su propiedad objetivo;
4. cada mutante Alloy produce al menos un contraejemplo;
5. los diez contraejemplos quedan almacenados;
6. `make validate` continúa pasando;
7. `make formal-research` termina correctamente;
8. GitHub Actions publica los resultados;
9. la documentación declara que la verificación es acotada.

#### Gate Q4

Después de esta fase existe una base defendible para un artículo Q4 si se agrega una evaluación básica y una redacción honesta sobre bounds, limitaciones y relación con la implementación.

Esta fase no demuestra refinamiento Java-TLA+. Esa correspondencia permanece asignada a la Fase 7.
