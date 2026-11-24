# G3-v2: corrección de comparabilidad y gate de publicación

## Alcance de la corrección

- G3-v1 se considera **evidencia congelada**; no reescribir archivos ni resultados originales.
- `auditar_g3_congelado.py` es un análisis independiente de la campaña ejecutada y conserva las clasificaciones desfavorables y no evaluables.
- G3-v2 es un **protocolo nuevo**, no una repetición idéntica de G3-v1, y no debe integrarse en el corpus histórico V1.
- `ejecutar_g3_v2.py` sí consume `expected_scenario` y solo ejecuta la sonda especializada declarada para el mutante, más el control del mismo escenario.
- `G3ComparadorMismoEscenario.java` produce cinco resultados del **mismo intento Java**: estado terminal, conservación de valor, contrato UTXO, auditoría G2 del journal y salud de instrumentación G1. La quinta **no** constituye un detector semántico.
- Las pruebas G2 dependen del journal G1. No son evidencias independientes. El contrato UTXO también observa snapshots del gestor, por lo que la ausencia de independencia estructural debe declararse.
- `P1CIsolationTest` sigue siendo un testigo especializado de aislamiento. No comparar sus FAIL con los otros cuatro niveles como si fueran una ablación homogénea.

## Defectos no cubiertos

G3-v1 ni este parche añaden un experimento que demuestre recuperación tras caída de proceso, quorum inválido aceptado, replay con doble crédito, atomicidad distribuida en distintos procesos o vivacidad general. No atribuir esas capacidades al artículo.

M05 altera la clave de consumo de un recibo; no equivale a doble aplicación. M08 modifica directamente el resultado de un lock; no debería sostener la novedad principal. M11 depende de interleavings; un marcador alcanzado no prueba interferencia.

## Gate de continuidad

1. Ejecutar **solo** el auditor offline sobre G3-v1 original, verificar SHA-256, estudiar los diez mutantes y la clasificación basada en sus logs.
2. Resolver D, **novedad frente a competidores directos**, antes de autorizar experimentos adicionales.
3. Si existe una hipótesis diferenciable, autorizar explícitamente G3-v2 y registrar un protocolo separado y una justificación *anterior* a esa ejecución. De lo contrario, no ejecutar G3-v2.
4. Para reenvío, reconciliar O1 (V1/V2.2/Java), O2 (14 familias, 112 warmups, 1160 mediciones), O3 (raw original por tarea), y D (novedad).

## Interpretación del resultado

El valor `PASS` de una prueba no demuestra la propiedad global. Un `FAIL` contra un control sano es solo un candidato a detección hasta atribuir la causa específica, demostrar el efecto semántico y descartar fallos del instrumento, compilación, error de entorno y equivalencia del mutante. No producir automáticamente un mutation score global.

Afirmaciones prohibidas sin prueba adicional: refinamiento de toda la implementación Java a TLA+, prueba de atomicidad distribuida, recuperación tras crash, generalización a sistemas externos, independencia plena G1/G2, o victoria metodológica basada en pruebas con escenarios diferentes.

**Decisión editorial actual:** GO para auditar evidencia y corregir documentación; NO GO para reivindicar método nuevo o enviar el artículo hasta cerrar D.
