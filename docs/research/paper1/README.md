### Paper 1: commit cross-shard formalmente verificado

#### Estado

Este directorio contiene la documentación de investigación del Paper 1 de DLT-Lab.

- Fase actual: Fase 0, baseline de investigación.
- Estado: definición inicial.
- Baseline del repositorio: commit `34f4c088b9f5db3e3b54824de69db8589fd06de3`.
- Versión visible del software: `v1.0.1`.
- Rama de trabajo: `paper1/fase-0-baseline-investigacion`.

La Fase 0 no cambia el comportamiento del software. Su propósito es fijar el problema científico, el alcance, los supuestos, el modelo de amenazas y la trazabilidad entre el código Java y las especificaciones formales existentes.

#### Objetivo del Paper 1

El Paper 1 estudiará un protocolo de commit cross-shard con bloqueo del UTXO origen, creación y consumo de recibos, quorum de validadores, commit, abort y recuperación por timeout.

La contribución esperada no es presentar DLT-Lab completo como un nuevo simulador blockchain. El objeto de estudio es el protocolo cross-shard y la relación entre:

- implementación Java;
- invariantes runtime;
- especificación TLA+;
- modelo Alloy;
- escenarios adversariales reproducibles;
- conformidad acotada entre trazas Java y acciones formales.

#### Documentos de la Fase 0

- `PREGUNTAS_DE_INVESTIGACION.md`: preguntas de investigación y resultados observables.
- `CONTRIBUCIONES.md`: contribuciones esperadas y condiciones para reclamarlas.
- `ALCANCE.md`: componentes incluidos y excluidos.
- `SUPUESTOS.md`: supuestos del protocolo y del entorno de evaluación.
- `MODELO_DE_AMENAZAS.md`: activos, capacidades del adversario y fallos estudiados.
- `HOJA_DE_RUTA.md`: fases técnicas hasta el artefacto reproducible.
- `MATRIZ_DE_TRAZABILIDAD.md`: relación entre propiedades, código, modelos y pruebas.
- `GLOSARIO_BILINGUE.md`: terminología española e inglesa utilizada en el paper.

#### Relación con la documentación existente

La documentación de este directorio no reemplaza:

- `docs/architecture.md`;
- `docs/formal-verification.md`;
- `specs/tla/README.md`;
- `README.md`.

Esos archivos describen el software y la Fase 4 existente. Este directorio define la conversión del componente cross-shard en un artefacto de investigación.

#### Reglas de trabajo

- Los comentarios y cadenas de texto del código se mantienen en español.
- Las firmas de funciones y los identificadores técnicos nuevos se escriben en inglés.
- Las propiedades formales conservan sus nombres canónicos en inglés.
- Los títulos de la documentación usan `###`.
- Los subtítulos de la documentación usan `####`.
- No se introducen resultados que no hayan sido generados por herramientas o experimentos reproducibles.
- No se afirma verificación completa cuando el resultado corresponde a un espacio de estados acotado.

#### Criterios de cierre de la Fase 0

La Fase 0 se considera completa cuando:

1. Las preguntas de investigación están definidas.
2. El alcance evita mezclar el Paper 1 con el trabajo futuro sobre MEV y benchmarking.
3. Los supuestos y el modelo de amenazas son explícitos.
4. Las contribuciones esperadas se distinguen de los resultados ya obtenidos.
5. La matriz de trazabilidad identifica las brechas entre Java, TLA+, Alloy y pruebas runtime.
6. No se modifica código de producción ni comportamiento existente.
7. `make validate` continúa pasando después de aplicar el parche.
