### Paper 1: commit cross-shard formalmente verificado

#### Estado

- Fase actual: Fase 6.1, cierre científico y documental.
- Fases 0 a 6: cerradas.
- Próxima fase: Fase 7A, exportación determinista de trazas.
- Versión visible: `v1.1.0-beta.2`.
- Verificación: acotada, reproducible y con mutantes científicos.
- Conformidad Java-TLA+: todavía pendiente.

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
- `CIERRE_CIENTIFICO_FASE_6.md`.

#### Reglas de redacción

- comentarios y cadenas de texto en español;
- firmas e identificadores técnicos en inglés;
- títulos con `###`;
- subtítulos con `####`;
- resultados respaldados por ejecuciones reproducibles;
- bounds y limitaciones declarados;
- ausencia de afirmaciones de refinamiento antes de Fase 7.

#### Gate actual

El baseline es defendible para preparar una evaluación Q4. La diferenciación prevista para Q3 depende de cerrar la conformidad acotada Java-TLA+ y la evaluación de la Fase 8.
