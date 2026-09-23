### Paper 1: evaluación reproducible de un protocolo de commit cross-shard

#### Estado

- Fase actual: 8G-F2, definición del alcance público del artifact.
- Fases científicas 8A a 8E y manuscrito 8F-A a 8F-J: cerrados.
- Submission 8G-A a 8G-E: cerrado técnicamente; metadata administrativa y artifact final pendientes.
- Scientific manuscript freeze: `06bea7de70971d5b22d705a2df19137122758c08`.
- `main` auditado para esta fase: `b68aeef7d212fd82c2466180134a51ed460473ee` (PR #29).
- Baseline experimental: `45cb114d61b1df8c605c50700f3cc72d48d157fe`.
- Fuente de reproducción independiente: `6cd88c377afd23fee4998882f91142d71e7d963e`.
- Artifact-release revision y submission revision: pendientes; no hay DOI ni URL persistente confirmados.
- Target actual: Science of Computer Programming (SCP), `Research Papers`, línea principal `Formal techniques`.
- Ruta económica prevista: suscripción, sin elegir voluntariamente open access con APC.
- Estado de envío: `NOT_READY_TO_CLICK_SUBMIT`.
- Versión visible histórica al cierre de 8B: `v1.1.0-rc.1`.
- Verificación: acotada, reproducible y con mutantes científicos.
- Trazas Java: JSONL versionado, determinista y reproducible.
- Abstracción Java-TLA+: ejecutable y tipada.
- Replay TLC: ejecutable sobre el catálogo válido.
- Corpus negativo: diez mutaciones tipadas con rechazo esperado.
- Conformidad Java-TLA+: acotada a escenarios, seeds y mutaciones declaradas.
- Campaña definitiva: 1272 tareas programadas, 1160 medidas y 112 warmups; 1188 completadas y 84 timeout.
- Reproducción independiente: 10/10 gates y 32/32 hashes de artefactos analíticos, en el mismo host Linux nativo, desde otro usuario, clon y workspace; no fue una repetición completa de la campaña.

#### Objetivo

El Paper 1 estudia un protocolo de commit cross-shard y la relación entre implementación Java, invariantes runtime, TLA+, Alloy, escenarios adversariales y conformidad acotada basada en trazas.

#### Documentos principales

- `PREGUNTAS_DE_INVESTIGACION.md`
- `CONTRIBUCIONES.md`
- `ALCANCE.md`
- `SUPUESTOS.md`
- `MODELO_DE_AMENAZAS.md`
- `HOJA_DE_RUTA.md`
- `MATRIZ_DE_TRAZABILIDAD.md`
- `PROTOCOLO.md`
- `MAQUINA_DE_ESTADOS.md`
- `PROPIEDADES_DE_SEGURIDAD.md`
- `PROPIEDADES_DE_VIVACIDAD.md`
- `MODELO_DE_FALLOS.md`
- `MAPEO_JAVA_TLA.md`
- `ARQUITECTURA_PROTOCOLO_ATOMICO.md`
- `SIMULACION_DETERMINISTA.md`
- `MODEL_CHECKING_EJECUTABLE.md`
- `MODELO_MULTISESION_MUTANTES.md`
- `CIERRE_CIENTIFICO_FASE_6.md`
- `FORMATO_DE_TRAZAS.md`
- `FUNCION_DE_ABSTRACCION.md`
- `REPLAY_TLC.md`
- `CORPUS_NEGATIVO_TRAZAS.md`
- `CIERRE_CONFORMIDAD_FASE_7.md`
- `PROTOCOLO_EXPERIMENTAL_Q3.md`
- `INFRAESTRUCTURA_EXPERIMENTAL.md`
- `EJECUCION_MATRIZ_EXPERIMENTAL.md`

#### Reglas de redacción

- comentarios y cadenas de texto en español
- firmas e identificadores técnicos en inglés
- títulos con `###`
- subtítulos con `####`
- resultados respaldados por ejecuciones reproducibles
- bounds y limitaciones declarados
- ausencia de afirmaciones de refinamiento general

#### Gate actual

8G-F2 define el alcance público del artifact científico del Paper 1. El contrato está en `manuscript/paper1/artifact/PUBLIC_ARTIFACT_SCOPE.md`. Se distinguen componentes `INCLUDE`, `CONDITIONAL`, `PENDING_F4` y `EXCLUDE`. El software general de DLT-Lab no se presenta como evidencia científica, raw y derived permanecen separados, el bundle histórico 8E no se confunde con el artifact final y el material editorial interno queda fuera del artifact público.
