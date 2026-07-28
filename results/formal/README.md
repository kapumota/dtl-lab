### Resultados formales generados

#### Propósito

Este directorio recibe los resultados del perfil `formal-research`.

Los archivos generados no se versionan. GitHub Actions los publica como un artefacto denominado `dtl-lab-formal-results`.

#### Estructura

```text
tool_versions.txt
environment.json
tla_runs.csv
alloy_runs.csv
execution_manifest.json
logs/
```

#### Generación

```bash
bash scripts/formal/install_tla_tools.sh
bash scripts/formal/install_alloy.sh
make formal-research
```
