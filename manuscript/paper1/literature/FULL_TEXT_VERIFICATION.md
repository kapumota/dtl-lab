### Verificacion de texto completo de seeds P1

#### Identificacion

Paper:

`DTL-Lab Paper 1`

Fase:

`8F-G3`

Fecha:

`2026-09-07`

Objetivo:

Verificar contra texto completo los cinco seeds P1 definidos en 8F-G2, extraer dimensiones comparables con DTL-Lab y registrar diferencias sin redactar todavia Background and Related Work.

#### Dependencia con la fase anterior

Esta fase consume:

`manuscript/paper1/literature/SEARCH_PROTOCOL.md`

`manuscript/paper1/literature/SEARCH_LOG.md`

`manuscript/paper1/literature/SEED_PAPERS.md`

La fase no modifica Java, TLA+, Alloy, resultados raw, protocolo experimental ni scripts cientificos.

#### S01 MongoDB distributed transactions

Paper:

`Design and Modular Verification of Distributed Transactions in MongoDB`

Autores:

William Schultz, Murat Demirbas.

Venue:

Proceedings of the VLDB Endowment, 2025.

DOI:

`10.14778/3750601.3750626`

Estado de fuente:

`primary_source_verified: yes`

Hallazgos verificados:

- el sistema estudiado implementa distributed transactions sobre multiples shards;
- el protocolo utiliza un esquema basado en two-phase commit para atomic commit entre shards;
- el paper modela el protocolo en TLA+;
- TLC se utiliza para verificar propiedades de aislamiento y analizar permissiveness;
- el paper separa formalmente el protocolo distribuido del modelo abstracto de la capa de almacenamiento;
- TLC genera casos de prueba a partir del grafo de estados alcanzables del modelo de almacenamiento;
- esos casos se ejecutan contra WiredTiger para comprobar conformance con la interfaz formal;
- el paper publica un artefacto con especificaciones y codigo de model-based testing;
- el articulo reporta estados, profundidad y tiempo para configuraciones verificadas;
- la evaluacion de model-based testing no cubre clientes concurrentes en el enfoque publicado.

Comparacion con DTL-Lab:

MongoDB 2025 es el comparador mas cercano por dominio y por vinculacion entre modelo e implementacion.

Coincidencias:

- transacciones distribuidas entre shards;
- atomic commit basado en 2PC;
- TLA+;
- TLC;
- verificacion acotada;
- implementacion ejecutable;
- comprobacion de conformance entre una interfaz formal y una implementacion;
- artefacto publico.

Diferencias verificadas:

- no usa Alloy;
- no presenta un catalogo de mutantes cientificos comparable a RQ2;
- su comprobacion de implementacion se basa en test-case generation desde el modelo de storage, no en el corpus de valid and deliberately corrupted traces usado por DTL-Lab;
- no estructura una pregunta de investigacion equivalente a verification-cost characterization;
- no se identifico una reproduccion independiente del artefacto equivalente a la Fase 8E.

Implicacion:

DLT-Lab no puede reclamar novedad por usar TLA+ para transacciones cross-shard ni por vincular un modelo formal con una implementacion.

#### S02 SEFM trace validation

Paper:

`Validating Traces of Distributed Programs Against TLA+ Specifications`

Autores:

Horatiu Cirstea, Markus A. Kuppe, Benjamin Loillier, Stephan Merz.

Venue:

SEFM 2024.

DOI:

`10.1007/978-3-031-77382-2_8`

Estado de fuente:

`primary_source_verified: yes`

Hallazgos verificados:

- reduce trace validation a constrained model checking con TLC;
- proporciona una API para instrumentar programas Java;
- relaciona eventos y actualizaciones observadas con variables y acciones TLA+;
- permite trazas parciales en las que no todas las variables o eventos son observados;
- TLC puede reconstruir informacion omitida mediante exploracion del modelo;
- el running example utiliza un protocolo Two-Phase Commit con implementacion Java;
- el enfoque fue aplicado tambien a otros sistemas distribuidos;
- los autores declaran que el metodo no proporciona una garantia formal completa de correccion;
- existe un tradeoff entre detalle de la traza, tamano de busqueda y fiabilidad del verdict;
- una traza con informacion insuficiente puede ser aceptada incorrectamente;
- el trabajo reporta adopcion posterior en CCF y etcd;
- existe una biblioteca publica de trace validation para Java y TLA+.

