### Paper 1: commit cross-shard formalmente verificado

#### Estado

- Fase actual: Fase 7D, corpus negativo de trazas.
- Fases 0 a 7D: cerradas.
- Próxima fase: Fase 7E, integración de conformidad.
- Versión visible: `v1.1.0-beta.2`.
- Verificación: acotada, reproducible y con mutantes científicos.
- Trazas Java: JSONL versionado, determinista y reproducible.
- Abstracción Java-TLA+: ejecutable y tipada.
- Replay TLC: ejecutable sobre el catálogo válido.
- Corpus negativo: diez mutaciones tipadas con rechazo esperado.
- Conformidad Java-TLA+: acotada a escenarios, seeds y mutaciones declaradas.

#### Objetivo

El Paper 1 estudia un protocolo de commit cross-shard y la relación entre implementación Java, invariantes runtime, TLA+, Alloy, escenarios adversariales y conformidad acotada basada en trazas.

#### Documentos principales

- `PREGUNTAS_DE_INVESTIGACION.md`;
- `CONTRIBUCIONES.md`;
- `ALCANCE.md`;
- `SUPUESTOS.md`;
- `MODELO_DE_AMENAZAS.md`;
- `HOJA_DE_RUTA.md`;
- `MATRIZ_DE_TRAZABILIDAD.md`;
- `PROTOCOLO.md`;
- `MAQUINA_DE_ESTADOS.md`;
- `PROPIEDADES_DE_SEGURIDAD.md`;
- `PROPIEDADES_DE_VIVACIDAD.md`;
- `MODELO_DE_FALLOS.md`;
- `MAPEO_JAVA_TLA.md`;
- `ARQUITECTURA_PROTOCOLO_ATOMICO.md`;
- `SIMULACION_DETERMINISTA.md`;
- `MODEL_CHECKING_EJECUTABLE.md`;
- `MODELO_MULTISESION_MUTANTES.md`;
- `CIERRE_CIENTIFICO_FASE_6.md`;
- `FORMATO_DE_TRAZAS.md`;
- `FUNCION_DE_ABSTRACCION.md`;
- `REPLAY_TLC.md`;
- `CORPUS_NEGATIVO_TRAZAS.md`.

#### Reglas de redacción

- comentarios y cadenas de texto en español;
- firmas e identificadores técnicos en inglés;
- títulos con `###`;
- subtítulos con `####`;
- resultados respaldados por ejecuciones reproducibles;
- bounds y limitaciones declarados;
- ausencia de afirmaciones de refinamiento antes de cerrar la Fase 7.

#### Gate actual

La Fase 7D demuestra que TLC rechaza las diez mutaciones declaradas y conserva su procedencia. La Fase 7E debe integrar catálogo válido, corpus negativo, CI y resultados científicos.
