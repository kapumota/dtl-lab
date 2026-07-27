### Matriz de trazabilidad inicial

#### Propósito

Esta matriz relaciona las propiedades científicas esperadas con el baseline de código, las especificaciones formales y las brechas que deben cerrarse.

#### Matriz

| Propiedad o elemento | Evidencia Java actual | Evidencia formal actual | Brecha identificada | Fase prevista |
|---|---|---|---|---|
| Estado de sesión | `CrossShardSession` y `CrossShardStatus` | Variables booleanas y estado abstracto | Solo existe un estado agregado `PENDING` antes de los estados terminales | Fase 2 |
| Bloqueo de origen | `ShardManager.beginAtomicTransfer` | `LockOrigin` en TLA+ | Falta modelar varias sesiones y conflictos concurrentes | Fases 4 y 6 |
| Consumo de recibo | `ShardManager.commitReceipt` y `commitAtomicTransfer` | `receiptUseCount` y `NoReceiptReplay` | Falta estudiar duplicación, entrega y consumo entre múltiples shards | Fases 4 y 6 |
| Protección runtime contra replay | `CrossShardReplayAttack` y `NoReceiptReplayInvariant` | `NoReceiptReplay` en TLA+ y Alloy | La comprobación runtime debe revisarse para detectar duplicación global y trazas adversariales | Fases 2 y 4 |
| Conservación de valor | `finalizeSourceDebit`, timeout y desbloqueo del origen | `NoValueLoss` y `TimeoutReleasesFunds` | Falta comprobar excepciones intermedias, rollback y estados parciales | Fase 3 |
| Decisión atómica | estados terminales y control `isTerminal` | `AtomicCommit` | Falta una tabla explícita de transiciones e irreversibilidad terminal | Fases 1 y 2 |
| Timeout | `advanceRound`, `expireTimedOutSessions` y `CrossShardTimeoutAttack` | `TimeoutOrigin` y `TimeoutReleasesFunds` | La propiedad formal actual es una invariante de estado, no una propiedad temporal completa | Fases 1 y 6 |
| Quorum | validación en origen y destino dentro de `ShardManager` | No existe una propiedad formal específica | Falta `QuorumRequired` en TLA+ y Alloy | Fase 6 |
| Fallos de red | disponibilidad de validadores y shard offline | No existe una red explícita de mensajes | Faltan retraso, pérdida, duplicación y reordenamiento | Fases 4 y 6 |
| Model checking ejecutado | ejecución opcional de TLC en `run_formal_checks.sh` | Modelos presentes | El perfil actual puede finalizar sin TLC y no automatiza Alloy | Fase 5 |
| Conformidad Java-TLA+ | alineación conceptual documentada | modelo abstracto | No existe formato de trazas, función de abstracción ni checker | Fase 7 |
| Reproducibilidad científica | scripts y validación general | configuración TLA+ básica | Faltan manifiestos, versiones fijadas y resultados estructurados | Fases 5 y 8 |

#### Observaciones sobre el baseline

- `ShardManager` coordina actualmente la mayor parte del protocolo.
- `CrossShardSession` conserva estado final y aprobaciones, pero no un historial completo de eventos.
- `NoStuckCrossShardInvariant` comprueba que las sesiones terminales no dejen el UTXO origen bloqueado.
- `NoReceiptReplayInvariant` ofrece una comprobación runtime inicial que debe fortalecerse para la evaluación científica.
- TLA+ y Alloy contienen cinco propiedades iniciales, pero requieren ejecución obligatoria y ampliación multisesión.
- La documentación actual reconoce que el modelo formal no demuestra la corrección completa de la implementación Java.

#### Regla de actualización

Cada Pull Request del Paper 1 deberá actualizar esta matriz cuando:

- agregue o cambie una propiedad;
- cambie una transición Java;
- modifique TLA+ o Alloy;
- agregue un ataque o mutante;
- agregue resultados o herramientas de verificación.
