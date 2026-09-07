### Seed papers para Background and Related Work

#### Identificacion

Paper:

`DTL-Lab Paper 1`

Fase:

`8F-G2`

Fecha:

`2026-09-07`

Objetivo:

Fijar los primeros seed papers para screening, lectura primaria y snowballing.

Los estados son preliminares hasta completar lectura de full text.

#### Prioridad P1: comparadores directos

#### S01 Design and Modular Verification of Distributed Transactions in MongoDB

Autores:

William Schultz, Murat Demirbas.

Ano:

2025.

Venue:

Proceedings of the VLDB Endowment, 18(12).

DOI:

`10.14778/3750601.3750626`

Clusters:

`C1`, `C2`, `C5`, `I1`, `I3`, `I6`

Estado:

`include-direct`

Razon:

Formaliza distributed cross-shard transactions con TLA+ y utiliza model-based verification para comprobar una interfaz de implementacion. Es el comparador metodologico mas cercano identificado en el discovery inicial.

Pendiente:

Lectura completa y extraccion de diferencias exactas frente a DTL-Lab.

#### S02 Validating Traces of Distributed Programs Against TLA+ Specifications

Autores:

Horatiu Cirstea, Markus A. Kuppe, Benjamin Loillier, Stephan Merz.

Ano:

2024.

Venue:

Software Engineering and Formal Methods, SEFM 2024.

DOI:

`10.1007/978-3-031-77382-2_8`

Clusters:

`C5`, `I3`, `I6`

Estado:

`include-direct`

Razon:

Propone constrained model checking con TLC para validar trazas de programas distribuidos contra especificaciones TLA+. Incluye instrumentacion Java y mapeo de eventos observados.

Pendiente:

Comparar alcance semantico, abstraccion de trazas, diagnosticos y cobertura de escenarios con RQ3.

#### S03 TraceLinking Implementations with Their Verified Designs

Autores:

Finn Hackett, Ivan Beschastnikh.

Ano:

2025.

Venue:

Proceedings of the ACM on Programming Languages, OOPSLA2.

DOI:

`10.1145/3763128`

Clusters:

`C5`, `C6`, `I3`, `I6`

Estado:

`include-direct`

Razon:

Automatiza trace validation entre implementaciones distribuidas y modelos TLA+. Reporta nueve bugs previamente no detectados y dispone de artefacto evaluado.

Pendiente:

Comparar automatizacion, generacion de implementacion, sidestep validation y relacion con el replay acotado de DTL-Lab.

#### S04 Towards Strengthening Formal Specifications with Mutation Model Checking

Autores:

Maxime Cordy, Sami Lazreg, Axel Legay, Pierre Yves Schobbens.

Ano:

2023.

Venue:

ESEC/FSE 2023.

DOI:

`10.1145/3611643.3613080`

Clusters:

`C4`, `I2`, `I6`

Estado:

`include-direct`

Razon:

Propone mutation model checking para evaluar y fortalecer especificaciones formales mediante modelos mutados.

Pendiente:

Comparar generacion de mutantes, objetivo de strengthening y significado del mutation score frente al catalogo cientifico fijo de DTL-Lab.

#### S05 Technical Report: Exploring Automatic Model-Checking of the Ethereum specification

Autores:

Igor Konnov, Jure Kukovec, Thomas Pani, Roberto Saltini, Thanh Hai Tran.

Ano:

2025.

Tipo:

Technical report.

Identificador:

`arXiv:2501.07958`

Clusters:

`C2`, `C3`, `I1`, `I4`, `I6`

Estado:

`include-direct`

Razon:

Usa TLA+ y Apalache y desarrolla codificaciones alternativas en SMT y Alloy para cross-validation. Discute explicitamente limites de model checking y abstraccion.

Pendiente:

Verificar si existe una version peer-reviewed posterior y comparar la funcion de Alloy con la doble modelizacion de DTL-Lab.

#### Prioridad P2: cross-shard y formal verification core

#### S06 Chainspace: A Sharded Smart Contracts Platform

Autores:

