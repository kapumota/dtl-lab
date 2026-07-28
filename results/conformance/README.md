### Resultados de replay y conformidad

#### Propósito

Este directorio recibe los módulos generados, salidas de TLC y manifiestos de las Fases 7C y 7D.

#### Generación

```bash
make conformance-replay
make conformance-negative
```

Las salidas predeterminadas son:

```text
results/conformance/replay-v1/
results/conformance/negative-v1/
```

#### Contenido generado

- un directorio por escenario;
- módulo y configuración de replay;
- copia de la especificación base;
- salidas estándar y de error de TLC;
- metadatos de model checking;
- `manifest.csv` con el resultado de cada escenario o mutación.

#### Política de versionado

Los resultados generados no se versionan. Solo este README permanece en Git.

La metodología se documenta en:

```text
docs/research/paper1/REPLAY_TLC.md
docs/research/paper1/CORPUS_NEGATIVO_TRAZAS.md
```
