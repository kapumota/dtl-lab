### Propiedades de vivacidad

#### Estado actual

La Fase 6.1 no demuestra vivacidad temporal general. El baseline ejecuta propiedades de safety y aproximaciones acotadas de estado.

#### Aproximación usada

`EventuallyReleasedAfterTimeout` conserva su nombre histórico, pero su definición actual expresa:

```text
status[transfer] = "Aborted" implica fundsReleased[transfer] = TRUE
```

Esta fórmula excluye estados abortados con fondos retenidos. No demuestra que un timeout habilitado sea ejecutado eventualmente.

#### Propiedades temporales pendientes

- `EventuallyDecided`;
- `EventuallyReleasedAfterTimeout` con semántica temporal;
- `EventuallyProcessedAfterDelivery`;
- `EventuallyCommittedUnderHealthyConditions`;
- `NoPermanentLock`.

#### Fairness pendiente

Una formulación temporal requeriría, como mínimo:

- weak fairness para timeout;
- weak fairness para procesamiento de recibos entregados;
- progreso del reloj lógico;
- ausencia de fallo permanente del coordinador;
- supuestos explícitos sobre entrega de mensajes.

#### Ubicación en la hoja de ruta

La Fase 7 se concentra en conformidad acotada de trazas y no debe introducir afirmaciones generales de liveness. Fairness e invariantes inductivas permanecen para una Fase 9 opcional o trabajo futuro.

#### Redacción permitida

Se puede afirmar que los estados abortados explorados liberan los fondos dentro del modelo acotado.

No se puede afirmar que toda sesión iniciada termina eventualmente ni que todo mensaje válido será procesado en cualquier ejecución.
