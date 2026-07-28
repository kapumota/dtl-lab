### Model checking ejecutable de la Fase 5

#### Propósito

La Fase 5 transforma la verificación formal de DLT-Lab desde una comprobación de presencia de archivos hacia una ejecución obligatoria y reproducible de TLC y Alloy.

El perfil científico no acepta como evidencia que una especificación exista o que contenga nombres de propiedades. La ejecución debe iniciar ambas herramientas, identificar sus versiones, producir reportes legibles por máquina y confirmar el resultado de cada propiedad.

#### Separación de perfiles

DLT-Lab conserva dos perfiles con objetivos diferentes.

#### Validación educativa

```bash
make validate
```

Este perfil mantiene una ejecución rápida y sin descargas obligatorias. Comprueba:

- estructura de los modelos;
- presencia de configuraciones;
- nombres de propiedades;
- scripts requeridos;
- compilación y pruebas Java;
- invariantes runtime existentes.

El perfil educativo no afirma que TLC o Alloy se hayan ejecutado.

#### Validación científica

```bash
make formal-research
```

Este perfil es estricto. Falla cuando:

- falta TLC;
- falta Alloy;
- no puede identificarse una versión ejecutable;
- una propiedad del modelo válido falla;
- una herramienta no genera un reporte;
- faltan métricas obligatorias;
- un control negativo no es detectado;
- no se generan los resultados estructurados.

#### Versiones fijadas

Las versiones se declaran en:

```text
scripts/formal/tool_versions.env
```

La Fase 5 fija:

```text
TLA+ Tools 1.7.4
Alloy 6.2.0
Java 17 o superior
```

`install_tla_tools.sh` verifica el SHA-1 publicado para `tla2tools.jar`. `install_alloy.sh` descarga el artefacto de Maven Central y valida el SHA-1 publicado junto al artefacto.

Las herramientas se almacenan localmente en `.formal-tools/`, directorio excluido del control de versiones.

#### Scripts

```text
scripts/formal/
├── check_structure.sh
├── install_tla_tools.sh
├── install_alloy.sh
├── run_tlc.sh
├── run_alloy.sh
├── parse_tlc_results.py
├── parse_alloy_results.py
├── run_formal_research.sh
└── tool_versions.env
```

#### Responsabilidades

`check_structure.sh` valida el contrato mínimo de modelos, propiedades, scripts y versiones.

`install_tla_tools.sh` instala una distribución concreta de TLA+ Tools y rechaza un artefacto con checksum diferente.

`install_alloy.sh` instala una distribución concreta de Alloy y comprueba que la versión ejecutable coincida con la versión declarada.

`run_tlc.sh` ejecuta TLC con un worker para conservar una profundidad reproducible, captura tiempo y memoria, y delega el análisis a Python.

`run_alloy.sh` ejecuta todos los comandos `check` mediante la interfaz CLI, conserva `receipt.json`, captura tiempo y memoria, y delega el análisis a Python.

`parse_tlc_results.py` extrae estados generados, estados distintos, profundidad, tiempo, memoria, versión y propiedad violada.

`parse_alloy_results.py` extrae solver, alcance, contraejemplos, tiempo de resolución, tiempo total, memoria y resultado de cada assertion.

`run_formal_research.sh` coordina el perfil completo, construye el manifiesto y comprueba que todos los reportes existan.

#### Resultados

La ejecución científica crea:

```text
results/formal/
├── tool_versions.txt
├── environment.json
├── tla_runs.csv
├── alloy_runs.csv
├── execution_manifest.json
└── logs/
```

`tool_versions.txt` registra versiones reportadas y hashes SHA-256 de los artefactos ejecutados.

`environment.json` registra commit, Java, Python, sistema operativo, arquitectura, CPU y metadatos de GitHub Actions cuando están disponibles.

`tla_runs.csv` contiene una fila por propiedad y ejecución de TLC.

`alloy_runs.csv` contiene una fila por assertion y ejecución de Alloy.

`execution_manifest.json` reúne los resúmenes de las ejecuciones y declara si se cumplieron las expectativas.

