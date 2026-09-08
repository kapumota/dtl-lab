### Síntesis final de Related Work para 8F-G6

#### Identificación

Paper:

`DTL-Lab Paper 1`

Fase:

`8F-G6`

Fecha:

`2026-09-07`

Objetivo:

Consolidar la matriz de Related Work, fijar la taxonomía de comparadores, delimitar el gap defendible y definir las reglas de redacción para la futura sección Background and Related Work.

Esta fase no amplía la búsqueda de forma general.

#### Base de evidencia consolidada

La matriz final contiene 17 trabajos.

Estado de verificación:

- 15 trabajos tienen `primary_source_verified: yes`;
- S10 Presto permanece como `include-supporting` con `primary_source_verified: no`;
- S20 ZooKeeper runtime trace-checking permanece como `include-supporting` con `primary_source_verified: no`.

Los dos trabajos sin full text verificado no se utilizarán para afirmaciones comparativas fuertes.

Cobertura de la matriz:

- siete trabajos se relacionan directamente con cross-shard processing o distributed cross-shard transactions;
- la literatura verificada incluye TLA+, Alloy, mutation model checking, implementation-model conformance, model-checking-guided testing y workflows multi-técnica;
- algunos trabajos publican artefactos y uno de los comparadores verificados reporta reproducción externa parcial;
- los trabajos cross-shard core se concentran principalmente en diseño del protocolo, atomicidad, consistencia, serializabilidad, liveness y desempeño del sistema.

#### Taxonomía final de la literatura

La literatura se organiza en cuatro familias.

#### Familia A: sharded ledgers y cross-shard commit

Trabajos principales:

- S06 Chainspace;
- S07 OmniLedger;
- S08 Prophet;
- S09 CSLAP;
- S10 Presto;
- S11 LightCross.

Síntesis:

Chainspace y OmniLedger establecen antecedentes seminales de atomic cross-shard processing mediante S-BAC y Atomix. Prophet reduce conflictos mediante deterministic ordering. CSLAP mantiene el uso de 2PC y agrega leader accountability. Presto optimiza confirmation latency, aunque su full text no fue recuperado durante esta revisión. LightCross propone ejecución cross-shard asistida por TEE y presenta teoremas de atomicity, serializability y liveness.

Estos trabajos demuestran que la literatura cross-shard ya contiene mecanismos explícitos de atomic commit y argumentos de correctitud. No debe afirmarse que los trabajos previos carezcan de verificación o de garantías de protocolo.

La diferencia relevante es el tipo de evidencia. En el conjunto verificado, los trabajos cross-shard core emplean principalmente argumentos informales, teoremas analíticos y evaluación de rendimiento del protocolo. No se observó en esos comparadores un workflow que combine TLA+, Alloy, mutation-based property validation, implementation-model trace conformance y verification-cost characterization.

#### Familia B: formal verification y validación multi-formalismo

Trabajos principales:

- S01 MongoDB distributed transactions;
- S04 Mutation Model Checking;
- S05 Ethereum 3SF.

Síntesis:

MongoDB 2025 es el comparador de mayor proximidad por combinar distributed cross-shard transactions, TLA+, TLC y model-based testing de una implementación real.

Mutation Model Checking 2023 demuestra que el uso de mutantes para evaluar la fuerza de especificaciones formales ya existe como línea metodológica independiente.

Ethereum 3SF 2025 es crítico para evitar overclaims. Ese trabajo combina TLA+, Alloy, deliberate bug injection y experimentos de tiempo, memoria y timeouts. Por tanto, DLT-Lab no puede atribuir novedad a la combinación aislada de múltiples formalismos, defect injection y medición de costo.

La diferencia potencial de DLT-Lab debe mantenerse vinculada al dominio cross-shard y a la integración de varias capas de evidencia bajo un protocolo experimental congelado.

#### Familia C: implementation-model conformance y testing

Trabajos principales:

- S02 SEFM trace validation;
- S03 TraceLink;
- S18 Smart Casual Verification of CCF;
- S19 Choreographic PlusCal;
- S21 Mocket;
- S22 iMocket;
- S23 Amazon S3 ShardStore.

Síntesis:

La conexión entre modelos formales y ejecutables está ampliamente establecida.

