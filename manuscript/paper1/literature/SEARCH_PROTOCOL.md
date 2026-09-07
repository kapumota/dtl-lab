### Protocolo de búsqueda para Background and Related Work

#### Identificación

Paper:

`DTL-Lab Paper 1`

Fase:

`8F-G1`

Estado:

`FROZEN FOR 8F-G`

Fecha de congelación:

`2026-09-07`

Objetivo:

Definir una estrategia reproducible y acotada para identificar, seleccionar, verificar y comparar literatura relacionada con el Paper 1 antes de redactar Background and Related Work.

Este protocolo corresponde a una structured related-work review. No se presenta como systematic literature review ni como revisión PRISMA.

#### Dependencias científicas

Antes de interpretar literatura o redactar Related Work deben revisarse:

`manuscript/paper1/CONTRIBUTION_EVIDENCE_MATRIX.md`

`manuscript/paper1/MANUSCRIPT_PLAN.md`

`manuscript/paper1/main.tex`

`manuscript/paper1/sections/03-cross-shard-model.tex`

`manuscript/paper1/sections/04-research-methodology.tex`

`manuscript/paper1/sections/07-discussion.tex`

`manuscript/paper1/sections/08-threats-to-validity.tex`

`manuscript/paper1/sections/09-reproducibility-artifact.tex`

La literatura no puede utilizarse para ampliar los claims experimentales más allá de la matriz contribución-evidencia.

#### Pregunta de posicionamiento

La pregunta principal de Related Work es:

What prior work addresses one or more components of the DTL-Lab evidence chain for cross-shard transaction validation, and to what extent has prior work integrated bounded formal verification, mutation-based validation, implementation-model trace conformance, verification-cost characterization, and reproducible artifact evaluation within one executable cross-shard study?

La revisión debe identificar:

- trabajos que cubren componentes individuales;
- trabajos que combinan dos o más componentes;
- trabajos directamente comparables por dominio cross-shard;
- trabajos metodológicamente cercanos aunque pertenezcan a otros sistemas distribuidos;
- diferencias entre DTL-Lab y los comparadores directos;
- límites de cualquier claim de novedad.

#### Hipótesis de posicionamiento

La hipótesis bibliográfica inicial es que los componentes de la cadena de evidencia existen por separado en la literatura, mientras que la integración completa evaluada por DTL-Lab puede estar menos cubierta.

Esta hipótesis no es un resultado.

Debe intentarse refutar mediante búsquedas específicas de integración antes de usar expresiones como `to our knowledge`.

No se utilizará la expresión `the first` salvo evidencia bibliográfica excepcionalmente fuerte. La formulación preferida será comparativa y acotada.

#### Ventana temporal

Ventana principal:

`2020-01-01` a `2026-09-07`

Excepción para trabajos seminales:

Se permiten publicaciones anteriores a 2020 cuando cumplan al menos una de estas condiciones:

- son citadas recurrentemente por trabajos incluidos de 2020 a 2026;
- introducen un protocolo cross-shard o atomic commit relevante;
- introducen una técnica formal necesaria para interpretar los comparadores;
- son necesarias para establecer antecedentes metodológicos directos.

Los trabajos anteriores a 2020 se registran como `seminal` y no deben dominar la comparación con el estado del arte reciente.

#### Tipos de publicación

Prioridad alta:

- artículos de journal revisados por pares;
- artículos de conference revisados por pares;
- proceedings de venues reconocidos en Computer Science;
- versiones oficiales publicadas por ACM, IEEE, Springer, Elsevier u otros publishers académicos establecidos.

Prioridad secundaria:

- technical reports de instituciones reconocidas;
- preprints cuando no exista una versión revisada por pares o cuando el trabajo sea demasiado reciente;
- artefactos oficiales asociados a un paper incluido.

Los preprints y technical reports deben marcarse explícitamente como no revisados por pares cuando corresponda.

#### Idioma

Idioma principal:

`English`

Un trabajo en otro idioma puede incluirse si aporta evidencia directa y su contenido puede verificarse de forma fiable, pero no se realizará una búsqueda específica por idioma.

