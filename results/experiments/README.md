### Resultados experimentales del Paper 1

#### Propósito

Este directorio reserva la separación entre resultados raw y resultados derivados.

La Fase 8B no versiona resultados experimentales definitivos.

#### Estructura prevista

```text
results/experiments/
  raw/
  derived/
  tables/
  figures/
  README.md
```

`raw` conservará salidas directas, ambiente, procedencia, snapshots del plan y resultados por tarea.

`derived` contendrá agregaciones producidas desde raw durante la Fase 8D.

`tables` y `figures` contendrán artefactos generados mediante scripts versionados.

#### Regla de integridad

Los resultados raw no se editan manualmente.

Las tablas y figuras no se copian manualmente desde salidas de consola.

Cada resultado derivado debe registrar los hashes de sus entradas raw.

#### Estado

La infraestructura se valida con directorios temporales.

La matriz definitiva se ejecutará en la Fase 8C.