Comparacion con DTL-Lab:

Coincidencias:

- Java;
- TLA+;
- TLC;
- two-phase commit como caso de estudio;
- instrumentacion;
- replay o validacion de ejecuciones concretas contra una especificacion;
- deteccion de discrepancias entre modelo e implementacion.

Diferencias verificadas:

- el paper es una metodologia general de trace validation y no un estudio cross-shard de blockchain;
- no usa Alloy;
- no incorpora mutation-based property validation como capa experimental;
- no utiliza el mismo diseno de valid versus deliberately corrupted trace classes de DTL-Lab;
- no reporta un protocolo experimental congelado equivalente;
- no reporta una reproduccion independiente equivalente a Fase 8E.

Implicacion:

DLT-Lab no debe presentar la idea de validar trazas Java contra TLA+ como una contribucion novedosa por si misma.

#### S03 TraceLink

Paper:

`TraceLinking Implementations with Their Verified Designs`

Autores:

Finn Hackett, Ivan Beschastnikh.

Venue:

Proceedings of the ACM on Programming Languages, OOPSLA2 2025.

DOI:

`10.1145/3763128`

Estado de fuente:

`primary_source_verified: yes`

Hallazgos verificados:

- TraceLink automatiza la relacion entre trazas de implementacion y modelos TLA+;
- la implementacion se genera mediante PGo a partir de Modular PlusCal;
- el sistema instrumenta eventos y cambios de estado de forma general;
- el paper define estrategias one-path, all-paths y one-path-sidestep;
- se evaluaron tres sistemas distribuidos;
- se encontraron nueve bugs previamente no detectados;
- la evaluacion incluye rendimiento de model checking y overhead de instrumentacion;
- una ejecucion all-paths para raftkvs fue terminada despues de superar 12 horas;
- el paper publica codigo, datos y scripts;
- el paquete de evaluacion contiene aproximadamente 400 GB de logs y counterexamples;
- los autores indican que Antithesis reprodujo parte de los experimentos;
- el paper declara una restriccion de generalizacion a configuraciones MPCal compatibles con sus assumptions de instanciacion.

Comparacion con DTL-Lab:

Coincidencias:

- TLA+;
- TLC;
- trace validation;
- implementacion distribuida ejecutable;
- mediciones asociadas al costo del proceso de validacion;
- artefacto reproducible.

Diferencias verificadas:

- TraceLink depende de PGo y de implementaciones generadas desde MPCal;
- DTL-Lab parte de una implementacion Java independiente del modelo;
- TraceLink no estudia transacciones cross-shard;
- no usa Alloy;
- no presenta mutation-based validation de la property suite;
- su objetivo central es detectar divergencias entre implementacion compilada y diseno formal;
- su reproduccion externa es parcial y no equivale al protocolo de regeneracion analitica de DTL-Lab.

Implicacion:

La contribucion de RQ3 debe posicionarse como una aplicacion acotada y experimental de implementation-model conformance dentro de una cadena de evidencia cross-shard, no como una nueva tecnica general de trace validation.

#### S04 Mutation Model Checking

Paper:

`Towards Strengthening Formal Specifications with Mutation Model Checking`

Autores:

Maxime Cordy, Sami Lazreg, Axel Legay, Pierre Yves Schobbens.

Venue:

ESEC/FSE 2023.

DOI:

`10.1145/3611643.3613080`

Estado de fuente:

`primary_source_verified: yes`

Hallazgos verificados:

- propone mutation model checking para evaluar la fuerza de especificaciones formales;
- los modelos se mutan sintacticamente aplicando operadores predefinidos;
- los experimentos usan modelos Promela y propiedades LTL;
- se estudian Minepump y ADAPRO;
- se generan 88 mutantes para Minepump y 406 para ADAPRO;
- el mutation score se utiliza como proxy de strength de la especificacion;
- los scores totales observados son aproximadamente 60 por ciento y 40 por ciento;
- el objetivo propuesto va mas alla de medir sensibilidad y busca sintetizar nuevas formulas que fortalezcan la especificacion;
- el paper es un NIER de cinco paginas y describe resultados preliminares y un framework aun no completamente operacional.

