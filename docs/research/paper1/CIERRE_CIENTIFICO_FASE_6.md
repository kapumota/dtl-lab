### Cierre científico de la Fase 6

#### Objetivo

La Fase 6.1 corrige la coherencia entre preguntas de investigación, contribuciones, propiedades ejecutadas, mutantes y procedencia de resultados antes de iniciar la conformidad Java-TLA+.

#### Exclusión de Fase 7

Esta fase no agrega:

- formatos de traza Java;
- exportadores JSONL;
- función de abstracción ejecutable;
- checker de conformidad;
- replay de trazas mediante TLC;
- corpus de trazas válidas o corruptas.

Esos componentes permanecen asignados a la Fase 7.

#### Propiedades incorporadas

`NoValueLossAtTermination` verifica que una transferencia terminal conserve el valor en el destino después de commit o lo libere en el origen después de abort.

`TerminalStateIrreversibility` verifica que la primera decisión terminal registrada no pueda ser reemplazada por otra decisión.

#### Mutante CommitAfterAbort

El mutante `CommitAfterAbort` pasa a usar `TerminalStateIrreversibility` como propiedad objetivo. `DecisionConsistency` y `NoValueLossAtTermination` pueden fallar también, pero esas violaciones se registran como efectos adicionales.

#### Precisión de mutantes Alloy

Cada ejecución Alloy registra:

- propiedad objetivo;
- propiedades violadas;
- confirmación de que la propiedad objetivo falló;
- propiedades adicionales violadas.

Una ejecución mutante ya no se acepta solamente porque cualquier assertion produzca un contraejemplo.

#### Procedencia

El manifiesto distingue:

- commit fuente;
- commit realmente descargado por el workflow;
- referencia fuente;
- evento de GitHub Actions;
- SHA proporcionado por GitHub;
- rama base y rama de origen.

#### Versión

El cierre científico corresponde a `v1.1.0-beta.2`.

#### Criterio de cierre

1. Las siete propiedades se ejecutan en los modelos válidos.
2. Las configuraciones válidas no producen violaciones.
3. Cada mutante Alloy viola su propiedad objetivo.
4. Cada mutante conserva al menos un contraejemplo.
5. El manifiesto registra diecisiete ejecuciones.
6. La procedencia identifica commit fuente y commit ejecutado.
7. La documentación usa afirmaciones acotadas.
8. No existen componentes de Fase 7 en este cambio.
9. `make validate` termina correctamente.
10. `make formal-research` termina correctamente.
