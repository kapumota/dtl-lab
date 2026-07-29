### Infraestructura experimental de Fase 8B

#### Propósito

La Fase 8B implementa la infraestructura reproducible que consumirá el protocolo congelado de la Fase 8A.

La infraestructura construye un plan determinista, captura ambiente y procedencia, ejecuta tareas de forma serial, conserva resultados raw y permite reanudar una ejecución sin repetir tareas terminales.

La Fase 8B no ejecuta la matriz definitiva.

#### Baseline

La infraestructura se construye sobre:

- protocolo `paper1-q3-v1`
- merge de Fase 8A `8c4afea245e14c5827b0f60ea72c0bacc36afc0a`
- `experiment-spec.json` congelado
- `configurations.csv` con catorce configuraciones
- `seeds.txt` con treinta seeds
- TLA+ Tools 1.7.4
- Alloy 6.2.0 con SAT4J
- Java 17
- Python 3.12

Los archivos congelados de Fase 8A no cambian.

#### Separación de responsabilidades

La Fase 8B agrega infraestructura y contratos de salida.

La Fase 8C agregará los executors reales para TLC, Alloy y conformidad multiseed.

La Fase 8D consumirá exclusivamente resultados raw para generar resultados derivados, tablas y figuras.

#### Inventario de casos

`experiments/paper1/cases.json` registra:

- siete propiedades disponibles para TLC y Alloy
- cinco mutantes TLC
- cinco mutantes Alloy
- diez escenarios válidos de conformidad
- diez mutaciones negativas de conformidad

El inventario deriva de artefactos versionados de las Fases 5, 6 y 7.

No agrega propiedades, mutantes, escenarios o seeds nuevos.

#### Construcción del plan

El comando:

```bash
python3 -B scripts/experiments/build_experiment_plan.py   --spec experiments/paper1/experiment-spec.json   --configurations experiments/paper1/configurations.csv   --seeds experiments/paper1/seeds.txt   --cases experiments/paper1/cases.json   --output-plan /ruta/plan.jsonl   --output-manifest /ruta/plan-manifest.json
```

genera un plan JSONL ordenado y un manifiesto con hashes SHA-256.

El plan contiene 1272 tareas:

- 504 tareas de propiedades para RQ1 y RQ4
- 120 tareas de mutantes para RQ2
- 600 tareas de conformidad multiseed para RQ3
- 48 tareas de perfiles de fallo para RQ4

El total incluye:

- 112 tareas de calentamiento
- 1160 tareas medidas

#### Identidad de tarea

Cada tarea registra:

- `task_id`
- `task_sha256`
- configuración
- RQ asociadas
- herramienta
- propiedad, mutante, escenario o perfil de fallo
- seed cuando corresponde
- tipo e índice de repetición
- timeout
- límite de memoria

`task_sha256` se calcula sobre la definición canónica de la tarea.

Un resultado existente solo puede reutilizarse cuando `task_id` y `task_sha256` coinciden.

#### Ejecución reanudable

`run_experiment_matrix.py` ejecuta una tarea por vez.

La ejecución:

- usa un lock exclusivo por directorio
- rechaza dos procesos concurrentes
- conserva un snapshot del plan y su manifiesto
- captura ambiente y procedencia
- escribe cada tarea en un directorio independiente
- usa escrituras atómicas para metadatos
- omite tareas que ya tienen un resultado terminal válido
- conserva intentos interrumpidos fuera del resultado terminal
- actualiza `state.json` después de cada tarea

El runner no contiene lógica específica de TLC, Alloy o Java.

Recibe un executor con esta interfaz:

```text
executor --task task.json --output-dir directorio
```

La separación permite agregar los executors científicos en Fase 8C sin cambiar la lógica de reanudación.

#### Medición y estados terminales

Cada tarea se envuelve con `/usr/bin/time -v`.

El resultado raw puede terminar en:

- `completed`
- `timeout`
- `out_of_memory`
- `tool_error`

Los estados incompletos no se eliminan ni se transforman en resultados favorables.

El runner registra:

- tiempo de inicio y fin
- tiempo transcurrido
- memoria residente máxima
- código de salida
- stdout
- stderr
- salida del executor
- cumplimiento de expectativa cuando está disponible

#### Restricciones de CI

GitHub Actions puede ejecutar:

- construcción del plan
- validación estructural
- dry run
- smoke test con executor simulado
- prueba de reanudación

GitHub Actions no puede producir mediciones definitivas.

El runner rechaza una ejecución definitiva cuando `GITHUB_ACTIONS=true`.

#### Captura de ambiente

`collect_environment.py` registra:

- commit y rama
- limpieza del árbol versionado
- sistema operativo
- arquitectura
- procesador
- cantidad de CPU
- memoria total
- versiones de Python, Java, javac, Git y `/usr/bin/time`
- presencia y SHA-256 de TLC y Alloy
- contexto de GitHub Actions cuando existe

La captura no instala herramientas ni modifica `.formal-tools`.

#### Estructura raw

Una ejecución usa:

```text
run/
  environment.json
  provenance.json
  state.json
  snapshots/
    plan.jsonl
    plan-manifest.json
  tasks/
    task-id/
        task.json
        stdout.txt
        stderr.txt
        time.txt
        executor-result.json
        result.json
  incomplete_attempts/
```

Los resultados derivados no pueden almacenarse dentro del directorio raw.

#### Validación

El gate de infraestructura es:

```bash
make experiment-infrastructure
```

El gate:

- valida nuevamente el protocolo 8A
- verifica que los cuatro archivos congelados mantengan sus blobs
- construye el plan de 1272 tareas
- valida conteos y hashes
- ejecuta un dry run
- ejecuta dos tareas simuladas
- repite la ejecución para comprobar reanudación
- valida el resultado raw parcial
- comprueba estilos documentales
- confirma ausencia de resultados definitivos en el repositorio

#### Executor simulado

`mock_experiment_executor.py` existe únicamente para probar la infraestructura.

No ejecuta TLC, Alloy, Java ni conformidad.

Sus salidas no son evidencia científica y se escriben dentro de un directorio temporal durante el gate.

#### Gate de cierre

La Fase 8B queda cerrada cuando:

- el protocolo 8A permanece sin cambios
- el plan es determinista
- los hashes de entrada y plan son reproducibles
- existen 1272 tareas sin duplicados
- la ejecución es serial
- la reanudación omite resultados terminales válidos
- ambiente y procedencia quedan registrados
- raw y derivados permanecen separados
- CI solo ejecuta smoke tests
- no existen resultados definitivos versionados
- Java, TLA+, Alloy, JSONL y conformidad no cambian

#### Alcance

La infraestructura no confirma H1, H2, H3 o H4.

La infraestructura no genera tablas ni figuras.

La infraestructura no reemplaza los scripts científicos de las fases anteriores.

La Fase 8C implementará los executors reales y ejecutará la matriz congelada.
