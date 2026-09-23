### Inventario de evidencia para el artifact del Paper 1

#### Estado

Gate: 8G-F4

Estado del inventario: verificado para continuar con 8G-F5.

#### Bundle histórico 8E

Ruta física auditada:

`/home/project/backups/dtl-lab/paper1-q3-v1-reproduction.tar.gz`

SHA-256:

`d464888e9f3e5d8cc64ef5d22cc7b7c24f83e3853f5825f18f23de26adf6a6e6`

El checksum externo coincide con el sidecar histórico.

El archive es un TAR gzip legible.

El verificador versionado del proyecto acepta su estructura y sus checksums internos.

#### Manifest del bundle

El `bundle-manifest.json` declara:

`schema_version = 1`

`phase = 8E`

`protocol_id = paper1-q3-v1`

`status = preparado`

`source_phase = 8D`

`source_commit = 6cd88c377afd23fee4998882f91142d71e7d963e`

`clean_repository_required = true`

`full_matrix_rerun_required = false`

`scientific_smoke_tasks = 6`

`task_rows = 1272`

`tables = 8`

`figures = 8`

`bundle_files = 35`

La reproducción histórica no exige repetir la matriz completa de 1272 tareas.

#### Raw histórico

El bundle contiene:

`raw/paper1-q3-v1.tar.gz`

SHA-256:

`d5b553de18d17da4c5b4278b4d13fe48e7e0898f7bbec494eece5e67f469b463`

El hash coincide con el sidecar interno y con `bundle-manifest.json`.

El respaldo contiene, entre otros:

- `raw-manifest.json`
- `provenance.json`
- `environment.json`
- `runtime.json`
- `state.json`
- `timing-host.json`

#### Resultados derivados de referencia

El bundle conserva:

- `reference/derived/`
- `reference/tables/`
- `reference/figures/`

La referencia contiene ocho figuras y ocho tablas en CSV y Markdown.

Los resultados derivados se mantienen separados de raw.

El `bundle-manifest.json` registra además el SHA-256 del `derived-manifest.json` de referencia:

`7a2622726f96e209171096a1c50109600c3af7c0416b595cad1a836fbc5b889f`

#### Dependencias del estudio

El subconjunto científico se concentra en los módulos de sharding, simulación, trazas, conformidad y verificación, junto con las pruebas directamente asociadas.

Los imports internos observados desde esas rutas alcanzan los siguientes módulos de DLT-Lab:

- `blockchain`
- `crypto`
- `sharding`
- `simulation`
- `trace`
- `transaction`

Si alguno de estos módulos pertenece al software general, su incorporación al artifact se considera una dependencia de compilación o ejecución y no evidencia científica adicional.

#### Dependencias Java externas

Resultado del inventario estático:

- No se detectaron imports de bibliotecas Java externas

`pom.xml` declara dependencias de librerías mediante una sección `dependencies`: `false`.

El build Maven fija Java 17 y utiliza el plugin de compilación correspondiente.

#### Herramientas formales

Versiones fijadas por el repositorio:

TLC:

`1.7.4`

Alloy:

`6.2.0`

Java requerido:

`17`

Los binarios descargados de TLC y Alloy no forman parte automáticamente del artifact público.

Las versiones y mecanismos de descarga permanecen definidos por los scripts versionados.

#### Comandos requeridos por la reproducción

El contrato de reproducción declara los siguientes comandos:

- `git`
- `make`
- `java`
- `javac`
- `python3`
- `curl`
- `tar`
- `sha256sum`
- `/usr/bin/time`
- `readlink`

La presencia de estos comandos en el contrato no significa que sus binarios deban incorporarse al archive público.

#### Clausura de dependencias

8G-F4 inventaría las dependencias requeridas.

8G-F6 comprobará que la selección final del artifact sea suficiente para compilar, ejecutar el smoke y regenerar el análisis.

Esta separación evita declarar como evidencia científica componentes generales de DLT-Lab que solo sean necesarios como soporte técnico.

#### Separación de revisiones

Experimental baseline:

`45cb114d61b1df8c605c50700f3cc72d48d157fe`

Independent reproduction source:

`6cd88c377afd23fee4998882f91142d71e7d963e`

Scientific manuscript freeze:

`06bea7de70971d5b22d705a2df19137122758c08`

Main al abrir 8G-F4:

`908392f356961b9689a6870aa1abe9b6c3e9f339`

Estas revisiones tienen funciones diferentes y no deben tratarse como un único estado canónico.

Artifact-release revision:

`PENDING`

Submission revision:

`PENDING`

#### Resultado de 8G-F4

El bundle histórico 8E fue localizado físicamente.

Su hash externo fue verificado.

Su manifest interno fue inspeccionado.

Los checksums internos fueron verificados.

El raw interno fue localizado y verificado.

Los resultados derivados, tablas y figuras fueron inventariados.

Las dependencias del subconjunto científico fueron inventariadas.

La clausura ejecutable de esas dependencias queda reservada para la verificación de 8G-F6.

El bundle histórico 8E permanece diferenciado del artifact final de publicación.

8G-F4 no crea un release, DOI ni URL persistente.

Estado final:

`8G-F4: DONE`
