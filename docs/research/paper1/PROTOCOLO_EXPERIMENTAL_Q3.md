### Protocolo experimental Q3

#### Propósito

La Fase 8A congela el diseño experimental del Paper 1 antes de ejecutar la matriz definitiva. El protocolo define preguntas, hipótesis, factores, niveles, configuraciones válidas, seeds, repeticiones, límites de recursos, métricas, análisis estadístico, tablas, figuras y reglas para ejecuciones incompletas.

Esta fase no genera resultados definitivos ni modifica la semántica de Java, TLA+, Alloy, JSONL, la función de abstracción, el replay TLC o el corpus negativo.

#### Baseline

El protocolo se construye sobre:

- commit fuente `45cb114d61b1df8c605c50700f3cc72d48d157fe`;
- release técnico `v1.1.0-rc.1`;
- TLA+ Tools 1.7.4;
- Alloy 6.2.0 con solver SAT4J;
- Java 17;
- Python 3.12;
- conformidad acotada cerrada en la Fase 7E.

El commit fuente identifica el estado previo a cualquier infraestructura o resultado de la Fase 8.

#### Principio de congelamiento

El protocolo se considera congelado cuando este documento, `experiment-spec.json`, `configurations.csv` y `seeds.txt` son consistentes y pasan el gate estructural.

Cualquier cambio posterior a factores, niveles, seeds, repeticiones, límites, métricas o reglas estadísticas requiere:

- una enmienda versionada;
- una justificación previa a la nueva ejecución;
- un nuevo identificador de protocolo;
- conservación del protocolo anterior;
- separación de los resultados producidos por cada versión.

No se permite ajustar la matriz después de observar resultados para favorecer una hipótesis.

#### RQ1: preservación de propiedades de seguridad

Pregunta:

¿El protocolo de commit cross-shard preserva las propiedades declaradas bajo los interleavings explorados dentro de los bounds documentados?

Hipótesis H1:

Los modelos válidos no producen violaciones dentro de los bounds declarados.

H1 es una hipótesis operacional pendiente de evaluación. Un resultado sin contraejemplo solo se interpreta dentro de la configuración ejecutada.

Unidad experimental:

- propiedad;
- herramienta;
- configuración;
- repetición medida.

Variables de respuesta:

- resultado de la propiedad;
- estados generados;
- estados distintos;
- profundidad;
- scope;
- tiempo total;
- memoria residente máxima;
- código de salida;
- contraejemplo.

#### RQ2: capacidad de detección de defectos

Pregunta:

¿TLC y Alloy detectan mutaciones realistas que eliminan controles del protocolo cross-shard?

Hipótesis H2:

Cada mutante científico produce una violación de su propiedad objetivo.

Unidad experimental:

- mutante;
- herramienta;
- configuración;
- repetición medida.

Variables de respuesta:

- mutante detectado;
- propiedad objetivo violada;
- tiempo hasta contraejemplo;
- memoria residente máxima;
- profundidad o scope;
- artefacto del contraejemplo;
- código de salida.

La medida principal es el mutation score, acompañado por la trazabilidad entre mutante y propiedad objetivo.

#### RQ3: conformidad entre implementación y modelo

Pregunta:

¿Las trazas observables producidas por la implementación Java corresponden a secuencias de acciones admitidas por el modelo TLA+ dentro de los escenarios y bounds evaluados?

Hipótesis H3:

Las trazas válidas son aceptadas y las trazas corruptas son rechazadas en el punto esperado.

Unidad experimental:

- escenario o mutación;
- seed;
- resultado TLC.

La matriz definitiva contiene:

- diez escenarios válidos por treinta seeds;
- diez mutaciones negativas por treinta seeds;
- trescientas trazas válidas;
- trescientas trazas negativas;
- seiscientas ejecuciones de replay TLC.

Variables de respuesta:

- aceptación de traza válida;
- rechazo de traza negativa;
- coincidencia de diagnóstico;
- paso abstracto;
- paso concreto;
- acción;
- transferencia;
- tiempo;
- memoria;
- código de salida.

