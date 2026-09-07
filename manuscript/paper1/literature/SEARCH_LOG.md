### Registro de busqueda de literatura del Paper 1

#### Identificacion

Paper:

`DTL-Lab Paper 1`

Fase:

`8F-G2`

Fecha:

`2026-09-07`

Objetivo:

Registrar el discovery inicial ejecutado a partir de `SEARCH_PROTOCOL.md` y conservar las queries, fuentes, candidatos retenidos e incidencias antes del screening detallado.

#### Estado del protocolo

Protocolo usado:

`manuscript/paper1/literature/SEARCH_PROTOCOL.md`

Estado:

`FROZEN FOR 8F-G`

No se modificaron codigo cientifico, modelos formales, resultados raw, protocolo experimental ni scripts de analisis durante esta fase.

#### Herramientas

Herramientas usadas:

- busqueda academica web;
- paginas primarias de publishers;
- repositorios institucionales;
- DBLP cuando fue necesario para confirmar metadata;
- arXiv para preprints y technical reports.

Herramienta auxiliar intentada:

`Elicit`

Incidencia:

La integracion esta conectada, pero la cuenta actual no habilita acceso API desde ChatGPT. Esta limitacion no bloquea el protocolo porque Elicit es una herramienta auxiliar y no una fuente primaria.

#### Batch D1: discovery general por cluster

Fecha:

`2026-09-07`

Queries ejecutadas:

1. `"cross-shard transaction" atomic commit blockchain formal verification`
2. `"cross-shard" blockchain atomicity "two-phase commit"`
3. `"distributed transactions" TLA+ model checking MongoDB`
4. `blockchain TLA+ formal verification distributed transactions`
5. `"mutation model checking" formal specifications`
6. `"trace validation" TLA+ implementation distributed systems`
7. `"model-based verification" implementation TLA+ distributed systems`
8. `"artifact evaluation" formal methods reproducibility model checking`
9. `Ethereum 3SF TLA+ Alloy formal verification 2025`
10. `TraceLink OOPSLA 2025 TLA+ trace validation`
11. `cross-shard transaction protocol sharding blockchain Rivet OmniLedger Chainspace`
12. `cross-shard blockchain formal verification model checking TLA+ Alloy`

Filtros conceptuales:

- prioridad 2020 a 2026;
- excepcion para trabajos seminales;
- prioridad a fuentes peer-reviewed;
- preprints marcados explicitamente;
- busqueda de trabajos que cubran una o varias capas de DTL-Lab.

Conteo:

El motor usado no expone un conteo total estable y comparable para todas las queries. Se inspeccionaron los resultados de mayor relevancia visibles para cada query y se retuvieron candidatos verificables contra fuentes primarias o institucionales.

Candidatos retenidos:

- Design and Modular Verification of Distributed Transactions in MongoDB;
- Towards Strengthening Formal Specifications with Mutation Model Checking;
- Validating Traces of Distributed Programs Against TLA+ Specifications;
- TraceLinking Implementations with Their Verified Designs;
- Technical Report: Exploring Automatic Model-Checking of the Ethereum specification;
- Chainspace: A Sharded Smart Contracts Platform;
- OmniLedger: A Secure, Scale-Out, Decentralized Ledger via Sharding;
- Prophet: Conflict-Free Sharding Blockchain via Byzantine-Tolerant Deterministic Ordering;
- Cross shard leader accountability protocol based on two phase atomic commit;
- Presto: Optimizing Cross-Shard Transactions in Sharded Blockchain Architecture;
- LightCross: Sharding with Lightweight Cross-Shard Execution for Smart Contracts;
- Efficient Cross-Shard Transaction Execution in Sharded Blockchains;
- Reusable Formal Verification of DAG-Based Consensus Protocols;
- Understanding Inconsistency in Azure Cosmos DB with TLA+;
- Model Checking Guided Testing for Distributed Systems;
- Replicability of experimental tool evaluations in model-based software and systems engineering with MATLAB/Simulink.

#### Batch D2: metadata y comparadores directos

Fecha:

`2026-09-07`

Queries ejecutadas:

1. `"Design and Modular Verification of Distributed Transactions in MongoDB" DOI`
2. `"Chainspace: A Sharded Smart Contracts Platform" NDSS`
3. `"OmniLedger" secure scale-out decentralized ledger via sharding DOI`
4. `"Prophet: Conflict-Free Sharding Blockchain via Byzantine-Tolerant Deterministic Ordering" DOI`
5. `"Validating Traces of Distributed Programs Against TLA+ Specifications" DOI`
6. `"TraceLinking Implementations with Their Verified Designs" DOI`
7. `"Understanding Inconsistency in Azure Cosmos DB with TLA+" DOI`
8. `"Reusable Formal Verification of DAG-Based Consensus Protocols" DOI`
9. `"Model Checking Guided Testing for Distributed Systems" DOI`
10. `"Replicability of experimental tool evaluations in model-based software and systems engineering with MATLAB/Simulink" DOI`