Comparacion con DTL-Lab:

Coincidencias:

- mutantes de modelos formales;
- model checking;
- mutation score;
- uso de violaciones para evaluar fuerza o sensibilidad de propiedades.

Diferencias verificadas:

- no estudia blockchain ni cross-shard transactions;
- no usa TLA+ ni Alloy en sus experimentos;
- genera mutantes sintacticos de forma amplia, mientras DTL-Lab usa un catalogo pequeno de mutantes cientificos dirigidos a obligaciones del protocolo;
- su objetivo es fortalecer automaticamente especificaciones;
- DTL-Lab no sintetiza nuevas propiedades;
- DTL-Lab repite cada mutant-tool-configuration run bajo un protocolo experimental congelado.

Implicacion:

DLT-Lab puede reclamar aplicacion y articulacion experimental de mutation-based property validation en su dominio, pero no la idea general de usar mutantes para evaluar especificaciones formales.

#### S05 Ethereum 3SF

Paper:

`Technical Report: Exploring Automatic Model-Checking of the Ethereum specification`

Autores:

Igor Konnov, Jure Kukovec, Thomas Pani, Roberto Saltini, Thanh Hai Tran.

Ano:

2025.

Tipo:

Technical report.

Identificador:

`arXiv:2501.07958`

Estado de fuente:

`primary_source_verified: yes`

Estado editorial:

`peer_reviewed: no confirmado`

Hallazgos verificados:

- parte de una especificacion ejecutable de 3SF escrita en Python;
- realiza una traduccion manual hacia TLA+;
- utiliza Apalache para model checking;
- desarrolla varias capas de abstraccion para controlar la complejidad combinatoria;
- desarrolla una codificacion SMT;
- desarrolla una especificacion Alloy para cross-validation;
- ejecuta experimentos con distintos tamanos y registra tiempo y memoria;
- reporta timeouts en configuraciones mas exigentes;
- verifica AccountableSafety de forma exhaustiva solo para instancias pequenas;
- en configuraciones con bugs deliberadamente introducidos, descritos por los autores como analogos a mutation testing, Apalache, Alloy y CVC5 generan contraejemplos;
- publica especificaciones, scripts y resultados experimentales en un repositorio publico;
- los autores reconocen variacion de runtime y proponen multiples repeticiones como extension.

Comparacion con DTL-Lab:

Coincidencias:

- blockchain;
- TLA+;
- Alloy;
- model checking acotado;
- deliberate bug injection;
- evaluacion de costo de verificacion;
- timeouts;
- artefacto publico;
- conexion inicial con una especificacion ejecutable.

Diferencias verificadas:

- el objeto de estudio es consensus 3SF, no cross-shard transaction commit;
- TLA+ usa Apalache, no TLC como herramienta principal;
- la relacion con Python es una traduccion manual de especificacion, no implementation-model trace conformance;
- la inyeccion de bugs no esta organizada como un catalogo de mutation-based property validation equivalente a RQ2;
- no se identifico una reproduccion independiente equivalente a Fase 8E;
- el trabajo es un technical report y debe distinguirse de los comparadores peer-reviewed.

Implicacion:

Este seed elimina cualquier intento de fundamentar la novedad de DTL-Lab solo en la combinacion TLA+ mas Alloy mas defect injection mas verification-cost measurements.

#### Resultado provisional de la verificacion P1

Los cinco seeds confirman que varias combinaciones parciales ya existen.

Combinaciones ya cubiertas por literatura:

- cross-shard transactions + TLA+ + implementation conformance;
- TLA+ + Java trace validation;
- automated TLA+ trace validation + reproducibility artifact;
- model checking + mutation-based specification assessment;
- blockchain + TLA+ + Alloy + deliberate defects + verification-cost experiments.

Hipotesis de gap que sobrevive:

La diferencia potencial de DTL-Lab debe buscarse en la integracion, dentro de un mismo estudio cross-shard, de:

- bounded property verification;
- dos formalismos complementarios TLA+ y Alloy;
- mutation-based property validation;
- bounded implementation-model trace conformance;
- verification-cost characterization;
- protocolo experimental congelado;
- analisis reproducible;
- reproduccion independiente de los artefactos analiticos.

