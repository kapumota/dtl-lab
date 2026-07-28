### Resultados formales generados

#### Propósito

Este directorio recibe los resultados del perfil `formal-research`. Los archivos generados no se versionan. GitHub Actions los publica como `dtl-lab-formal-results`.

#### Estructura

```text
tool_versions.txt
environment.json
tla_runs.csv
alloy_runs.csv
mutant_matrix.csv
execution_manifest.json
logs/
counterexamples/
```

#### Propiedad objetivo

`alloy_runs.csv` registra la propiedad objetivo de cada mutante. Los resúmenes JSON indican si esa propiedad falló y qué otras propiedades produjeron contraejemplos.

#### Procedencia

`environment.json` y `execution_manifest.json` distinguen commit fuente, commit ejecutado y referencia fuente. En GitHub Actions también registran evento, run, SHA y referencias de pull request.

#### Contraejemplos

Cada mutante TLA+ conserva la salida de TLC. Cada mutante Alloy conserva al menos una solución JSON del solver.

#### Generación

```bash
bash scripts/formal/install_tla_tools.sh
bash scripts/formal/install_alloy.sh
make formal-research
```