Resultados principales:

- MongoDB 2025 fue confirmado como comparador directo por combinar distributed cross-shard transactions, TLA+ y model-based verification de una interfaz de implementacion.
- SEFM 2024 fue confirmado como trabajo central de trace validation contra TLA+ mediante constrained model checking.
- TraceLink OOPSLA2 2025 fue confirmado como trabajo central de trace validation automatizada.
- Mutation Model Checking ESEC/FSE 2023 fue confirmado como antecedente directo de validacion de propiedades mediante mutantes.
- Ethereum 3SF 2025 fue retenido como technical report por combinar TLA+, Apalache y una codificacion alternativa en Alloy para cross-validation.
- Prophet, CSLAP, Presto y LightCross fueron retenidos como comparadores recientes de procesamiento cross-shard.
- Chainspace y OmniLedger fueron retenidos como antecedentes seminales de commit atomico cross-shard.
- Rivet fue retenido como preprint relevante y debe mantenerse separado de trabajos peer-reviewed.
- Boll et al. fue retenido como trabajo de soporte para reproducibilidad de evaluaciones experimentales de herramientas.

#### Batch D3: implementation-model conformance

Fecha:

`2026-09-07`

Queries ejecutadas:

1. `"trace validation" distributed programs TLA+ implementation`
2. `"model checking guided testing" distributed systems`
3. `"Verifying Zookeeper" model-based runtime trace-checking TLA+`
4. `"Understanding Inconsistency in Azure Cosmos DB with TLA+"`

Candidatos retenidos:

- Cirstea et al., SEFM 2024;
- Hackett and Beschastnikh, OOPSLA2 2025;
- Wang et al., EuroSys 2023;
- Hackett, Rowe and Kuppe, ICSE-SEIP 2023.

Candidato pendiente de metadata completa:

- Verifying Zookeeper based on Model-Based runtime Trace-Checking using TLA+.

Decision:

El paper de ZooKeeper permanece como candidato de snowballing y no se promociona todavia a seed principal porque falta verificar metadata primaria completa y su comparabilidad con DTL-Lab.

#### Batch D4: reproducibilidad y artifact evaluation

Fecha:

`2026-09-07`

Queries ejecutadas:

1. `"artifact evaluation" formal methods reproducibility model checking`
2. `"reproducibility" "formal methods" artifact evaluation`
3. `"replicability" experimental tool evaluations model-based software`
4. `"Results Reproduced" artifact evaluation distributed systems`

Candidatos retenidos:

- Boll, Vieregg and Kehrer, replicability of experimental tool evaluations;
- PADS reproducibility and artifact evaluation guidelines como contexto;
- ACM-style artifact evaluation criteria como contexto metodologico.

Decision:

Los guidelines de artifact evaluation se clasifican como `include-context`, no como evidencia de novedad cientifica.

#### Resultado de integration probes iniciales

I1 Cross-shard plus formal verification:

Se identifico un comparador muy fuerte fuera de blockchain productiva: MongoDB 2025 formaliza distributed cross-shard transactions con TLA+ y realiza model-based verification de una interfaz de implementacion.

I2 Cross-shard plus mutation validation:

No se identifico en esta primera pasada un comparador directo que combine cross-shard transactions con mutation model checking de propiedades formales.

I3 Cross-shard plus implementation-model conformance:

MongoDB 2025 cubre una relacion implementation-model mediante model-based verification. No se identifico todavia un trabajo cross-shard blockchain que combine el mismo dominio con trace replay equivalente a DTL-Lab.

I4 Cross-shard plus verification cost:

Se identificaron trabajos cross-shard con evaluaciones de rendimiento del protocolo, pero no se confirmo todavia una caracterizacion explicita del costo del model checking comparable a RQ4.

I5 Cross-shard plus reproducibility:

No se identifico en esta primera pasada un trabajo cross-shard que documente una reproduccion independiente equivalente a la regeneracion de artefactos analiticos de DTL-Lab.

I6 Multi-layer formal validation:

MongoDB 2025 es el comparador de mayor prioridad porque integra varias capas. Ethereum 3SF 2025 tambien es metodologicamente cercano por usar multiples formalismos para cross-validation. Ninguno de estos hallazgos autoriza todavia un claim de prioridad para DTL-Lab.