La conclusión se denomina `bounded implementation-model trace conformance`. No se presenta como refinamiento general.

#### RQ4: costo de verificación

Pregunta:

¿Cómo crece el costo de verificación al aumentar shards, transferencias concurrentes, bounds y fallos habilitados?

Hipótesis H4:

El costo de model checking crece de forma no lineal con el número de transferencias concurrentes y shards.

H4 permanece pendiente de evaluación. No debe redactarse como resultado confirmado antes de ejecutar y analizar la matriz.

Unidad experimental:

- herramienta;
- perfil de bound;
- perfil de fallo;
- repetición medida.

Variables de respuesta:

- estados generados;
- estados distintos;
- profundidad;
- scope;
- tiempo total;
- memoria residente máxima;
- timeout;
- falta de memoria;
- razón de crecimiento dentro de cada herramienta.

#### Factores y niveles

| Factor | Niveles |
|---|---|
| herramienta | TLC, Alloy |
| modelo | válido, mutante |
| shards | 2, 3, 4 |
| transferencias concurrentes | 1, 2, 3 |
| validadores | 2, 3, 4 |
| quorum | 2, 2, 3 según perfil |
| copias de recibo | 1, 2 |
| perfil de bound | catálogo, pequeño, mediano, grande |
| perfil de fallo TLC | normal, replay, timeout, quorum insuficiente |
| tipo de traza | válida, negativa |
| seed de conformidad | treinta valores fijados |

Las combinaciones permitidas están enumeradas en `experiments/paper1/configurations.csv`. Una combinación ausente no pertenece a la matriz definitiva.

#### Perfiles de bound

| Perfil | Shards | Transferencias | Validadores | Quorum | Copias de recibo | Scope de estados Alloy |
|---|---:|---:|---:|---:|---:|---:|
| pequeño | 2 | 1 | 2 | 2 | 1 | 6 |
| mediano | 3 | 2 | 3 | 2 | 2 | 8 |
| grande | 4 | 3 | 4 | 3 | 2 | 10 |

El perfil `catálogo` conserva las configuraciones y escenarios de la Fase 7 para RQ3.

Los perfiles son bundles experimentales. La Fase 8B debe traducirlos a configuraciones de TLC y scopes de Alloy sin modificar los modelos base versionados.

#### Configuraciones válidas

La matriz congelada contiene catorce familias de configuración:

- seis perfiles válidos de escalabilidad, tres por herramienta;
- dos familias de mutantes, una por herramienta;
- dos familias de conformidad TLC, válida y negativa;
- cuatro perfiles de fallo TLC.

Cada familia tiene un identificador único. El runner de la Fase 8B debe expandir propiedades, mutantes, escenarios, seeds y repeticiones sin crear combinaciones adicionales.

#### Seeds

Las treinta seeds definitivas se almacenan en:

```text
experiments/paper1/seeds.txt
```

Las seeds son enteros únicos y ordenados. La matriz usa exactamente los valores `2026001` a `2026030`.

La seed `2026` se conserva únicamente para smoke tests heredados de la Fase 7. No sustituye la matriz multiseed definitiva.

#### Repeticiones

Para mediciones de tiempo y memoria:

- dos repeticiones de calentamiento;
- diez repeticiones medidas;
- ejecución serial;
- ningún resultado de calentamiento entra al análisis principal.

Para resultados lógicos deterministas:

- una observación lógica por caso y seed;
- las repeticiones medidas se usan para tiempo y memoria;
- el resultado lógico debe coincidir en todas las repeticiones.

RQ3 usa treinta seeds y una ejecución por caso y seed. No usa repeticiones de calentamiento.

#### Límites de recursos

Cada ejecución medida usa:

- timeout de 1800 segundos;
- límite de memoria residente de 12288 MiB;
- una ejecución experimental simultánea;
- un worker de TLC;
- solver SAT4J para Alloy;
- captura mediante `/usr/bin/time -v`.