Mustafa Al-Bassam, Alberto Sonnino, Shehar Bano, Dave Hrycyszyn, George Danezis.

Ano:

2018.

Venue:

NDSS 2018.

DOI:

`10.14722/ndss.2018.23241`

Clusters:

`C1`

Estado:

`include-core`

Marca:

`seminal`

Razon:

Introduce S-BAC, un distributed commit protocol para transacciones sobre objetos distribuidos entre shards.

#### S07 OmniLedger: A Secure, Scale-Out, Decentralized Ledger via Sharding

Autores:

Eleftherios Kokoris-Kogias, Philipp Jovanovic, Linus Gasser, Nicolas Gailly, Ewa Syta, Bryan Ford.

Ano:

2018.

Venue:

IEEE Symposium on Security and Privacy.

DOI:

`10.1109/SP.2018.000-5`

Clusters:

`C1`

Estado:

`include-core`

Marca:

`seminal`

Razon:

Introduce un protocolo de commit cross-shard atomico y constituye un antecedente central para Atomicity y Atomix.

#### S08 Prophet: Conflict-Free Sharding Blockchain via Byzantine-Tolerant Deterministic Ordering

Autores:

Zicong Hong, Song Guo, Enyuan Zhou, Jianting Zhang, Wuhui Chen, Jinwen Liang, Jie Zhang, Albert Zomaya.

Ano:

2023.

Venue:

IEEE INFOCOM 2023.

DOI:

`10.1109/INFOCOM53939.2023.10228939`

Clusters:

`C1`

Estado:

`include-core`

Razon:

Propone deterministic ordering para reducir conflictos y abortos de transacciones cross-shard y evalua un prototipo.

#### S09 Cross shard leader accountability protocol based on two phase atomic commit

Autores:

Zhiqiang Du, Wendong Zhang, Liangxin Liu et al.

Ano:

2024.

Venue:

Scientific Reports, 14, 14953.

DOI:

`10.1038/s41598-024-64945-1`

Clusters:

`C1`

Estado:

`include-core`

Razon:

Construye CSLAP sobre 2PC, analiza consistencia y liveness y evalua latencia de comunicacion.

#### S10 Presto: Optimizing Cross-Shard Transactions in Sharded Blockchain Architecture

Ano:

2024.

Venue:

IEEE SRDS 2024.

DOI:

`10.1109/SRDS64841.2024.00023`

Clusters:

`C1`

Estado:

`include-core`

Razon:

Optimiza latencia cross-shard mediante optimistic pre-execution y una pending tree, con evaluacion de prototipo.

Pendiente:

Completar autores desde la fuente primaria durante screening.

#### S11 LightCross: Sharding with Lightweight Cross-Shard Execution for Smart Contracts

Ano:

2024.

Venue:

IEEE INFOCOM 2024.

DOI:

`10.1109/INFOCOM52122.2024.10621127`

Clusters:

`C1`

Estado:

`include-core`

Razon:

Propone ejecucion cross-shard ligera y un commit protocol para smart contracts, con implementacion sobre FISCO-BCOS.

Pendiente:

Completar autores y extraer assumptions de seguridad desde full text.

#### S12 Efficient Cross-Shard Transaction Execution in Sharded Blockchains

Autores:

Sourav Das, Vinith Krishnan, Ling Ren.

Ano:

2020.

Tipo:

Preprint.

Identificador:

`arXiv:2007.14521`

Clusters:

`C1`

Estado:

`include-supporting`

Razon:

Presenta Rivet como alternativa a 2PC y evalua un prototipo sobre infraestructura AWS.

Limitacion:

No se confirmo una version peer-reviewed en el discovery inicial. No debe tratarse como equivalente a un paper revisado por pares.

#### S13 Reusable Formal Verification of DAG-Based Consensus Protocols

Autores:

Nathalie Bertrand, Pranav Ghorpade, Sasha Rubin, Bernhard Scholz, Pavle Subotic.

Ano:

2025.

Venue:

NASA Formal Methods, NFM 2025.

DOI:

