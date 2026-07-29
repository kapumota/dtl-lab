### Ejecución de la matriz experimental

#### Propósito

La Fase 8C conecta la infraestructura de Fase 8B con TLC, Alloy y la conformidad Java-TLA+ para ejecutar el protocolo congelado `paper1-q3-v1`.

La fase usa el plan determinista de 1272 tareas. No modifica factores, configuraciones, seeds, repeticiones, límites de recursos, hipótesis ni métricas de Fase 8A.

La Fase 8C no genera resultados derivados. Las tablas, figuras y respuestas finales a RQ1 a RQ4 pertenecen a la Fase 8D.

#### Baseline

La ejecución se construye sobre:

- protocolo experimental congelado en Fase 8A
- infraestructura serial y reanudable de Fase 8B
- modelos TLA+ y Alloy versionados
- parsers formales existentes
- función de abstracción Java-TLA+
- replay TLC de trazas válidas
- corpus negativo con diagnóstico esperado

Los modelos base y los catálogos existentes permanecen sin cambios.

#### Traducción de perfiles

`experiments/paper1/execution-profiles.json` traduce los perfiles congelados a constantes TLC y scopes Alloy.

La traducción no agrega nuevas unidades experimentales. Cada tarea conserva un `configuration_id` presente en `configurations.csv`.

Para TLC se generan configuraciones temporales dentro del directorio raw de cada tarea.

Para Alloy se genera una copia ejecutable dentro del directorio raw de cada tarea. La copia conserva el modelo versionado y sustituye únicamente los comandos `check` por el comando correspondiente a la tarea.

Los archivos generados no se escriben en `specs/tla` ni en `specs/alloy`.

#### Perfiles de fallo TLC

Los cuatro perfiles de fallo usan la topología mediana y aplican niveles ya declarados por el protocolo.

- `normal` usa una copia de recibo, timeout deshabilitado y quorum 2
- `replay` usa dos copias de recibo, timeout deshabilitado y quorum 2
- `timeout` usa dos copias, una copia retrasada, timeout habilitado y quorum 2
- `insufficient_quorum` usa dos copias, timeout deshabilitado y quorum 3

El perfil `insufficient_quorum` aumenta el requisito de quorum dentro de los tres validadores declarados. No usa el mutante `QuorumBypass`.

#### Executor TLC

El executor TLC reutiliza:

```text
scripts/formal/run_tlc.sh
scripts/formal/parse_tlc_results.py
```

Cada tarea usa un directorio `FORMAL_RESULTS_DIR` aislado.

Las tareas válidas generan una configuración temporal con `TypeOK` y la propiedad objetivo.

Las tareas mutantes reutilizan el modelo y la configuración versionados de Fase 6.

El executor conserva:

- stdout
- stderr
- tiempo de herramienta
- memoria de herramienta
- estados generados
- estados distintos
- profundidad
- propiedad violada
- contraejemplo cuando corresponde

#### Executor Alloy

El executor Alloy reutiliza:

```text
scripts/formal/run_alloy.sh
scripts/formal/parse_alloy_results.py
```

Cada tarea genera un módulo temporal con un único comando `check`.

Los scopes usados son:

| Perfil | State | Transfer | Shard | Validator | Receipt | Message |
|---|---:|---:|---:|---:|---:|---:|
| pequeño | 6 | 1 | 2 | 2 | 2 | 10 |
| mediano | 8 | 2 | 3 | 3 | 4 | 20 |
| grande | 10 | 3 | 4 | 4 | 6 | 30 |

`Receipt` representa entidades del modelo Alloy. No se interpreta como una igualdad directa con `ReceiptCopies` de TLC.

El executor conserva:

- assertion
- resultado
- solver
- scope
- contraejemplos
- tiempo de resolución
- tiempo total
- memoria residente
- propiedad objetivo del mutante

#### Executor de conformidad

`ConformanceCaseRunner` ejecuta un único escenario válido o una única mutación negativa.

El runner reutiliza:

- `ScenarioCatalog`
- `TraceRecorder`
- `JavaToTlaStateMapper`
- `TraceConformanceChecker`
- `NegativeTraceCatalog`

No redefine acciones, guardas, abstracción ni diagnóstico.

Para cada tarea registra:

- caso
- seed
- aceptación
- coincidencia de diagnóstico
- paso abstracto
- paso concreto
- acción
- transferencia
- rutas del módulo y configuración generados
- stdout y stderr de TLC

#### Preparación del runtime

Antes de una ejecución se verifican:

- Java 17
- Python 3.12
- TLA+ Tools 1.7.4
- SHA-1 fijado de TLC
- Alloy 6.2.0
- compilación de las fuentes Java
- disponibilidad de `/usr/bin/time`

La compilación se realiza antes de medir tareas de conformidad.

#### Smoke científico

El comando:

```bash
make experiment-scientific-smoke
```

ejecuta seis tareas representativas:

- propiedad válida TLC
- mutante TLC
- propiedad válida Alloy
- mutante Alloy
- traza válida
- traza negativa

El smoke puede ejecutarse en CI. Sus tiempos no se usan en el análisis principal.

GitHub Actions publica el directorio `results/experiments/smoke-v1` como artefacto temporal.

#### Matriz definitiva

El comando:

```bash
make experiment-matrix
```

ejecuta o reanuda las 1272 tareas.

El directorio predeterminado es:

```text
results/experiments/raw/paper1-q3-v1
```

La ejecución definitiva exige:

- Linux nativo
- ausencia de WSL
- ausencia de máquina virtual detectada
- ausencia de GitHub Actions
- árbol versionado limpio
- memoria total mínima de 12288 MiB
- ejecución serial
- herramientas y versiones fijadas

Una segunda invocación reanuda el mismo directorio y omite tareas terminales válidas.

#### Estados terminales

La infraestructura conserva:

- `completed`
- `timeout`
- `out_of_memory`
- `tool_error`

Un timeout o falta de memoria permanece en raw.

Un mutante detectado se registra como tarea `completed` con `logical_outcome=counterexample` porque cumplió su expectativa experimental.

#### Manifiesto raw

Al completar una ejecución se genera:

```text
raw-manifest.json
```

El manifiesto registra:

- hash del plan
- hash del manifiesto del plan
- hash del ambiente
- hash de procedencia
- conteos por estado
- conteos por herramienta
- conteos por RQ
- resultados lógicos
- hash SHA-256 de cada `result.json`

El manifiesto declara que todavía no existen resultados derivados, tablas ni figuras.

#### Gate de Fase 8C

La fase queda lista para ejecución cuando:

- los archivos congelados de Fase 8A mantienen sus blobs
- los contratos de Fase 8B mantienen sus blobs
- el plan completo conserva 1272 tareas
- el smoke contiene seis rutas científicas distintas
- los ejecutores validan sus mapeos sin modificar modelos
- Java compila
- el smoke científico pasa con herramientas reales
- CI publica el artefacto smoke
- no se versionan resultados definitivos

La fase queda científicamente cerrada cuando la matriz definitiva termina en el host dedicado y `raw-manifest.json` registra las 1272 tareas terminales.