Un timeout o falta de memoria es un resultado de escalabilidad. No se elimina de la matriz.

#### Hardware

Las mediciones principales se ejecutan en un único host Linux nativo y dedicado.

Antes de iniciar la Fase 8C se debe registrar:

- fabricante y modelo de CPU;
- cantidad de núcleos físicos y lógicos;
- memoria RAM;
- almacenamiento;
- kernel;
- distribución;
- arquitectura;
- versión de JVM;
- versión de Python;
- estado de virtualización;
- commit;
- temperatura y carga inicial cuando estén disponibles.

No se usan WSL2, máquinas virtuales ni runners compartidos para las comparaciones principales de tiempo y memoria.

GitHub Actions se usa para corrección funcional, integridad y reproducibilidad estructural. Sus tiempos no se mezclan con las mediciones principales.

#### Versiones de software

Las versiones congeladas son:

- TLA+ Tools 1.7.4;
- Alloy 6.2.0;
- solver Alloy SAT4J;
- Java 17;
- Python 3.12;
- un worker de TLC;
- `/usr/bin/time` para tiempo y memoria.

La ejecución definitiva falla si una versión o checksum no coincide con el protocolo.

#### Tratamiento de ejecuciones incompletas

Los resultados se clasifican como:

- `passed`;
- `counterexample`;
- `timeout`;
- `out_of_memory`;
- `tool_error`;
- `instrumentation_error`;
- `invalid_configuration`.

Reglas:

- timeout y falta de memoria se conservan;
- una violación de propiedad se conserva;
- un código de salida no esperado se conserva;
- una configuración inválida no se ejecuta;
- un fallo de instrumentación puede repetirse una vez;
- el intento original y la causa permanecen registrados;
- no se repite una ejecución por producir un resultado desfavorable.

#### Criterios de exclusión

Solo se excluyen del análisis estadístico principal:

- configuración que no coincide con `configurations.csv`;
- versión o checksum de herramienta incorrecto;
- ausencia de métricas por fallo de instrumentación antes de iniciar la herramienta;
- archivo raw corrupto o incompleto;
- ejecución manual interrumpida antes de iniciar el solver.

Los casos excluidos permanecen en el manifiesto con su razón.

Timeouts, falta de memoria, contraejemplos y resultados negativos no son criterios de exclusión.

#### Métricas TLC

Se registran:

- propiedad;
- resultado;
- estados generados;
- estados distintos;
- profundidad;
- tiempo total;
- memoria residente máxima;
- código de salida;
- longitud del contraejemplo;
- perfil experimental;
- repetición;
- commit;
- ambiente.

#### Métricas Alloy

Se registran:

- assertion;
- resultado;
- solver;
- scope;
- contraejemplos;
- tiempo de resolución;
- tiempo total;
- memoria residente máxima;
- código de salida;
- tamaño del contraejemplo;
- perfil experimental;
- repetición;
- commit;
- ambiente.

No se comparan estados TLC con scopes Alloy como si fueran la misma unidad.

#### Métricas de conformidad

Se registran:

- escenario o mutación;
- seed;
- tipo de caso;
- aceptación;
- coincidencia del diagnóstico;
- paso abstracto;
- paso concreto;
- acción;
- transferencia;
- tiempo;
- memoria;
- commit;
- hashes de entrada.

#### Plan estadístico

Para tiempo y memoria:

- mediana;
- rango intercuartílico;
- mínimo;
- máximo;
- coeficiente de variación;
- intervalo bootstrap del 95 por ciento para la mediana;
- diez mil remuestras bootstrap.

Para RQ2:

- mutation score global;
- mutation score por herramienta;
- detección por propiedad objetivo;
- intervalo exacto o Wilson del 95 por ciento para proporciones.

Para RQ3:

