### Contribuciones esperadas

#### Estado de las contribuciones

Las contribuciones descritas en este archivo son objetivos de investigación. En la Fase 0 no deben presentarse como resultados alcanzados.

Cada contribución solo podrá reclamarse cuando exista código, configuración, resultado reproducible y evidencia versionada.

#### C1: especificación formal multisesión

Una especificación formal acotada de un protocolo de commit cross-shard que represente:

- múltiples shards;
- múltiples transferencias concurrentes;
- recibos identificables;
- quorum de validadores;
- commit, abort y timeout;
- mensajes retrasados, duplicados o reordenados;
- estados terminales irreversibles.

Condición para reclamarla:

- modelos TLA+ y Alloy ejecutables;
- configuraciones versionadas;
- propiedades claramente separadas de los supuestos;
- resultados de model checking conservados.

#### C2: evaluación mediante mutantes y contraejemplos

Una evaluación de la capacidad de las herramientas formales y runtime para detectar variantes defectuosas del protocolo.

Condición para reclamarla:

- catálogo de mutantes;
- una propiedad esperada por mutante;
- contraejemplos almacenados;
- comparación entre herramientas;
- resultados generados automáticamente.

#### C3: conformidad acotada Java-TLA+

Un mecanismo de trazas que relacione estados observables de la implementación Java con acciones y estados del modelo TLA+.

Condición para reclamarla:

- formato estable de trazas;
- función de abstracción documentada;
- verificador de conformidad;
- escenarios válidos y trazas corruptas de control;
- resultados reproducibles.

La denominación inicial será `bounded implementation-model trace conformance` o conformidad acotada de trazas entre implementación y modelo.

#### C4: artefacto reproducible

Un artefacto que permita ejecutar:

- pruebas Java;
- invariantes runtime;
- TLC;
- Alloy;
- mutantes;
- generación de trazas;
- análisis de conformidad;
- tablas del paper.

Condición para reclamarla:

- versiones de herramientas fijadas;
- comando principal de reproducción;
- datos raw conservados;
- resultados procesados generados por scripts;
- release y commit identificables.

#### Contribuciones que no se reclamarán

El Paper 1 no reclamará:

- una blockchain de producción;
- un protocolo BFT industrial completo;
- seguridad criptográfica de extremo a extremo;
- superioridad de rendimiento frente a otros simuladores;
- eliminación general de MEV;
- verificación no acotada de toda la implementación Java;
- prueba matemática general para una cantidad arbitraria de shards, salvo que una fase posterior la incorpore.