SEFM 2024 reduce trace validation a constrained model checking con TLC. TraceLink automatiza la relación entre implementaciones generadas y TLA+ y reporta bugs previamente no detectados. CCF integra TLA+, trace validation y CI alrededor de una implementación C++ de producción. Choreographic PlusCal genera monitors para comprobar conformance de implementaciones existentes. Mocket e iMocket utilizan el state space formal para dirigir testing de sistemas distribuidos. Amazon S3 demuestra que un workflow multi-técnica alrededor de modelos ejecutables tampoco constituye por sí solo una contribución novedosa.

En consecuencia, RQ3 debe posicionarse como bounded implementation-model trace conformance dentro de una cadena cross-shard de evidencia, no como una nueva técnica general de trace validation.

#### Familia D: reproducibilidad y artefactos

La matriz muestra que artifact availability y reproducibility evidence existen en literatura relacionada.

TraceLink publica código, datos y scripts y reporta reproducción externa parcial. Mocket dispone de artifact availability. CSLAP publica código. Otros trabajos publican implementaciones o repositorios sin demostrar una reproducción independiente equivalente a la campaña de DLT-Lab.

La contribución de DLT-Lab no debe reducirse a publicar código o hashes. Su argumento de reproducibilidad se apoya en la relación entre source revision, protocolo experimental congelado, resultados raw preservados, regeneración completa del análisis, hashes de contenido y una reproducción ejecutada desde un clon y workspace independientes.

#### Matriz de posicionamiento conceptual

| Dimensión | Evidencia previa verificada | Posición de DLT-Lab |
| --- | --- | --- |
| Cross-shard atomic commit | Chainspace, OmniLedger, CSLAP, LightCross y MongoDB cubren atomicidad o commit distribuido | No es una novedad aislada |
| TLA+ para sistemas o transacciones distribuidas | MongoDB, SEFM, TraceLink, CCF, Choreographic PlusCal, Mocket e iMocket | No es una novedad aislada |
| Alloy junto con TLA+ | Ethereum 3SF combina ambos formalismos | No es una novedad aislada |
| Mutation-based specification validation | Mutation Model Checking establece el enfoque; Ethereum 3SF usa deliberate bugs | No es una novedad aislada |
| Implementation-model validation | SEFM, TraceLink, CCF, Choreographic PlusCal, Mocket e iMocket cubren distintas variantes | RQ3 debe presentarse como aplicación acotada e integrada |
| Medición de costo de herramientas de validación | Ethereum 3SF, TraceLink, CCF, Mocket e iMocket reportan costos, tiempos u overheads | RQ4 aporta una caracterización específica bajo el protocolo congelado |
| Artefactos reproducibles | Varios comparadores publican código, datos o artefactos | La diferencia está en la cadena de regeneración e integridad |
| Integración completa en un estudio cross-shard | No observada en los 15 comparadores con full text verificado | Gap provisional defendible y acotado |

#### Gap defendible

La formulación más fuerte permitida por la evidencia actual es:

En el conjunto de comparadores con full text verificado, no se identificó un estudio cross-shard que integre simultáneamente bounded property verification con TLA+ y Alloy, targeted mutation-based property validation, bounded implementation-model trace conformance con valid and deliberately corrupted traces, verification-cost characterization bajo un protocolo experimental congelado y reproducción independiente de los artefactos analíticos.

Esta formulación está deliberadamente acotada al conjunto verificado.

No implica que no exista ningún trabajo fuera de la búsqueda realizada.

No autoriza utilizar `the first`.

#### Jerarquía de comparadores directos

Prioridad 1:

- S01 MongoDB, por dominio cross-shard más TLA+ más implementation testing;
- S05 Ethereum 3SF, por TLA+ más Alloy más deliberate bugs más verification cost;
- S03 TraceLink, por implementation-model trace validation más artefacto y reproducción parcial;
- S04 Mutation Model Checking, por validation de especificaciones mediante mutantes.

Prioridad 2:

- S06 Chainspace;
- S07 OmniLedger;
- S09 CSLAP;
- S11 LightCross;
- S18 CCF;
- S19 Choreographic PlusCal;
- S21 Mocket.

Prioridad 3:

- S08 Prophet;
- S22 iMocket;
- S23 Amazon S3.

Supporting only:

- S10 Presto;
- S20 ZooKeeper runtime trace-checking.

#### Claims permitidos para G7

Se permiten formulaciones del tipo:

