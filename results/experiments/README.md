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

`derived` contendrá agregaciones producidas durante la Fase 8D.

`tables` y `figures` contendrán artefactos generados mediante scripts versionados.

`smoke-v1` contiene un artefacto temporal de corrección funcional. Sus tiempos no se mezclan con las mediciones definitivas.

#### Regla de integridad

Los resultados raw no se editan manualmente.

Las tablas y figuras no se copian manualmente desde salidas de consola.

Cada resultado derivado debe registrar los hashes de sus entradas raw.

#### Matriz definitiva

La ruta predeterminada es:

```text
results/experiments/raw/paper1-q3-v1
```

La ejecución puede reanudarse sin repetir tareas terminales válidas.

Al completar las 1272 tareas se genera `raw-manifest.json`.

#### Estado

La Fase 8C produce resultados raw y manifiestos.

La Fase 8D realizará el análisis estadístico y generará tablas y figuras.
