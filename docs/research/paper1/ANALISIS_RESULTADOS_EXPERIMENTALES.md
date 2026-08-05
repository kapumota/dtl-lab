### Análisis de resultados experimentales de Fase 8D

#### Propósito

La Fase 8D transforma la ejecución definitiva de Fase 8C en resultados derivados reproducibles para responder RQ1, RQ2, RQ3 y RQ4.

Los resultados raw son de solo lectura. Ningún script de esta fase modifica `results/experiments/raw/paper1-q3-v1`.

#### Frontera con Fase 8C

La Fase 8C conserva:

- snapshots del plan experimental
- ambiente y procedencia
- salidas por tarea
- métricas de tiempo y memoria
- payload científico de TLC, Alloy o conformidad
- `raw-manifest.json` con hashes por resultado

La Fase 8D no vuelve a ejecutar TLC, Alloy, Java ni el replay de conformidad. Tampoco modifica modelos, configuraciones, seeds, tareas o resultados terminales.

La Fase 8D consume `result.json` como estado operativo y `executor_payload` como resultado científico. Esta separación evita duplicar la lógica de los ejecutores de Fase 8C.

#### Contrato de entrada

La entrada principal es:

```text
results/experiments/raw/paper1-q3-v1
```

Antes del análisis se exige:

- ejecución `definitive`
- estado `complete`
- 1272 tareas en el plan
- 1272 resultados terminales
- hashes válidos para plan, ambiente, procedencia y cada `result.json`
- ausencia de resultados derivados dentro de `raw`

Un error de integridad detiene el análisis antes de crear salidas.

#### Resultados derivados

El dataset consolidado se genera en:

```text
results/experiments/derived/paper1-q3-v1
```

Contiene:

- `task-results.csv`: una fila por tarea terminal
- `measured-results.csv`: repeticiones medidas y conformidad multiseed
- `exclusions.csv`: calentamientos y errores sin métricas científicas
- `statistics.csv`: estadísticas por configuración, caso y métrica
- `analysis-summary.json`: conteos de control
- `rq-findings.json`: evidencia estructurada para RQ1 a RQ4
- `rq-findings.md`: resumen legible para el manuscrito
- `derived-manifest.json`: hashes de entradas y salidas

Los dos calentamientos de cada unidad formal permanecen en `task-results.csv`, pero no ingresan al análisis principal.

Los estados `timeout` y `out_of_memory` se conservan como observaciones censuradas. No se sustituyen por valores inventados.

#### Plan estadístico implementado

Para tiempo, memoria, estados y profundidad se generan:

- cantidad total de observaciones
- cantidad de observaciones numéricas
- cantidad de observaciones censuradas
- mediana
- primer y tercer cuartil
- rango intercuartílico
- mínimo y máximo
- media
- coeficiente de variación
- intervalo bootstrap del 95 por ciento para la mediana

El bootstrap usa diez mil remuestras y una seed determinista derivada de la identidad del grupo. La misma entrada produce los mismos bytes de salida.

Para proporciones se usa el intervalo Wilson del 95 por ciento.

Para RQ4 se calcula Spearman entre el nivel ordinal del perfil y el costo observado dentro de cada herramienta. TLC y Alloy no se comparan mediante una afirmación absoluta de velocidad.

#### Tablas generadas

La fase produce ocho tablas en CSV y Markdown:

1. herramientas, versiones y hardware
2. propiedades y resultados por herramienta
3. mutantes, propiedad objetivo y detección
4. conformidad multiseed
5. costo por perfil de bound
6. fallos habilitados y costo TLC
7. ejecuciones incompletas
8. amenazas a la validez y mitigaciones

Las tablas se almacenan en:

```text
results/experiments/tables/paper1-q3-v1
```

#### Figuras generadas

La fase produce ocho figuras SVG sin dependencias gráficas externas:

1. arquitectura de evidencia
2. flujo del protocolo experimental
3. estados distintos frente a transferencias concurrentes
4. tiempo frente a transferencias concurrentes
5. memoria frente a shards
6. costo relativo por perfil
7. tiempo hasta contraejemplo por mutante
8. aceptación y rechazo multiseed

Las figuras se almacenan en:

```text
results/experiments/figures/paper1-q3-v1
```

Cuando una métrica no está disponible, la figura declara datos insuficientes en lugar de fabricar una observación.

#### Respuestas a las preguntas de investigación

RQ1 informa propiedades evaluadas, resultados completados, violaciones y censura. La conclusión se limita a los bounds completados.

RQ2 calcula el mutation score por mutante y herramienta. La detección exige un contraejemplo asociado con la propiedad objetivo.

RQ3 calcula aceptación de trazas válidas, rechazo de trazas negativas y coincidencia del diagnóstico. La conclusión se denomina conformidad acotada de trazas entre implementación y modelo.

RQ4 reporta medianas por perfil, razones de crecimiento, censura y Spearman. La evidencia es descriptiva y no demuestra una ley asintótica.

#### Ejecución

Para validar la estructura sin usar los resultados definitivos:

```bash
make experiment-analysis-structure
```

El gate construye un fixture sintético temporal, ejecuta el análisis dos veces y compara hashes. También verifica que los contratos de Fase 8A, Fase 8B y Fase 8C no hayan cambiado.

Para analizar la matriz definitiva:

```bash
make experiment-analysis
```

La ejecución valida primero los resultados raw y después genera derivados, tablas y figuras.

#### Integridad y reproducción

`derived-manifest.json` registra:

- hash de `raw-manifest.json`
- hash del plan y su manifiesto
- hash del ambiente y la procedencia
- hash del protocolo experimental
- hash del contrato de análisis
- hash de los scripts de análisis
- hash de cada dataset, tabla y figura

La fase genera todos los artefactos en directorios temporales y solo promueve las salidas cuando el proceso termina correctamente.

#### Gate de cierre

La Fase 8D puede cerrarse cuando:

- el gate estructural pasa
- el análisis definitivo conserva 1272 filas
- se separan 112 calentamientos y 1160 mediciones
- se generan ocho tablas y ocho figuras
- cada salida aparece en `derived-manifest.json`
- `raw` conserva los mismos hashes
- RQ1 a RQ4 tienen evidencia estructurada
- las amenazas a la validez están documentadas