`10.1007/978-3-031-93706-4_9`

Clusters:

`C2`, `C3`

Estado:

`include-core`

Razon:

Presenta especificaciones TLA+ reutilizables y verificacion de seguridad para protocolos de consenso DAG mediante refinement y TLAPS.

#### S14 Understanding Inconsistency in Azure Cosmos DB with TLA+

Autores:

A. Finn Hackett, Joshua Rowe, Markus Alexander Kuppe.

Ano:

2023.

Venue:

ICSE-SEIP 2023.

DOI:

`10.1109/ICSE-SEIP58684.2023.00006`

Clusters:

`C2`, `C3`

Estado:

`include-core`

Razon:

Muestra el uso de TLA+ en un distributed database real para precisar comportamiento observable e identificar problemas de documentacion y consistencia.

#### S15 Model Checking Guided Testing for Distributed Systems

Autores:

Dong Wang, Wensheng Dou, Yu Gao, Chenao Wu, Jun Wei, Tao Huang.

Ano:

2023.

Venue:

EuroSys 2023.

DOI:

`10.1145/3552326.3587442`

Clusters:

`C5`, `I3`, `I6`

Estado:

`include-core`

Razon:

Usa el state space de model checking para guiar testing de implementaciones distribuidas y reporta bugs nuevos.

#### Prioridad P3: reproducibilidad

#### S16 Replicability of experimental tool evaluations in model-based software and systems engineering with MATLAB/Simulink

Autores:

Alexander Boll, Nicole Vieregg, Timo Kehrer.

Ano:

2024.

Venue:

Innovations in Systems and Software Engineering, 20, 209-224.

DOI:

`10.1007/s11334-022-00442-w`

Clusters:

`C6`

Estado:

`include-supporting`

Razon:

Evalua de forma sistematica la replicabilidad de estudios experimentales basados en herramientas y modelos. Es util para contextualizar por que la disponibilidad de codigo, modelos y datos es necesaria pero no suficiente.

#### Prioridad P4: contexto de artifact evaluation

#### S17 PADS Reproducibility and Artifact Evaluation guidelines

Ano:

2024.

Tipo:

Guideline.

Clusters:

`C6`

Estado:

`include-context`

Razon:

Documenta criterios de artifact evaluation, disponibilidad, funcionalidad, reuso y resultados reproducidos en una comunidad directamente relacionada con modelling and simulation.

Limitacion:

No es un research paper y no debe usarse como evidencia de novedad.

#### Semillas de mayor prioridad para snowballing

Orden inicial:

1. S01 MongoDB distributed transactions;
2. S02 SEFM trace validation;
3. S03 TraceLink;
4. S04 Mutation Model Checking;
5. S05 Ethereum 3SF;
6. S06 Chainspace;
7. S07 OmniLedger;
8. S08 Prophet.

#### Hipotesis de gap despues del discovery inicial

Hallazgo provisional:

La literatura identificada cubre de forma fuerte componentes individuales y varias combinaciones parciales.

No se identifico todavia, en esta primera pasada, un comparador que combine de forma verificable todas estas dimensiones en un mismo estudio cross-shard:

- bounded verification;
- TLA+ y Alloy como formalismos complementarios;
- mutation-based property validation;
- implementation-model trace conformance;
- verification-cost characterization;
- protocolo experimental congelado;
- regeneracion reproducible del analisis;
- reproduccion independiente del artefacto.

Este hallazgo es provisional.

No autoriza claims de prioridad.

Debe intentarse refutar mediante full-text screening y snowballing.

#### S18 Smart Casual Verification of the Confidential Consortium Framework

Autores:

Heidi Howard, Markus A. Kuppe, Edward Ashton, Amaury Chamayou, Natacha Crooks.

Ano:

2025.

Venue:

22nd USENIX Symposium on Networked Systems Design and Implementation, NSDI 2025.

Clusters:

`C2`, `C5`, `C6`, `I3`, `I6`

Estado:

`include-direct`

Origen:

Snowballing de S02 y S03.

Razon:

Combina TLA+, model checking y automated testing enlazado con una implementacion C++ de CCF. El enfoque se integra en CI y reporta seis bugs.

#### S19 Protocol Conformance with Choreographic PlusCal

Autores:

Darius Foo, Andreea Costea, Wei-Ngan Chin.

Ano:

2023.

Venue:

Theoretical Aspects of Software Engineering, TASE 2023.

DOI:

`10.1007/978-3-031-35257-7_8`

Clusters:

`C3`, `C5`, `I3`

Estado:

`include-core`

Origen:

Backward snowballing de S02 y S03.

Razon:

Extiende PlusCal para describir protocolos como choreographies y generar mecanismos de conformance o monitoring para implementaciones.

#### S20 Verifying Zookeeper based on Model-Based runtime Trace-Checking using TLA+

Autores:

Zhi Niu, Luming Dong, Yong Zhu, Li Chen.

Ano:

2022.

Venue:

ICCSIE 2022.

DOI:

`10.1145/3558819.3558822`

Clusters:

`C2`, `C5`, `I3`

Estado:

`include-core`

Origen:

Backward snowballing de S03.

Razon:

Aplica TLA+ y runtime trace-checking a una implementacion distribuida de ZooKeeper y Zab.

#### S21 Model Checking Guided Testing for Distributed Systems

Autores:

Dong Wang, Wensheng Dou, Yu Gao, Chenao Wu, Jun Wei, Tao Huang.

Ano:

2023.

Venue:

EuroSys 2023.

DOI:

`10.1145/3552326.3587442`

Clusters:

`C5`, `C6`, `I3`, `I6`

Estado:

`include-direct`

Origen:

Backward snowballing de S03 y relacion metodologica con S01.

Razon:

Usa el state space generado por model checking para guiar testing de implementaciones distribuidas y reporta tres bugs previamente desconocidos. El venue registra artifact availability.

#### S22 Model Checking Guided Incremental Testing for Distributed Systems

Autores:

Yu Gao, Dong Wang, Wensheng Dou, Wenhan Feng y colaboradores.

Ano:

2025.

Venue:

Proceedings of the ACM on Software Engineering, ISSTA 2025.

DOI:

`10.1145/3728883`

Clusters:

`C5`, `I3`, `I6`

Estado:

`include-core`

Origen:

Forward snowballing de S21.

Razon:

Extiende model checking guided testing hacia testing incremental de sistemas distribuidos.

Pendiente:

Completar autores y full-text verification en la siguiente ronda.

#### S23 Using Lightweight Formal Methods to Validate a Key-Value Storage Node in Amazon S3

Autores:

James Bornholt, Rajeev Joshi, Vytautas Astrauskas, Brendan Cully, Bernhard Kragl, Seth Markle, Kyle Sauri, Drew Schleit, Grant Slatton, Serdar Tasiran, Jacob Van Geffen, Andrew Warfield.

Ano:

2021.

Venue:

SOSP 2021.

DOI:

`10.1145/3477132.3483540`

Clusters:

`C2`, `C5`, `C6`, `I3`, `I6`

Estado:

`include-core`

Origen:

Snowballing metodologico de S01 y S03.

Razon:

Combina executable reference models, property-based testing y validacion continua contra una implementacion productiva de Amazon S3. Reporta 16 issues prevenidos antes de produccion.

#### Actualizacion de screening 8F-G4

Estado despues de full-text verification:

- S18: `include-direct`, `primary_source_verified: yes`;
- S19: `include-direct`, `primary_source_verified: yes`;
- S20: `include-supporting`, `primary_source_verified: no`;
- S21: `include-direct`, `primary_source_verified: yes`;
- S22: `include-core`, `primary_source_verified: yes`;
- S23: `include-direct`, `primary_source_verified: yes`.

Decision:

S20 no se utilizara para afirmaciones comparativas fuertes hasta disponer de su full text.

Regla de parada:

La expansion metodologica general termina en G4. La siguiente fase prioriza los comparadores cross-shard S06 a S11 y la sintesis de la matriz.