Esta hipotesis sigue siendo provisional y debe continuar sometida a snowballing.

#### Snowballing inicial

El backward y forward snowballing de los P1 produjo nuevos candidatos de alta prioridad:

- Smart Casual Verification of the Confidential Consortium Framework, NSDI 2025;
- Protocol Conformance with Choreographic PlusCal, TASE 2023;
- Verifying Zookeeper based on Model-Based runtime Trace-Checking using TLA+, ICCSIE 2022;
- Model Checking Guided Testing for Distributed Systems, EuroSys 2023;
- Model Checking Guided Incremental Testing for Distributed Systems, ISSTA 2025;
- Using Lightweight Formal Methods to Validate a Key-Value Storage Node in Amazon S3, SOSP 2021.

Estos candidatos se incorporan a `SEED_PAPERS.md` para la siguiente ronda.

#### Cierre de 8F-G3

8F-G3 puede cerrarse cuando:

- los cinco P1 tienen `primary_source_verified: yes`;
- existe una matriz inicial con los cinco comparadores;
- el snowballing produce una lista versionada de candidatos;
- no se formula ningun claim de prioridad;
- `git diff --check` pasa;
- el diff permanece limitado a `manuscript/paper1/`.

### Screening de texto completo de seeds S18 a S23

#### Identificacion

Paper:

`DTL-Lab Paper 1`

Fase:

`8F-G4`

Fecha:

`2026-09-07`

Objetivo:

Verificar los seeds obtenidos por snowballing en 8F-G3, ampliar la matriz comparativa y volver a intentar refutar el gap metodologico antes de redactar Background and Related Work.

#### Dependencia con la fase anterior

Esta fase consume:

`manuscript/paper1/literature/FULL_TEXT_VERIFICATION.md`

`manuscript/paper1/literature/RELATED_WORK_MATRIX.csv`

`manuscript/paper1/literature/SEARCH_LOG.md`

`manuscript/paper1/literature/SEED_PAPERS.md`

Antes de aplicar el parche deben existir los registros S01 a S05 y los seeds S18 a S23.

La fase no modifica Java, TLA+, Alloy, resultados raw, protocolo experimental ni scripts cientificos.

#### S18 Smart Casual Verification of the Confidential Consortium Framework

Paper:

`Smart Casual Verification of the Confidential Consortium Framework`

Autores:

Heidi Howard, Markus A. Kuppe, Edward Ashton, Amaury Chamayou, Natacha Crooks.

Ano:

2025.

Venue:

22nd USENIX Symposium on Networked Systems Design and Implementation, NSDI 2025.

Estado de fuente:

`primary_source_verified: yes`

Hallazgos verificados:

- estudia CCF, un sistema distribuido de produccion que soporta Azure Confidential Ledger;
- combina especificaciones TLA+, model checking, simulation, automated testing y trace validation;
- enlaza las especificaciones TLA+ con una implementacion C++;
- integra la validacion dentro del pipeline CI de CCF;
- trace validation comprueba si una traza observada de implementacion corresponde a un comportamiento permitido por la especificacion;
- el uso de DFS en TLC redujo en un caso la validacion de aproximadamente una hora a menos de un segundo;
- el desarrollo inicial de trace validation para consensus requirio aproximadamente dos engineer-months distribuidos en cuatro meses;
- el trabajo de consistency trace validation posterior requirio aproximadamente una engineer-week;
- una exploracion exhaustiva de model checking requirio 48 horas sobre una maquina de 128 cores;
- el workflow encontro seis bugs sutiles de safety y liveness antes de produccion.

Comparacion con DTL-Lab:

Coincidencias:

- TLA+;
- TLC;
- bounded model checking;
- implementation-model trace validation;
- implementacion ejecutable independiente de la especificacion;
- mediciones de costo y esfuerzo de verificacion;
- workflow reproducible a nivel de codigo y CI.

Diferencias verificadas:

- el dominio principal es consensus y client consistency, no cross-shard transaction commit;
- no usa Alloy;
- no presenta mutation-based property validation como capa experimental;
- no usa el diseno multiseed de valid and deliberately corrupted traces de DTL-Lab;
- no se identifico una reproduccion independiente comparable a Fase 8E.

Implicacion:

DLT-Lab no puede reclamar novedad por integrar TLA+, trace validation y CI alrededor de una implementacion distribuida. La diferencia potencial debe apoyarse en el dominio cross-shard y en la combinacion completa de capas experimentales.

Estado:

`include-direct`

#### S19 Protocol Conformance with Choreographic PlusCal

Paper:

`Protocol Conformance with Choreographic PlusCal`

Autores:

Darius Foo, Andreea Costea, Wei-Ngan Chin.

Ano:

2023.

Venue:

Theoretical Aspects of Software Engineering, TASE 2023.

DOI:

`10.1007/978-3-031-35257-7_8`

Estado de fuente:

`primary_source_verified: yes`

Hallazgos verificados:

- extiende PlusCal para expresar protocolos distribuidos como choreographies;
- proyecta las choreographies hacia PlusCal y TLA+;
- genera monitors en el lenguaje de implementacion a partir del modelo;
- utiliza el protocolo clasico Two-Phase Commit como ejemplo principal;
- el monitor comprueba comportamientos concretos contra la formula temporal del modelo;
- como estrategia de testing, el monitoring es sound pero incomplete;
- una assertion que falla indica una violacion de refinement para la ejecucion observada, pero una assertion nunca ejecutada no permite concluir correccion;
- la evaluacion instrumenta una implementacion Go de 2PC y etcd;
- para la implementacion 2PC de 3032 LoC reporta 19 por ciento de overhead, aproximadamente 5 ms;
- para etcd reporta 2 por ciento de overhead, aproximadamente 4 ms;
- los autores consideran el overhead suficientemente bajo para randomized testing, CI y potencialmente produccion;
- existe codigo asociado publicado por los autores.

Comparacion con DTL-Lab:

Coincidencias:

- TLA+ y TLC;
- 2PC;
- implementation-model conformance;
- ejecuciones concretas contra un modelo formal;
- implementacion no generada desde el modelo;
- evaluacion de overhead.

Diferencias verificadas:

- no es un estudio de blockchain ni de cross-shard transactions;
- no usa Alloy;
- no presenta mutation-based validation;
- no utiliza un corpus experimental de valid and corrupted traces equivalente a RQ3;
- no presenta una reproduccion independiente comparable a Fase 8E.

Implicacion:

La idea de protocol conformance entre un modelo TLA+ y una implementacion existente tampoco es nueva. DTL-Lab debe presentar RQ3 como una capa especifica de evidencia dentro del estudio cross-shard.

Estado:

`include-direct`

#### S20 Verifying Zookeeper based on Model-Based runtime Trace-Checking using TLA+

Paper:

`Verifying Zookeeper based on Model-Based runtime Trace-Checking using TLA+`

Autores:

Zhi Niu, Luming Dong, Yong Zhu, Li Chen.

Ano:

2022.

Venue:

ICCSIE 2022.

DOI:

`10.1145/3558819.3558822`

Estado de fuente:

`primary_source_verified: no`

Evidencia disponible:

- metadata bibliografica verificada mediante DBLP, DOI y perfil de autor;
- el trabajo es citado por literatura posterior de trace validation;
- el titulo y metadata indican una aplicacion de runtime trace-checking con TLA+ a ZooKeeper.

Limitacion:

No se obtuvo un full text primario accesible durante 8F-G4.

Regla de uso:

- no usar este paper para una comparacion sustantiva;
- no inferir detalles metodologicos ausentes;
- conservarlo para snowballing y contexto;
- sustituirlo por S21 u otra fuente primaria cuando se necesite respaldar un claim sobre implementation-model testing.

Estado:

`include-supporting`

#### S21 Model Checking Guided Testing for Distributed Systems

Paper:

`Model Checking Guided Testing for Distributed Systems`

Autores:

Dong Wang, Wensheng Dou, Yu Gao, Chenao Wu, Jun Wei, Tao Huang.

Ano:

2023.

Venue:

EuroSys 2023.

DOI:

`10.1145/3552326.3587442`

Estado de fuente:

`primary_source_verified: yes`

Hallazgos verificados:

