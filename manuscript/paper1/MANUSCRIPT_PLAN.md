### Plan del manuscrito del Paper 1

#### Estado de actualización

Actualizado para estado operacional: 2026-09-23. La planificación original de 2026-09-07 se conserva más abajo como historia.

Estado general:

* Fases científicas 8A a 8E cerradas.
* Manuscrito 8F-A a 8F-J cerrado; scientific manuscript freeze `06bea7de70971d5b22d705a2df19137122758c08`.
* Baseline experimental `45cb114d61b1df8c605c50700f3cc72d48d157fe`; fuente de reproducción independiente `6cd88c377afd23fee4998882f91142d71e7d963e`.
* `main` auditado: `b68aeef7d212fd82c2466180134a51ed460473ee` (PR #29); PR 28 es un antecedente histórico.
* Submission 8G-A a 8G-E cerrado; fase actual 8G-F1, reconciliación documental.
* Estado de envío: `NOT_READY_TO_CLICK_SUBMIT`. Artifact-release revision y submission revision pendientes.
* `references.bib`, Introduction, Conclusions, Abstract, título y highlights científicos se completaron en 8F-J.

#### Objetivo editorial

Objetivo principal:

Publicar el Paper 1 como artículo científico revisado por pares en una revista legítima e indizada que permita reconocimiento en RENACYT, priorizando adecuación temática, calidad editorial y tiempo razonable de publicación.

Target editorial primario durante la planificación de 8F, sustituido para el submission actual:

Simulation Modelling Practice and Theory.

Target actual de submission: Science of Computer Programming (SCP), ruta `Research Papers`, línea principal `Formal techniques`, con modalidad de suscripción prevista y sin elección voluntaria de APC. La fuente editorial canónica actual es `manuscript/paper1/submission/targets/`, en particular `TARGET_CASCADE.md` y `scp/SCP_ADAPTATION_PROFILE.md`. El `\journal` del `main.tex` congelado conserva SMPT como dato histórico; la adaptación SCP se aplica en el bundle editorial, fuera del scientific freeze.

Publisher:

Elsevier.

Estrategia de publicación:

* Históricamente se consideró Simulation Modelling Practice and Theory como primera opción; para el envío actual rige SCP y la cascada editorial de `manuscript/paper1/submission/targets/TARGET_CASCADE.md`.
* No retrasar indefinidamente el envío por perseguir un cuartil específico.
* Mantener una lista corta de revistas alternativas indizadas en Scopus o Web of Science.
* Verificar indexación, cuartil, alcance, tipo de artículo, costos y política editorial inmediatamente antes de cada envío.
* Priorizar una revista que cumpla el criterio RENACYT vigente y tenga encaje real con modelado, verificación, sistemas distribuidos o blockchain.
* No utilizar revistas de legitimidad dudosa ni seleccionar un venue únicamente por rapidez.

#### Criterio RENACYT para la estrategia editorial

El criterio operativo para este proyecto es que el artículo publicado sea reconocible dentro de la producción científica considerada por RENACYT.

La normativa vigente consultada al actualizar este plan considera artículos científicos en revistas indizadas en Scopus, Web of Science y SciELO. En Scopus o Web of Science, los artículos reciben puntaje diferenciado según el cuartil de la revista.

La estrategia del Paper 1 no exige un cuartil mínimo como condición interna para enviar. Un Q4 legítimo e indizado puede ser preferible a retrasar excesivamente el trabajo si el objetivo inmediato es obtener una publicación válida para RENACYT.

La condición de indexación y el cuartil deben verificarse nuevamente en las fuentes oficiales correspondientes antes de someter el manuscrito y antes de tomar una decisión basada en puntaje RENACYT.

#### Alineación con el target principal

El manuscrito se posiciona como un estudio reproducible de modelado, verificación formal, validación y evaluación experimental de manejo de transacciones cross-shard en distributed ledgers.

El paper enfatiza:

* formal modelling;
* model checking;
* validation and verification;
* experimental design;
* distributed systems;
* blockchain transaction handling;
* implementation-model conformance;
* mutation-based validation;
* verification-cost characterization;
* reproducibility.

#### Baseline científico

Protocolo:

`paper1-q3-v1`

Matriz experimental definitiva:

1272 tareas.

Ejecuciones medidas:

1160.

Warmups:

112.

Formal tools:

* TLC 1.7.4
* Alloy 6.2.0

Implementación:

Java 17.

Análisis:

Python 3.12.

Reproducción independiente:

Completada.

Bundle histórico de reproducción independiente 8E, SHA-256 (no es por ello el artifact final de publicación):

`d464888e9f3e5d8cc64ef5d22cc7b7c24f83e3853f5825f18f23de26adf6a6e6`

Commit fuente reproducido:

`6cd88c377afd23fee4998882f91142d71e7d963e`

Resultado de reproducción independiente:

* 10 de 10 gates aprobados.
* 32 de 32 artefactos regenerados coincidentes por SHA-256.
* cero diferencias no resueltas;
* cero incidentes no resueltos;
* reproducción desde un clon, usuario y workspace separados;
* misma máquina Linux nativa para la reproducción independiente;
* no se reejecutaron las 1272 tareas de la campaña definitiva.

#### Preguntas de investigación

RQ1 examina si los modelos formales válidos satisfacen las propiedades declaradas dentro de los bounds evaluados.

RQ2 examina si los mutantes científicos exponen violaciones de sus propiedades objetivo.

RQ3 examina bounded implementation-model trace conformance entre la implementación Java y el modelo formal usando trazas válidas y deliberadamente corruptas.

RQ4 examina cómo cambia el costo de model checking al aumentar las configuraciones evaluadas.

#### Frontera de afirmaciones

El manuscrito no debe afirmar:

* corrección exhaustiva más allá de los bounds declarados;
* refinement general entre la implementación Java y los modelos formales;
* superioridad absoluta de rendimiento de TLC sobre Alloy o de Alloy sobre TLC;
* una ley de complejidad asintótica demostrada a partir de tres niveles de tamaño;
* generalización directa de los resultados a redes blockchain de producción;
* completitud de la specification suite a partir del mutation score;
* que las veinte clases de trazas representan todas las ejecuciones posibles;
* repetición independiente de las 1272 mediciones de rendimiento;
* reproducción independiente en una segunda máquina física;
* que la coincidencia de hashes demuestre corrección científica.

#### Contribución científica principal

El Paper 1 presenta un workflow reproducible que conecta:

* lógica ejecutable de commit cross-shard;
* dos enfoques formales complementarios con TLA+ y Alloy;
* bounded property verification;
* mutation-based property validation;
* bounded implementation-model trace conformance;
* verification-cost characterization;
* protocolo experimental congelado;
* análisis reproducible;
* reproducción independiente de los artefactos analíticos.

La novedad no debe atribuirse de forma aislada al uso de TLA+, Alloy, mutation testing o trace conformance. En 8F-G se examinó la literatura primaria para delimitar la contribución integrada de DTL-Lab; consultar los registros de auditoría de `manuscript/paper1/literature/`.

#### Estrategia de construcción del manuscrito

El manuscrito se construye desde evidencia hacia claims.

Orden actualizado:

1. modelo, metodología y diseño experimental;
2. resultados;
3. discusión y amenazas a la validez;
4. reproducibilidad y artefacto;
5. related work;
6. introduction;
7. conclusions;
8. abstract, título, keywords y highlights;
9. revisión integral del manuscrito;
10. preparación del artefacto de envío.

#### Estado detallado de Fase 8F

* 8F-A, contrato y estructura del manuscrito: DONE.
* 8F-B, matriz contribución-evidencia: DONE.
* 8F-C, modelo cross-shard, metodología y diseño experimental: DONE.
* 8F-D, Results RQ1 a RQ4: DONE.
* 8F-E, Discussion y Threats to Validity: DONE.
* 8F-F, Reproducibility and Artifact: DONE.
* 8F-G, Background and Related Work: DONE.
* 8F-H, Introduction: DONE.
* 8F-I, Conclusions: DONE.
* 8F-J, Abstract, título, keywords, highlights y revisión integral: DONE.

8G-A, baseline universal: DONE. 8G-B, journal target matrix: DONE. 8G-C, perfil SCP: DONE. 8G-D, declaraciones y cover letter: DONE como drafts con metadata administrativa pendiente. 8G-E, archivos técnicos SCP: DONE con gate `PASS_TECHNICAL_WITH_ADMINISTRATIVE_PENDING`. 8G-F1, reconciliación documental: CURRENT. El resto de 8G está pendiente; ver `docs/research/paper1/HOJA_DE_RUTA.md`.

#### Fase 8F-G, Background and Related Work

Las instrucciones de esta sección registran el plan ejecutado en 8F-G. Su gate quedó cerrado; no son tareas actuales.

Objetivo:

Construir el posicionamiento bibliográfico del Paper 1 antes de redactar la sección final.

Clusters mínimos de búsqueda:

* cross-shard transactions and atomic commit;
* formal verification of blockchain and DLT protocols;
* TLA+ and Alloy verification of distributed systems;
* mutation-based validation of formal specifications;
* implementation-model conformance and trace replay;
* reproducibility and artifact evaluation in formal methods and software engineering.

Artefactos de trabajo previstos:

`manuscript/paper1/literature/SEARCH_PROTOCOL.md`

`manuscript/paper1/literature/SEARCH_LOG.md`

`manuscript/paper1/literature/SEED_PAPERS.md`

`manuscript/paper1/literature/RELATED_WORK_MATRIX.csv`

`manuscript/paper1/literature/RELATED_WORK_SYNTHESIS.md`

`manuscript/paper1/references.bib`

Workflow bibliográfico:

* búsqueda inicial y screening estructurado con Elicit y fuentes académicas;
* verificación de metadata, DOI, venue y contenido en fuentes primarias;
* backward y forward snowballing;
* ResearchRabbit y Connected Papers como herramientas auxiliares cuando aporten cobertura;
* Zotero como biblioteca canónica si se conecta al workflow;
* Scite como auditoría opcional para claims y citas críticas;
* ninguna afirmación científica se incorpora al manuscrito únicamente desde un resumen generado por IA.

La Fase 8F-G termina únicamente cuando:

* existe un protocolo de búsqueda documentado;
* existe una matriz de comparación;
* los papers centrales han sido verificados contra fuentes primarias;
* `references.bib` contiene referencias reales y verificadas;
* Related Work posiciona la contribución sin afirmar novedad inexistente;
* cada claim comparativo importante puede rastrearse a una fuente.

#### Reglas para cada parche y fase

Antes de aplicar un parche se debe analizar el resultado de la fase inmediatamente anterior y los contratos científicos relevantes.

Para las fases restantes del manuscrito:

* no modificar Java, TLA+, Alloy, resultados raw, protocolo experimental ni scripts científicos salvo que se descubra un defecto verificable;
* comprobar que el nuevo contenido no duplique ni contradiga la fase anterior;
* comparar cada claim nuevo con `CONTRIBUTION_EVIDENCE_MATRIX.md`;
* revisar los archivos que alimentan directamente la nueva fase antes de escribir;
* ejecutar `git diff --check`;
* verificar el alcance del diff antes del commit;
* mantener un commit y un PR claramente delimitados por fase siempre que sea práctico.

Para 8F-G se deben revisar antes de escribir:

* `manuscript/paper1/CONTRIBUTION_EVIDENCE_MATRIX.md`;
* `manuscript/paper1/MANUSCRIPT_PLAN.md`;
* `manuscript/paper1/main.tex`;
* `manuscript/paper1/sections/03-cross-shard-model.tex`;
* `manuscript/paper1/sections/04-research-methodology.tex`;
* `manuscript/paper1/sections/07-discussion.tex`;
* `manuscript/paper1/sections/08-threats-to-validity.tex`;
* `manuscript/paper1/sections/09-reproducibility-artifact.tex`.

#### Convenciones de código y documentación

En código:

* los comentarios deben escribirse en español;
* las cadenas de texto visibles o diagnósticas deben escribirse en español;
* las firmas de funciones y métodos se mantienen en inglés.

En documentación:

* los títulos nuevos usan `###`;
* los subtítulos nuevos usan `####`;
* evitar guiones largos;
* evitar separadores decorativos con signos repetidos;
* evitar emoticones;
* evitar símbolos extraños o decorativos sin función técnica;
* mantener texto simple y reproducible en Markdown.

Estas reglas no obligan a traducir el manuscrito científico en LaTeX, que permanece redactado en inglés para el envío internacional.

#### Estrategia de ramas para cerrar el paper

Rama actual 8G-F1:

`paper1/fase-8g-f1-reconciliacion-documental`, creada desde `origin/main` auditado.

Ramas históricas de 8F, ya completadas:

* `paper1/fase-8f-h-introduction`
* `paper1/fase-8f-i-conclusions`
* `paper1/fase-8f-j-final-manuscript`

La rama `paper1/fase-8g-submission-artifact` fue una previsión general anterior. Los siguientes gates 8G se trabajarán en ramas pequeñas definidas al abrir cada fase; no se anticipa su nombre ni su contenido. Para 8G-F1 no hacer commit, push, merge ni PR sin autorización explícita.

Flujo por fase:

1. actualizar `main` con `git pull --ff-only`;
2. crear una rama desde `main`;
3. inspeccionar la fase anterior;
4. aplicar el parche de alcance mínimo;
5. ejecutar gates;
6. solicitar autorización explícita antes de commit, push, merge o PR;
7. tras autorización, revisar y fusionar la fase;
8. actualizar `main` y cerrar la rama de fase.

#### Estructura objetivo del manuscrito

1. Introduction
2. Background and Related Work
3. Cross-Shard Transaction Model
4. Research Methodology
5. Experimental Design
6. Results
7. Discussion
8. Threats to Validity
9. Reproducibility and Artifact
10. Conclusions

#### Restricciones editoriales de trabajo

Abstract:

Máximo histórico de trabajo: 250 palabras cuando Simulation Modelling Practice and Theory era el target. Para SCP rige el perfil de adaptación canónico; no reabrir el abstract científico en 8G-F1.

Keywords:

1 a 7.

Highlights:

3 a 5.

Longitud máxima de trabajo por highlight:

85 caracteres incluyendo espacios.

Estilo de referencias para el draft:

Referencias numeradas consistentes con `elsarticle`.

La plantilla y las restricciones exactas deben verificarse nuevamente antes del envío.

#### Criterio de cierre de Fase 8F

La Fase 8F termina cuando:

* Background and Related Work está redactado y citado;
* Introduction está redactada después del posicionamiento bibliográfico;
* Conclusions responde a RQ1 a RQ4 sin exceder la matriz de claims;
* el título refleja la contribución real;
* el abstract resume problema, método, resultados, contribución y límites;
* keywords y highlights son consistentes con el target;
* no existen placeholders editoriales;
* todas las referencias usadas existen en `references.bib`;
* las citas han sido verificadas;
* el manuscrito compila;
* tablas, figuras y referencias están conectadas;
* `git diff --check` pasa;
* se realiza una auditoría final de claims contra `CONTRIBUTION_EVIDENCE_MATRIX.md`.

#### Fase 8G, artefacto de envío

La Fase 8G no modifica los resultados científicos.

Objetivos:

* congelar la versión sometida;
* crear release y tag editorial;
* crear snapshot del manuscrito;
* registrar checksums;
* empaquetar el artefacto reproducible;
* generar instrucciones finales de reproducción;
* archivar el artefacto con un identificador persistente cuando corresponda;
* conservar la relación entre versión de manuscrito, commit fuente y bundle reproducible.

#### Prioridad operativa

La prioridad es completar y someter un manuscrito científicamente defendible sin agregar nuevas fases experimentales que no sean necesarias para el primer envío.

La Fase 9 opcional, que contempla invariantes inductivas, fairness, liveness temporal y una relación de refinamiento más fuerte, permanece fuera del camino crítico del primer envío.
