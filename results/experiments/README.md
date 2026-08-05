### Resultados experimentales del Paper 1

#### Propósito

Este directorio separa resultados raw, resultados derivados, tablas y figuras.

#### Estructura

```text
results/experiments/
  raw/
  derived/
  tables/
  figures/
  smoke-v1/
  README.md
```

`raw` conserva salidas directas, ambiente, procedencia, snapshots del plan y resultados por tarea.

`derived` conserva datasets y estadísticas producidos durante la Fase 8D.

`tables` y `figures` conservan artefactos generados mediante scripts versionados.

`smoke-v1` contiene un artefacto temporal de corrección funcional. Sus tiempos no se mezclan con las mediciones definitivas.

#### Regla de integridad

Los resultados raw no se editan manualmente.

Las tablas y figuras no se copian manualmente desde salidas de consola.

Cada resultado derivado registra los hashes de sus entradas raw.

#### Matriz definitiva

La ruta predeterminada es:

```text
results/experiments/raw/paper1-q3-v1
```

La ejecución puede reanudarse sin repetir tareas terminales válidas.

Al completar las 1272 tareas se genera `raw-manifest.json`.

#### Análisis definitivo

Las rutas predeterminadas son:

```text
results/experiments/derived/paper1-q3-v1
results/experiments/tables/paper1-q3-v1
results/experiments/figures/paper1-q3-v1
```

El análisis se ejecuta con:

```bash
make experiment-analysis
```

`derived-manifest.json` identifica los hashes de entrada, scripts y salidas.

#### Estado

La Fase 8C produce resultados raw y manifiestos.

La Fase 8D produce datasets consolidados, estadística, respuestas a RQ1 a RQ4, ocho tablas y ocho figuras.