`logs/` conserva stdout, stderr, mediciones de `/usr/bin/time`, reportes JSON, filas intermedias y metadatos de las herramientas.

#### Métricas TLC

Cada ejecución válida de TLC debe informar:

- estados generados;
- estados distintos;
- profundidad del grafo de estados;
- tiempo total;
- memoria residente máxima;
- código de salida;
- resultado de cada invariante.

La Fase 5 usa un worker. El objetivo es evitar que la profundidad reportada dependa de una exploración paralela no determinista.

#### Métricas Alloy

Alloy no usa la terminología de estados generados, estados distintos y profundidad de TLC. Por esa razón esas columnas permanecen vacías en `alloy_runs.csv` y se registran métricas propias:

- solver;
- alcance del comando;
- cantidad de contraejemplos;
- duración de resolución disponible en el recibo;
- tiempo total del proceso;
- memoria residente máxima;
- código de salida;
- resultado de cada assertion.

#### Corrección mínima del modelo Alloy

La ejecución obligatoria reveló que el predicado `wellFormed` del baseline no expresaba tres obligaciones que las assertions sí reclamaban:

- un crédito destino consume exactamente un recibo;
- un abort después del débito libera fondos;
- una expiración libera fondos y no puede coexistir con commit.

La Fase 5 agrega esas restricciones al predicado y declara `expect 0` en los cinco checks. No introduce mensajes, sesiones múltiples ni nuevos estados formales.

#### Controles negativos

El workflow debe demostrar que las herramientas detectan una propiedad defectuosa. Para evitar superposición con la Fase 6, la Fase 5 no versiona un catálogo de mutantes científicos.

`run_formal_research.sh` crea temporalmente:

- una copia TLA+ donde `AtomicCommit` es falsa;
- una copia Alloy donde la assertion `AtomicCommit` es falsa.

Estas copias solo verifican el comportamiento del pipeline. Se eliminan al finalizar la ejecución. Los mutantes científicos, sus operadores y sus contraejemplos versionados pertenecen a la Fase 6.

#### Workflow

El workflow nuevo es:

```text
.github/workflows/formal-verification.yml
```

Ejecuta:

1. checkout;
2. Java 17;
3. Python;
4. restauración de caché de herramientas;
5. instalación versionada de TLC;
6. instalación versionada de Alloy;
7. modelos válidos;
8. controles negativos temporales;
9. verificación de resultados;
10. publicación de `results/formal/`.

#### Interpretación de los resultados

Un resultado `PASS` significa que no se encontró una violación dentro del alcance y configuración ejecutados.

Un resultado `FAIL` significa que la herramienta encontró una violación o que el proceso no terminó correctamente.

La ejecución acotada no constituye una prueba universal de la implementación Java. La relación automática entre trazas Java y estados formales se desarrollará en la Fase 7.

#### Comandos locales

Instalación:

```bash
bash scripts/formal/install_tla_tools.sh
bash scripts/formal/install_alloy.sh
```

Ejecución científica:

```bash
make formal-research
```

Inspección:

```bash
column -s, -t < results/formal/tla_runs.csv
column -s, -t < results/formal/alloy_runs.csv
python3 -m json.tool results/formal/execution_manifest.json
```

#### Criterios de cierre

La Fase 5 se considera cerrada cuando:

1. TLC se ejecuta obligatoriamente en el perfil científico.
2. Alloy se ejecuta obligatoriamente en el perfil científico.
3. las versiones de distribución están fijadas;
4. la versión ejecutable de cada herramienta puede identificarse;
5. los modelos válidos pasan;
6. los controles negativos son detectados;
7. TLC registra estados, profundidad, tiempo y memoria;
8. Alloy registra solver, alcance, contraejemplos, tiempo y memoria;
9. cada propiedad tiene un resultado estructurado;
10. GitHub Actions publica `results/formal/`;
11. `make validate` conserva el perfil educativo;
12. no se introducen todavía modelos multisesión ni mutantes científicos.