#### Regla de interpretacion

Los resultados anteriores corresponden a discovery inicial.

No se permite concluir:

- que DTL-Lab sea el primer trabajo con estas tecnicas;
- que un trabajo no cubra una dimension sin verificar su full text;
- que la ausencia de un resultado en una query equivalga a ausencia en la literatura;
- que un preprint tenga el mismo peso que una publicacion peer-reviewed.

#### Siguiente accion

Usar `SEED_PAPERS.md` para priorizar lectura P1 y P2.

Despues:

- verificar full text de comparadores directos;
- ejecutar backward snowballing;
- ejecutar forward snowballing;
- ampliar discovery solo donde aparezcan huecos;
- construir `RELATED_WORK_MATRIX.csv` en la fase de screening detallado.

#### Batch G3: full-text verification y snowballing P1

Fecha:

`2026-09-07`

Seeds verificados:

- S01 MongoDB distributed transactions;
- S02 SEFM trace validation;
- S03 TraceLink;
- S04 Mutation Model Checking;
- S05 Ethereum 3SF.

Hallazgo principal:

La hipotesis de novedad tuvo que estrecharse. Ethereum 3SF combina TLA+, Alloy, deliberate bug injection y experimentos de costo de model checking. MongoDB 2025 combina cross-shard distributed transactions, TLA+ y conformance de implementacion. TraceLink agrega automatizacion avanzada y evidencia de reproduccion parcial.

Nuevos candidatos por snowballing:

- S18 Smart Casual Verification of the Confidential Consortium Framework;
- S19 Protocol Conformance with Choreographic PlusCal;
- S20 Verifying Zookeeper based on Model-Based runtime Trace-Checking using TLA+;
- S21 Model Checking Guided Testing for Distributed Systems;
- S22 Model Checking Guided Incremental Testing for Distributed Systems;
- S23 Using Lightweight Formal Methods to Validate a Key-Value Storage Node in Amazon S3.

Decision:

No redactar todavia Related Work. Primero verificar S18 a S23 y completar la matriz comparativa.

#### Batch G4: full-text screening de S18 a S23

Fecha:

`2026-09-07`

Seeds procesados:

- S18 Smart Casual Verification of CCF;
- S19 Protocol Conformance with Choreographic PlusCal;
- S20 ZooKeeper runtime trace-checking;
- S21 Mocket;
- S22 iMocket;
- S23 Amazon S3 ShardStore.

Full text verificado:

- S18;
- S19;
- S21;
- S22;
- S23.

Full text no recuperado:

- S20.

Decision sobre S20:

Se conserva como `include-supporting`. Su metadata fue verificada, pero no se utilizara para claims comparativos sustantivos.

Hallazgo principal:

Los nuevos trabajos cubren TLA+ ligado a implementaciones de produccion, trace validation, generated monitors, model-checking-guided testing, CI y workflows multi-tecnica. Estos resultados estrechan el gap de DTL-Lab y descartan claims generales de novedad sobre integration of formal methods and implementation testing.

Regla de parada:

No se ejecuta una tercera ronda general de snowballing metodologico. La siguiente expansion se limita a verificar comparadores cross-shard core que puedan modificar el posicionamiento del dominio.

#### Batch G5: verificación cross-shard core

Fecha:

`2026-09-07`

Seeds procesados:

- S06 Chainspace;
- S07 OmniLedger;
- S08 Prophet;
- S09 CSLAP;
- S10 Presto;
- S11 LightCross.

Full text verificado:

- S06;
- S07;
- S08;
- S09;
- S11.

Full text no recuperado:

- S10 Presto.

Decisión sobre S10:

Se conserva como `include-supporting`. La metadata y el abstract fueron verificados, pero las dimensiones metodológicas no observables desde el abstract se registran como `unclear`.

Hallazgo principal:

Los trabajos cross-shard revisados presentan distintos niveles de evidencia de correctness. Chainspace, Prophet, CSLAP y LightCross contienen argumentos o teoremas sobre propiedades del protocolo, mientras OmniLedger presenta argumentos de seguridad de Atomix. Esto no equivale a model checking basado en TLA+ o Alloy.

Regla de comparación:

Las métricas de throughput, latency y scalability de estos protocolos no se compararán con elapsed time, memory o state-space measurements de RQ4. Las primeras caracterizan desempeño del sistema; las segundas caracterizan costo de la verificación formal.

Regla de parada:

Con G5 termina la lectura intensiva de comparadores cross-shard core. G6 debe consolidar la matriz y producir la síntesis, ampliando la búsqueda solo si aparece una omisión crítica para un claim concreto.