#### Fuentes de descubrimiento

Fuentes principales:

- ACM Digital Library;
- IEEE Xplore;
- SpringerLink;
- ScienceDirect;
- Proceedings of the VLDB Endowment;
- arXiv para trabajos recientes o technical reports;
- repositorios institucionales de autores;
- sitios oficiales de proyectos de investigación;
- búsqueda académica web;
- Semantic Scholar o Google Scholar para descubrimiento y citation trails.

Herramientas auxiliares:

- Elicit para discovery y screening cuando el acceso disponible lo permita;
- ResearchRabbit para backward y forward snowballing;
- Connected Papers para localizar clusters relacionados;
- Zotero para gestión bibliográfica y deduplicación;
- Scite para auditoría opcional de citas críticas.

Las herramientas auxiliares no sustituyen la verificación contra la fuente primaria.

La indisponibilidad de una herramienta auxiliar no bloquea el protocolo.

#### Clusters de búsqueda

Los clusters se mantienen separados durante discovery para evitar que una query demasiado específica o demasiado amplia oculte literatura relevante.

#### C1 Cross-shard transactions and atomic commit

Objetivo:

Identificar protocolos y evaluaciones sobre atomicidad, commit, rollback, receipt handling, quorum, replay y coordinación entre shards.

Semantic query:

`cross-shard transactions atomic commit blockchain sharding protocol atomicity rollback quorum replay`

Keyword query base:

`"cross-shard transaction" AND (atomicity OR "atomic commit" OR "two-phase commit" OR 2PC OR rollback) AND (blockchain OR sharding)`

Términos auxiliares:

- inter-shard transaction;
- multi-shard transaction;
- atomic cross-shard;
- sharded blockchain;
- receipt;
- coordinator;
- commit protocol.

#### C2 Formal verification of blockchain and DLT protocols

Objetivo:

Identificar trabajos que aplican especificación formal, model checking o verificación automática a blockchain, DLT, consenso o transacciones distribuidas.

Semantic query:

`formal verification model checking blockchain distributed ledger transaction protocol safety liveness`

Keyword query base:

`(blockchain OR "distributed ledger") AND ("formal verification" OR "model checking" OR "formal specification")`

Términos auxiliares:

- safety;
- liveness;
- temporal logic;
- executable specification;
- consensus verification;
- transaction verification.

#### C3 TLA+ and Alloy verification of distributed systems

Objetivo:

Identificar usos metodológicamente cercanos de TLA+, Alloy o múltiples formalismos para verificar protocolos distribuidos y estudiar límites de model checking.

Semantic query:

`TLA+ Alloy distributed systems protocol model checking bounded verification abstraction scalability`

Keyword query base:

`(TLA+ OR Alloy OR Apalache) AND ("distributed system" OR protocol OR transaction) AND ("model checking" OR verification)`

Términos auxiliares:

- bounded model checking;
- explicit-state model checking;
- relational model finding;
- abstraction;
- state-space explosion;
- cross-validation;
- verification cost.

#### C4 Mutation-based validation of formal specifications

Objetivo:

Identificar mutation testing, mutation model checking y técnicas relacionadas para evaluar sensibilidad o adecuación de especificaciones y propiedades formales.

Semantic query:

`mutation model checking formal specifications mutation testing property adequacy defect detection`

Keyword query base:

`("mutation model checking" OR "mutation testing") AND ("formal specification" OR "model checking" OR property OR specification)`

Términos auxiliares:

- specification mutation;
- mutant;
- property strength;
- property adequacy;
- fault seeding;
- mutation score.

#### C5 Implementation-model conformance and trace validation

Objetivo:

Identificar trabajos que conectan implementaciones ejecutables con modelos formales mediante trazas, replay, model-based verification o conformance testing.

Semantic query:

`implementation model conformance trace validation trace replay TLA+ distributed systems formal specification`

Keyword query base:

`("trace validation" OR "trace conformance" OR replay OR "model-based verification") AND (implementation OR runtime) AND (TLA+ OR "formal model" OR specification)`

Términos auxiliares:

- runtime verification;
- trace checking;
- refinement testing;
- model-based testing;
- implementation verification;
- execution trace;
- model conformance.

#### C6 Reproducibility and artifact evaluation

Objetivo:

Identificar prácticas y estudios sobre artefactos reproducibles, replication, artifact evaluation e integridad experimental en formal methods y software engineering.

Semantic query:

`reproducibility artifact evaluation formal methods model checking software engineering replication`

Keyword query base:

`(reproducibility OR reproducible OR replication OR artifact) AND ("formal methods" OR "model checking" OR "software engineering")`

Términos auxiliares:

- artifact evaluation;
- reproducible research;
- research artifact;
- experimental replication;
- artifact badge;
- open science.

#### Integration probes

Estas búsquedas son obligatorias porque intentan encontrar trabajos que combinen directamente el dominio y las capas metodológicas de DTL-Lab.

#### I1 Cross-shard plus formal verification

`"cross-shard" AND ("formal verification" OR "model checking" OR TLA+ OR Alloy)`

#### I2 Cross-shard plus mutation validation

`"cross-shard" AND (mutation OR mutant OR "mutation testing" OR "mutation model checking")`

#### I3 Cross-shard plus implementation-model conformance

`"cross-shard" AND ("trace validation" OR "trace conformance" OR "model-based testing" OR replay)`

#### I4 Cross-shard plus verification cost

`"cross-shard" AND ("verification cost" OR "state-space" OR scalability OR bounds) AND ("model checking" OR verification)`

#### I5 Cross-shard plus reproducibility

`"cross-shard" AND (reproducibility OR reproducible OR artifact OR replication)`

#### I6 Multi-layer formal validation

`("cross-shard" OR "distributed transaction") AND ("formal verification" OR "model checking") AND (implementation OR conformance) AND (artifact OR reproducibility OR mutation)`

Un resultado relevante de los integration probes tiene prioridad de lectura aunque esté fuera de la ventana principal, siempre que sea científicamente comparable.

#### Estrategia de búsqueda

La búsqueda se ejecuta por cluster y no como una única query global.

Para cada cluster:

1. ejecutar la semantic query;
2. ejecutar la keyword query adaptada al buscador;
3. conservar los primeros resultados relevantes;
4. deduplicar por DOI y título normalizado;
5. seleccionar candidatos para lectura de abstract;
6. registrar inclusiones y exclusiones;
7. identificar seed papers;
8. ejecutar backward snowballing;
9. ejecutar forward snowballing;
10. repetir únicamente si aparecen nuevos comparadores directos.

Los integration probes se ejecutan después del primer discovery de C1 a C6 y antes de redactar cualquier claim de novedad.

#### Criterios de inclusión

Un trabajo se incluye si cumple al menos una condición temática y todas las condiciones de verificabilidad.

Condiciones temáticas:

- estudia cross-shard, inter-shard o distributed transactions;
- aplica formal verification o model checking a blockchain o sistemas distribuidos;
- usa TLA+, Alloy o una técnica formal directamente comparable;
- evalúa propiedades mediante mutantes o defect injection;
- conecta implementación y modelo mediante trazas, replay o model-based testing;
- evalúa costo o escalabilidad del proceso de verificación;
- estudia reproducibilidad o evaluación de artefactos aplicable a formal methods o software engineering.

Condiciones de verificabilidad:

- título y autores verificables;
- año y venue verificables;
- DOI, identificador persistente o URL primaria verificable;
- abstract verificable;
- full text disponible para trabajos usados en comparación directa;
- no existe evidencia de retractación activa.

#### Criterios de exclusión

Se excluye un trabajo cuando:

- solo menciona blockchain o sharding de forma tangencial;
- estudia exclusivamente asignación de shards sin tratamiento relevante de cross-shard transactions;
- es una página comercial o contenido promocional sin publicación científica asociada;
- es una tesis, presentación o blog sin evidencia adicional y existe una publicación primaria equivalente;
- la metadata no puede verificarse;
- el contenido completo no está disponible y el trabajo se necesita para una afirmación comparativa fuerte;
- duplica una versión posterior o peer-reviewed del mismo trabajo;
- fue retractado;
- se limita a rendimiento de blockchain sin relación con atomicidad, formal validation o alguno de los clusters definidos.

