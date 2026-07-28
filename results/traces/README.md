### Resultados de trazas deterministas

#### Propósito

Este directorio recibe las salidas reproducibles de la Fase 7A.

#### Generación

```bash
make trace-export
```

La salida predeterminada se escribe en:

```text
results/traces/catalog-v1/
```

#### Contenido generado

- un archivo JSONL por escenario;
- `manifest.csv` con seed, cantidad de eventos, hash de contenido y hash de archivo.

#### Política de versionado

Los resultados generados no se versionan. Solo este README permanece en Git.

El contrato del formato se encuentra en:

```text
docs/research/paper1/FORMATO_DE_TRAZAS.md
```