- propone Mocket para usar el state space generado por model checking como guia de testing de una implementacion distribuida;
- parte de una especificacion TLA+ verificada;
- mapea variables y acciones TLA+ con el codigo Java;
- genera test cases a partir de estados y transiciones verificados;
- fuerza deterministamente la implementacion a seguir las secuencias abstractas seleccionadas;
- compara estados runtime con estados verificados del modelo;
- una divergence se trata como potencial inconsistencia entre specification e implementation;
- se evalua en Xraft, Raft-java y ZooKeeper;
- reporta siete implementation bugs, tres de ellos previamente desconocidos, y dos specification bugs adicionales en la especificacion oficial de Raft;
- para ZooKeeper, desarrollar la especificacion TLA+ y el mapping requirio aproximadamente tres semanas;
- Mocket obtuvo el badge `Artifacts Available` de EuroSys 2023;
- el artifact DOI es `10.5281/zenodo.7654817`;
- no obtuvo el badge `Results Reproduced` en la evaluacion de EuroSys 2023.

Limitaciones verificadas:

- requiere una especificacion TLA+ suficientemente cercana a la implementacion;
- el nivel de abstraccion afecta la capacidad de detectar bugs;
- el mapping y annotations requieren trabajo manual;
- la cobertura esta limitada a los estados y acciones representados en el modelo.

Comparacion con DTL-Lab:

Coincidencias:

- TLA+;
- TLC/model checking;
- Java;
- mapping entre modelo e implementacion;
- testing sistematico de implementacion basado en evidencia formal;
- artefacto publico.

Diferencias verificadas:

- Mocket genera tests desde el state space formal y controla el scheduling de la implementacion;
- DTL-Lab evalua un catalogo congelado de valid and deliberately corrupted traces y exige diagnostic agreement en los casos negativos;
- Mocket no estudia cross-shard blockchain;
- no usa Alloy;
- no presenta mutation-based property validation;
- no presenta una reproduccion independiente equivalente a Fase 8E.

Implicacion:

Mocket es una tecnica implementation-facing mas general y sistematica que el replay de trazas de DTL-Lab. La contribucion de DTL-Lab no debe formular RQ3 como superioridad metodologica, sino como integracion y evaluacion controlada dentro del caso cross-shard.

Estado:

`include-direct`

#### S22 Model Checking Guided Incremental Testing for Distributed Systems

Paper:

`Model Checking Guided Incremental Testing for Distributed Systems`

Autores:

Yu Gao, Dong Wang, Wensheng Dou, Wenhan Feng, Yu Liang, Yuan Feng, Jun Wei.

Ano:

2025.

Venue:

Proceedings of the ACM on Software Engineering, ISSTA 2025.

DOI:

`10.1145/3728883`

Estado de fuente:

`primary_source_verified: yes`

Hallazgos verificados:

- iMocket extiende Mocket para cambios incrementales en sistemas distribuidos;
- identifica las partes del state space afectadas por cambios en specification o implementation;
- genera tests centrados en los estados y transiciones potencialmente afectados;
- evalua doce escenarios de cambio en Raft-java, Xraft y ZooKeeper;
- reduce el numero de test cases en 74.83 por ciento en promedio;
- reduce testing time entre 22.54 y 99.99 por ciento;
- 68.91 a 100 por ciento de los tests generados en escenarios de bug fixing disparan el bug correspondiente;
- el promedio reportado de bug-triggering tests es 92.1 por ciento;
- la evaluacion reconoce que Mocket puede requerir de cinco dias a mas de tres semanas y que iMocket puede requerir de 30 minutos a 15 dias.

Limitaciones verificadas:

- no detecta automaticamente algunos cambios fuera de mapped actions;
- no detecta automaticamente cambios de concurrencia entre acciones;
- depende de una especificacion formal verificada;
- el costo de generar y procesar grandes state graphs limita su uso per-commit;
- la evaluacion se limita a tres sistemas y doce escenarios.

Comparacion con DTL-Lab:

iMocket es relevante principalmente como evolucion del estado del arte de implementation-model testing. No es un comparador directo del dominio cross-shard y no agrega una combinacion de Alloy, mutation-based property validation o independent reproduction comparable a DTL-Lab.

Estado:

`include-core`

#### S23 Using Lightweight Formal Methods to Validate a Key-Value Storage Node in Amazon S3

Paper:

`Using Lightweight Formal Methods to Validate a Key-Value Storage Node in Amazon S3`

Autores:

James Bornholt, Rajeev Joshi, Vytautas Astrauskas, Brendan Cully, Bernhard Kragl, Seth Markle, Kyle Sauri, Drew Schleit, Grant Slatton, Serdar Tasiran, Jacob Van Geffen, Andrew Warfield.

Ano:

2021.

Venue:

SOSP 2021.

DOI:

`10.1145/3477132.3483540`

Estado de fuente:

`primary_source_verified: yes`

Hallazgos verificados:

- estudia ShardStore, un key-value storage node de Amazon S3;
- adopta lightweight formal methods orientados a una implementacion de produccion en evolucion;
- no busca full formal verification;
- descompone correctness en propiedades independientes y selecciona una herramienta apropiada para cada una;
- utiliza executable reference models como especificaciones;
- compara estados de la implementacion y del modelo;
- usa property-based testing para functional correctness;
- combina property-based testing con failure injection para crash consistency;
- usa stateless model checking para concurrent linearizability;
- reporta que el workflow evito que 16 issues alcanzaran produccion;
- el metodo fue extendido por ingenieros que no eran especialistas en formal methods.

Comparacion con DTL-Lab:

Coincidencias:

- workflow multi-tecnica;
- implementacion ejecutable;
- especificaciones ejecutables o modelos de referencia;
- validacion de implementation against model;
- enfoque pragmatico y reproducible de software validation.

Diferencias verificadas:

- no es un protocolo cross-shard de blockchain;
- el termino ShardStore corresponde al storage node y no a blockchain sharding;
- no usa TLA+ ni Alloy como formalismos principales del estudio;
- no usa mutation model checking para validar una property suite;
- no usa trace conformance como constructo experimental equivalente a RQ3;
- no presenta una caracterizacion de model-checking cost equivalente a RQ4;
- no se identifico una reproduccion independiente comparable a Fase 8E.

Implicacion:

DLT-Lab tampoco puede presentar como novedad general la idea de integrar multiples tecnicas de validacion alrededor de una implementacion. El gap debe mantenerse especifico al dominio, a las capas concretas y al diseno experimental.

Estado:

`include-direct`

#### Resultado de 8F-G4

El screening de S18 a S23 estrecha nuevamente el espacio de contribucion.

La literatura ya cubre de forma verificable:

- TLA+ + production implementation + trace validation + CI;
- TLA+ + 2PC + generated monitors + protocol conformance;
- TLA+ state-space exploration + systematic implementation testing;
- incremental model-checking-guided implementation testing;
- multi-tool lightweight validation de una implementacion productiva;
- artefactos publicos para algunas tecnicas de implementation-model testing.

No se identifico en S18 a S23 un trabajo que agregue al mismo estudio cross-shard todas estas dimensiones:

- TLA+ y Alloy complementarios;
- targeted scientific mutation validation;
- valid and deliberately corrupted implementation traces con diagnostic agreement;
- verification-cost characterization bajo un protocolo experimental congelado;
- regeneracion completa del analisis desde resultados raw preservados;
- reproduccion independiente del artefacto analitico.

Este resultado sigue sin autorizar un claim de prioridad absoluta.

#### Decision de parada para este snowballing

El segundo conjunto de comparadores metodologicos produjo trabajos fuertes, pero no produjo un nuevo comparador cross-shard que cubra la cadena completa.

Para cumplir el objetivo editorial con plazos ajustados:

- se cierra la expansion metodologica general despues de S18 a S23;
- se conserva S20 solo como supporting porque no se verifico su full text;
- no se ejecuta una tercera ronda general de snowballing;
- la siguiente fase debe consolidar la matriz, verificar los comparadores cross-shard core S06 a S11 y preparar la sintesis para Related Work.

#### Cierre de 8F-G4

8F-G4 se considera cerrado cuando:

- S18, S19, S21, S22 y S23 tienen full text verificado;
- S20 queda explicitamente marcado como no verificado a texto completo;
- `RELATED_WORK_MATRIX.csv` contiene S01 a S05 y S18 a S23;
- `SEARCH_LOG.md` registra la ronda G4;
- `SEED_PAPERS.md` registra el estado posterior al screening;
- no se formula ningun claim de prioridad;
- `git diff --check` pasa;
- el diff permanece dentro de `manuscript/paper1/`.