Los surveys pueden conservarse como fuentes de contexto y snowballing, pero no sustituyen las fuentes primarias de los trabajos comparados.

#### Screening

Cada candidato recibe uno de estos estados:

`include-core`

`include-direct`

`include-supporting`

`include-context`

`exclude`

Definiciones:

`include-core`

Trabajo central para explicar un cluster metodológico.

`include-direct`

Trabajo directamente comparable con DTL-Lab por dominio, combinación metodológica o ambos.

`include-supporting`

Trabajo que respalda una técnica, limitación o práctica metodológica.

`include-context`

Survey, guideline o antecedente útil para contexto y snowballing.

`exclude`

No cumple los criterios del protocolo.

#### Prioridad de lectura

Prioridad P1:

Comparadores directos.

Prioridad P2:

Trabajos centrales de cada cluster.

Prioridad P3:

Trabajos de soporte metodológico.

Prioridad P4:

Contexto general.

Los trabajos P1 y P2 requieren lectura de full text antes de redactar comparaciones sustantivas.

#### Snowballing

Backward snowballing:

Revisar referencias de los seed papers P1 y P2 para identificar antecedentes omitidos.

Forward snowballing:

Revisar trabajos que citan los seed papers, especialmente para publicaciones de 2023 o anteriores.

Regla de expansión:

Se ejecuta una ronda completa de backward y forward snowballing por seed P1.

Se ejecuta una segunda ronda solo si la primera produce al menos dos nuevos candidatos `include-direct` o `include-core` para el cluster.

Regla de parada:

La búsqueda de un cluster puede cerrarse cuando:

- no aparecen nuevos comparadores directos en una ronda;
- los principales trabajos recuperados empiezan a repetirse;
- el cluster contiene al menos tres trabajos core, salvo que la literatura disponible sea menor;
- los integration probes no producen combinaciones nuevas relevantes.

La regla de parada debe registrarse en `SEARCH_LOG.md`.

#### Target de volumen

Discovery pool esperado:

`60 a 100`

Candidatos después de deduplicación y abstract screening:

`30 a 45`

Core Related Work:

`20 a 30`

Comparadores directos:

`12 a 18`

Estos valores son objetivos de trabajo y no cuotas obligatorias.

La calidad y comparabilidad tienen prioridad sobre alcanzar un número prefijado.

#### Campos de extracción

La matriz de Related Work debe incluir como mínimo:

`paper_id`

`cluster`

`title`

`authors`

`year`

`venue`

`doi`

`primary_url`

`publication_type`

`peer_reviewed`

`system_domain`

`cross_shard`

`atomic_commit`

`formal_method`

`tla_plus`

`alloy`

`executable_implementation`

`mutation_validation`

`trace_conformance`

`verification_cost`

`artifact_available`

`reproducibility_evidence`

`independent_reproduction`

`main_contribution`

`method`

`main_result`

`main_limitation`

`gap_vs_dtl_lab`

`screening_status`

`exclusion_reason`

`primary_source_verified`

`notes`

Los valores booleanos se registran como:

`yes`

`no`

`unclear`

No se infiere `yes` si el paper no lo declara o demuestra.

#### Verificación de fuentes

Para cada trabajo `include-direct` o `include-core` se deben verificar:

- título;
- autores;
- año;
- venue;
- DOI;
- tipo de publicación;
- abstract;
- método relevante;
- resultado relevante;
- limitación utilizada en la comparación.

La fuente preferida es la página del publisher o el PDF oficial.

Si solo existe un preprint, se registra esa condición.

Un resumen de Elicit, SciSpace, Scite, ResearchRabbit, Connected Papers o un modelo de lenguaje nunca es la fuente final de una afirmación del manuscrito.

#### Gestión de referencias

`references.bib` debe contener únicamente referencias verificadas.

Reglas:

- preferir DOI oficial;
- usar claves BibTeX estables;
- evitar duplicados preprint y versión publicada;
- conservar la versión peer-reviewed cuando exista;
- no inventar campos BibTeX ausentes;
- validar caracteres LaTeX;
- mantener consistencia de autores, título, año y venue.

Zotero puede utilizarse como biblioteca canónica, pero `references.bib` versionado sigue siendo la fuente bibliográfica del manuscrito.

#### Registro de búsqueda

Cada ejecución debe registrarse en `SEARCH_LOG.md` con:

- fecha;
- cluster o integration probe;
- herramienta;
- query exacta;
- filtros;
- cantidad aproximada de resultados inspeccionados;
- candidatos retenidos;
- observaciones;
- incidencias.

No se requiere registrar todos los resultados irrelevantes de un buscador, pero sí debe quedar evidencia suficiente para repetir la estrategia.

#### Regla de comparación con DTL-Lab

La comparación no se basa en si un paper utiliza exactamente las mismas herramientas.

Se comparan dimensiones científicas:

- dominio cross-shard;
- atomicidad y rollback;
- propiedades formalizadas;
- tipo de model checking;
- uso de múltiples formalismos;
- sensibilidad de propiedades mediante mutantes;
- conexión implementación-modelo;
- evaluación de costo de verificación;
- protocolo experimental;
- disponibilidad de artefacto;
- reproducción o regeneración independiente.

La ausencia de una dimensión se registra como `no` solo si la fuente primaria permite concluirlo. En caso contrario se usa `unclear`.

#### Reglas para claims de novedad

No afirmar:

- que TLA+ aplicado a distributed transactions es nuevo;
- que Alloy aplicado a protocolos distribuidos es nuevo;
- que mutation testing de especificaciones es nuevo;
- que trace validation entre implementación y modelo es nueva;
- que artifact evaluation es nueva;
- que DTL-Lab es el primer trabajo que combina estas técnicas sin completar los integration probes.

Se permite afirmar diferencias verificadas como:

- un trabajo cubre una parte de la cadena de evidencia;
- un trabajo utiliza una técnica distinta;
- un trabajo no evalúa una dimensión cuando la fuente permite establecerlo;
- DTL-Lab integra determinadas capas que no aparecen juntas en los comparadores directos identificados.

Las frases de prioridad científica deben redactarse de forma conservadora.

#### Relación con las amenazas a la validez

Related Work no debe ocultar las limitaciones ya declaradas.

La comparación debe conservar:

- verificación acotada;
- censura del perfil TLC-large;
- mutation score limitado al catálogo;
- trace conformance limitado a la abstracción y escenarios evaluados;
- ausencia de refinement proof;
- ausencia de comparación absoluta TLC versus Alloy;
- ausencia de ley de crecimiento asintótica;
- reproducción independiente sin segunda máquina física;
- regeneración del análisis desde resultados raw preservados, no rerun completo de 1272 tareas.

#### Artefactos de la fase 8F-G

El directorio de trabajo será:

`manuscript/paper1/literature/`

Archivos previstos:

`SEARCH_PROTOCOL.md`

`SEARCH_LOG.md`

`SEED_PAPERS.md`

`RELATED_WORK_MATRIX.csv`

`RELATED_WORK_SYNTHESIS.md`

El manuscrito utilizará además:

`manuscript/paper1/references.bib`

#### Gate de 8F-G1

8F-G1 se considera cerrado cuando:

- `SEARCH_PROTOCOL.md` existe;
- los seis clusters están definidos;
- los integration probes están definidos;
- la ventana temporal está explícita;
- existen criterios de inclusión y exclusión;
- existe regla de snowballing;
- existe regla de parada;
- están definidos los campos de extracción;
- están definidas las reglas de claims de novedad;
- no se modifican código científico, modelos formales, resultados ni contratos experimentales;
- `git diff --check` no reporta errores.

#### Siguiente fase

Después de cerrar 8F-G1:

`8F-G2 Initial discovery and seed identification`

8F-G2 ejecutará las queries congeladas, registrará el discovery pool inicial y propondrá los primeros seed papers por cluster.