- proporción de trazas válidas aceptadas;
- proporción de trazas negativas rechazadas;
- proporción de diagnósticos coincidentes;
- intervalo Wilson del 95 por ciento;
- listado completo de cualquier discrepancia.

Para RQ4:

- medianas por perfil;
- razón de crecimiento dentro de cada herramienta;
- correlación de Spearman entre nivel ordinal y costo;
- análisis separado de tiempo, memoria y estados;
- registro de censura por timeout o falta de memoria.

TLC y Alloy se comparan por capacidad de detección y crecimiento relativo dentro de cada herramienta. No se reclama superioridad absoluta de rendimiento entre semánticas no equivalentes.

#### Tablas previstas

- Tabla 1: herramientas, versiones y hardware;
- Tabla 2: propiedades y resultados por herramienta;
- Tabla 3: mutantes, propiedad objetivo y detección;
- Tabla 4: conformidad multiseed;
- Tabla 5: costo por perfil de bound;
- Tabla 6: fallos habilitados y costo TLC;
- Tabla 7: ejecuciones incompletas;
- Tabla 8: amenazas a la validez y mitigaciones.

#### Figuras previstas

- Figura 1: arquitectura de evidencia Java, TLA+, Alloy y replay;
- Figura 2: flujo del protocolo experimental;
- Figura 3: estados distintos frente a transferencias;
- Figura 4: tiempo frente a transferencias concurrentes;
- Figura 5: memoria frente a shards;
- Figura 6: costo relativo por perfil;
- Figura 7: tiempo hasta contraejemplo por mutante;
- Figura 8: aceptación y rechazo multiseed.

#### Relación entre RQ, métrica y artefacto

| RQ | Métricas principales | Artefactos raw | Tablas o figuras |
|---|---|---|---|
| RQ1 | resultado, estados, profundidad, scope, tiempo, memoria | `tla_runs.csv`, `alloy_runs.csv`, logs | Tablas 1 y 2, Figuras 3 a 6 |
| RQ2 | detección, propiedad objetivo, tiempo hasta contraejemplo | manifiestos de mutantes y contraejemplos | Tabla 3, Figura 7 |
| RQ3 | aceptación, rechazo, diagnóstico, seed | manifiestos y matriz de conformidad | Tabla 4, Figura 8 |
| RQ4 | crecimiento de estados, tiempo, memoria, timeout y falta de memoria | resultados por perfil | Tablas 5 a 7, Figuras 3 a 6 |

#### Integridad y procedencia

Cada resultado definitivo debe incluir:

- identificador de protocolo;
- identificador de configuración;
- commit fuente;
- commit ejecutado;
- seed;
- repetición;
- versiones y checksums;
- ambiente;
- hora de inicio y fin;
- código de salida;
- hashes SHA-256 de entradas y salidas.

Los resultados raw son inmutables. Las tablas y figuras se regeneran desde copias derivadas.

#### Gate de Fase 8A

La Fase 8A queda cerrada cuando:

- ninguna configuración experimental queda implícita;
- RQ1, RQ2, RQ3 y RQ4 tienen variables y métricas;
- H1, H2, H3 y H4 están declaradas como hipótesis pendientes;
- las treinta seeds son únicas y están fijadas;
- dos calentamientos y diez repeticiones medidas están fijados;
- timeout, memoria y paralelismo están declarados;
- los criterios de exclusión están escritos;
- el tratamiento de timeout y falta de memoria está escrito;
- hardware y versiones requeridas están declarados;
- el plan estadístico está congelado;
- tablas y figuras previstas están declaradas;
- el protocolo pasa la validación estructural;
- no se han generado resultados definitivos.

#### Fuera de alcance

La Fase 8A no:

- ejecuta la matriz definitiva;
- produce conclusiones para H1 a H4;
- modifica Java;
- modifica TLA+;
- modifica Alloy;
- modifica JSONL;
- modifica la función de abstracción;
- modifica el replay TLC;
- modifica el corpus negativo;
- crea el manuscrito final;
- crea el snapshot editorial.