- `In the verified comparator set, prior cross-shard studies primarily focus on protocol design, correctness arguments, and system performance.`
- `Implementation-model validation with TLA+ is well established in distributed systems.`
- `Mutation-based assessment of formal specifications predates this work.`
- `Multi-formalism validation using TLA+ and Alloy has also been explored in blockchain verification.`
- `Our contribution lies in evaluating these evidence layers together for an executable cross-shard protocol under one frozen experimental protocol.`

Las frases anteriores son guías conceptuales. G7 debe redactarlas en inglés académico y asociarlas a citas concretas.

#### Claims prohibidos para G7

No utilizar:

- `the first`;
- `the only`;
- `no prior work`;
- `previous work does not verify cross-shard protocols`;
- `no prior work combines formal methods and implementation testing`;
- `TLA+ and Alloy have not been combined before`;
- `mutation testing has not been used for formal specifications`;
- comparaciones absolutas de rendimiento entre TLC y Alloy;
- comparaciones entre throughput del protocolo y verification cost;
- afirmaciones sustantivas apoyadas en S10 o S20.

#### Regla para `to our knowledge`

`To our knowledge` no debe aparecer en el primer borrador de G7.

Solo puede incorporarse después de 8F-G8 si:

- la frase está acotada al tipo exacto de integración estudiada;
- los integration probes fueron revisados;
- no existe un comparador directo omitido;
- la frase mejora realmente el posicionamiento editorial.

La preferencia es evitarla si una formulación comparativa puede expresar la contribución sin claim de prioridad.

#### Estructura fijada para Background and Related Work

La sección futura debe tener cuatro bloques.

Bloque 1:

`Sharded ledgers and cross-shard commit`

Papers principales:

S06, S07, S08, S09, S11.

S10 puede mencionarse solo como contexto secundario.

Objetivo:

Establecer que atomic cross-shard processing y garantías de protocolo ya son problemas maduros y separar system performance de formal verification.

Bloque 2:

`Formal analysis of distributed and blockchain protocols`

Papers principales:

S01, S05 y S04.

Objetivo:

Mostrar antecedentes de TLA+, multi-formalism validation, mutation-based specification validation y límites computacionales del model checking.

Bloque 3:

`Specification validation and implementation-model conformance`

Papers principales:

S02, S03, S18, S19, S21, S22 y S23.

Objetivo:

Establecer que la conexión implementation-model es un área madura y posicionar RQ3 como una instancia cross-shard acotada dentro de una cadena mayor de evidencia.

Bloque 4:

`Positioning of this work`

Objetivo:

Comparar explícitamente la cobertura parcial de los trabajos anteriores con el workflow integrado de DLT-Lab, sin afirmar prioridad absoluta.

#### Regla de citación para la redacción

Cada párrafo comparativo debe cumplir:

- una afirmación técnica debe poder rastrearse a una fuente primaria verificada;
- S10 y S20 no respaldan afirmaciones técnicas fuertes;
- las ausencias se redactan como `not reported or evaluated in the cited work` cuando no puedan demostrarse como inexistencia;
- los papers cross-shard con teoremas no deben describirse como trabajos sin verificación;
- la palabra `formal` debe reservarse con precisión para el tipo de método que realmente utiliza cada trabajo.

#### Decisión de parada bibliográfica

La búsqueda general queda cerrada después de G6.

Solo se permite una nueva búsqueda si durante G7 aparece uno de estos casos:

- una cita necesaria no tiene fuente primaria verificable;
- un claim depende de una dimensión marcada como `unclear`;
- se detecta un comparador directo que podría invalidar el gap;
- el editor o reviewer exige un antecedente específico.

No se ampliará la literatura únicamente para aumentar el número de referencias.

#### Gate de 8F-G6

8F-G6 se considera cerrado cuando:

- `RELATED_WORK_MATRIX.csv` contiene 17 trabajos ordenados por ID;
- la matriz incorpora tipo de evidencia, alcance del costo y rol de posicionamiento;
- `RELATED_WORK_SYNTHESIS.md` existe;
- el gap está acotado al conjunto de comparadores verificados;
- existen listas explícitas de claims permitidos y prohibidos;
- la estructura de cuatro bloques para Related Work está fijada;
- la búsqueda general se declara cerrada;
- `git diff --check` pasa;
- el diff permanece dentro de `manuscript/paper1/`.

#### Siguiente fase

Después de cerrar G6:

`8F-G7 references.bib + Background and Related Work drafting`

G7 construirá bibliografía verificable y redactará la sección en inglés académico.
