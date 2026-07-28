### Resultados de replay y conformidad

#### Propósito

Este directorio recibe los módulos generados, salidas de TLC y manifiestos de las Fases 7C y 7D.

#### Generación

```bash
make conformance-replay
make conformance-negative
make conformance-research
```

Las salidas predeterminadas son:

```text
results/conformance/replay-v1/
results/conformance/negative-v1/
results/conformance/research-v1/
```

#### Contenido generado

- un directorio por escenario;
- módulo y configuración de replay;
- copia de la especificación base;
- salidas estándar y de error de TLC;
- metadatos de model checking;
- `manifest.csv` con el resultado de cada escenario o mutación;
- `manifest.json` con procedencia y hashes integrados;
- `summary.json` y `summary.md` con el cierre acotado;
- `conformance_matrix.csv` con los veinte casos evaluados.

#### Política de versionado

Los resultados generados no se versionan. Solo este README permanece en Git.

La metodología se documenta en:

```text
docs/research/paper1/REPLAY_TLC.md
docs/research/paper1/CORPUS_NEGATIVO_TRAZAS.md
docs/research/paper1/CIERRE_CONFORMIDAD_FASE_7.md
```
