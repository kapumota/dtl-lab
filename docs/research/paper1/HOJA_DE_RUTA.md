### Hoja de ruta del Paper 1

#### Estado operacional al cierre de 8G-F6

El manuscrito científico permanece congelado en `06bea7de70971d5b22d705a2df19137122758c08`. 8G-F1 a 8G-F6 están cerrados. La integridad de la evidencia preservada fue comprobada sin repetir la campaña experimental ni generar nuevos resultados.

El camino crítico continúa con el cierre editorial SCP. 8G-F7 y 8G-F8 quedan diferidos como tareas no bloqueantes del artifact público.

Estado de envío: `NOT_READY_TO_CLICK_SUBMIT`.

#### Principio de integración

Cada fase se desarrolla en una rama corta creada desde `main` actualizado y se integra mediante Pull Request antes de abrir la siguiente.

#### Fases cerradas

- Fase 0: baseline de investigación.
- Fase 1: contrato del protocolo.
- Fase 2: máquina de estados Java.
- Fase 3: protocolo atómico y rollback.
- Fase 4: interleavings deterministas.
- Fase 5: model checking ejecutable.
- Fase 6: modelo multisesión y mutantes.
- Fase 6.1: cierre científico y documental.
- Fase 7A: exportación determinista de trazas.
- Fase 7B: función de abstracción Java-TLA+.
- Fase 7C: replay formal con TLC.
- Fase 7D: corpus negativo de trazas.
- Fase 7E: integración y cierre de conformidad.

#### Fase 6.1: cierre científico

Rama: `paper1/fase-6-1-cierre-cientifico`

Objetivos:

- alinear RQ1 con las propiedades ejecutadas,
- agregar conservación de valor e irreversibilidad terminal,
- exigir propiedad objetivo en mutantes Alloy,
- registrar procedencia inequívoca,
- actualizar documentación y versión,
- evitar componentes de conformidad.

Gate:

- baseline `v1.1.0-beta.2` listo para iniciar trazas.

#### Fase 7A: exportación de trazas

Rama: `paper1/fase-7a-exportacion-trazas`

Objetivos cerrados:

- definir JSONL versionado,
- exportar configuración, seed, observaciones y estados finales,
- conservar transiciones reales sin duplicar la máquina de estados,
- conservar pérdida, retraso, duplicación, rechazo y disponibilidad de shards,
- garantizar serialización y hashes deterministas,
- mantener fuera de alcance la abstracción TLA+ y el replay TLC.

Gate:

- los diez escenarios deterministas exportan JSONL,
- la misma seed produce los mismos bytes,
- `contentHash` y `fileHash` son reproducibles,
- el esquema `trace-schema-v1.json` describe los tres tipos de registro,
- `TraceExportTest` verifica eventos de protocolo y observaciones de simulación,
- las pruebas previas permanecen en el mismo runner.

#### Fase 7B: función de abstracción

Rama: `paper1/fase-7b-funcion-abstraccion`

Objetivos cerrados:

- mapear estados Java al vocabulario de `CrossShardCommit.tla`,
- mapear cada evento a una o más acciones formales,
- expandir `targetApprovals` en votos canónicos y reproducibles,
- conservar identidades de transferencia, shard, UTXO y recibo,
- consumir el contrato de Fase 7A sin modificar JSONL,
- mantener fuera de alcance la evaluación de `Next` y TLC.

Gate:

- los diez escenarios generan abstracciones deterministas,
- cada evento concreto produce al menos un paso abstracto,
- la preparación se expande en consumo y votos,
- commit, timeout y fallo de quorum conservan su efecto abstracto,
- los eventos de red se proyectan como `Stutter`,
- los cambios de identidad o topología son rechazados,
- no se generan decisiones de conformidad.

#### Fase 7C: replay formal con TLC

Rama: `paper1/fase-7c-replay-tlc`

Objetivos cerrados:

- generar módulos TLA+ deterministas desde `AbstractTrace`,
- usar `Init` y los operadores reales de `CrossShardCommit`,
- exigir coincidencia exacta del estado posterior,
- ejecutar TLC con un solo worker,
- localizar índice abstracto, paso concreto, acción y transferencia,
- conservar fuera de alcance el corpus negativo de Fase 7D.

Gate:

- los diez escenarios válidos generan módulos reproducibles,
- TLC acepta el catálogo válido,
- `ReplayEventuallyComplete` detecta un replay detenido,
- Java no redefine `Next` ni sus guardas,
- TLA+, Alloy y JSONL permanecen sin cambios,
- las pruebas de 7A y 7B continúan en verde.

#### Fase 7D: corpus negativo

Rama: `paper1/fase-7d-trazas-corruptas`

Objetivos cerrados:

- construir diez mutaciones tipadas desde trazas válidas,
- conservar prefijos válidos y un único paso objetivo corrupto,
- reutilizar `TraceConformanceChecker` y el parser de Fase 7C,
- rechazar commit inválido, replay, crédito sin recibo y commit sin quorum,
- cubrir votos duplicados, cambio de sesión, topología y orden de acciones,
- reportar paso abstracto, paso concreto, acción y transferencia.

Gate:

- las diez mutaciones son únicas y deterministas,
- TLC rechaza las diez trazas corruptas,
- cada diagnóstico coincide con su paso objetivo,
- el catálogo válido de Fase 7C continúa siendo aceptado,
- Java no duplica las guardas de `Next`,
- TLA+, Alloy, JSONL y los mapeadores permanecen sin cambios.

#### Fase 7E: integración de conformidad

Rama: `paper1/fase-7e-integracion-conformidad`

Objetivos cerrados:

- ejecutar catálogo válido y corpus negativo con una misma seed,
- generar un manifiesto científico con procedencia y hashes,
- publicar resumen y matriz de conformidad,
- integrar `make conformance-research` en CI,
- publicar el artefacto de conformidad,
- cerrar RQ3 con afirmaciones acotadas.

Gate Q3:

- TLC acepta diez escenarios válidos,
- TLC rechaza diez trazas corruptas,
- los diez diagnósticos coinciden con su objetivo,
- la procedencia identifica commit fuente y commit ejecutado,
- el workflow publica el artefacto integrado,
- TLA+, Alloy, JSONL, abstracción, replay y mutaciones no cambian de semántica.

#### Fase 8A: protocolo experimental Q3

Rama: `paper1/fase-8a-protocolo-experimental-q3`

Objetivos cerrados:

- elevar la pregunta de escalabilidad a RQ4,
- declarar H1 a H4 como hipótesis pendientes,
- congelar factores, niveles y configuraciones válidas,
- fijar treinta seeds, calentamientos y repeticiones,
- declarar timeout, memoria, hardware y versiones,
- congelar métricas, análisis estadístico, tablas y figuras,
- mantener fuera de alcance los resultados definitivos.

Gate cerrado:

- ninguna configuración queda implícita,
- cada RQ tiene unidad y métricas,
- H1 a H4 permanecen pendientes,
- seeds, repeticiones y recursos están fijados,
- criterios de exclusión y ejecuciones incompletas están declarados,
- el protocolo pasa `make experiment-protocol`,
- Java, TLA+, Alloy y conformidad no cambian,
- no se generan resultados definitivos.

#### Fase 8B: infraestructura experimental

Rama: `paper1/fase-8b-infraestructura-experimental`

Objetivos cerrados:

- derivar un inventario ejecutable desde artefactos existentes,
- construir un plan determinista de 1272 tareas,
- implementar un runner serial y reanudable,
- capturar ambiente y procedencia,
- conservar stdout, stderr, tiempo y memoria por tarea,
- validar configuraciones antes de ejecutar,
- separar resultados raw y derivados,
- mantener fuera de alcance los executors científicos y resultados definitivos.

Gate cerrado:

- los archivos congelados de Fase 8A conservan sus blobs,
- el plan contiene 112 calentamientos y 1160 tareas medidas,
- cada tarea tiene identidad y hash reproducibles,
- el runner rechaza ejecución concurrente,
- una segunda invocación omite tareas terminales válidas,
- CI solo ejecuta dry run y smoke test,
- ambiente, procedencia y snapshots quedan registrados,
- no se generan resultados definitivos,
- Java, TLA+, Alloy, JSONL y conformidad no cambian.

#### Fase 8C: matriz experimental

Rama: `paper1/fase-8c-matriz-experimental`

Objetivos:

- traducir perfiles congelados a entradas concretas de TLC y Alloy,
- ejecutar una propiedad o mutante por tarea,
- ejecutar un escenario o mutación de conformidad por tarea,
- preparar Java antes de iniciar las mediciones,
- ejecutar un smoke científico de seis tareas,
- ejecutar o reanudar la matriz completa de 1272 tareas,
- conservar timeout, falta de memoria y errores de herramienta,
- generar un manifiesto raw con hashes por resultado,
- mantener fuera de alcance el análisis estadístico.

Gate:

- Fase 8A y Fase 8B conservan sus contratos,
- los modelos TLA+ y Alloy versionados no cambian,
- el executor TLC reutiliza launcher y parser existentes,
- el executor Alloy reutiliza launcher y parser existentes,
- la conformidad reutiliza los catálogos y el checker existentes,
- el smoke científico cubre seis rutas representativas,
- CI publica el artefacto smoke sin usar sus tiempos,
- la ejecución definitiva rechaza WSL, virtualización y CI,
- la matriz puede reanudarse sin repetir resultados terminales,
- `raw-manifest.json` conserva 1272 resultados terminales,
- no se generan tablas ni figuras.

#### Fase 8D: análisis, tablas y figuras

Objetivos:

- generar resultados derivados,
- producir tablas y figuras desde raw,
- responder RQ1 a RQ4,
- documentar amenazas a la validez.

#### Fase 8E: reproducción independiente

Objetivos:

- ejecutar el artefacto en un ambiente limpio,
- registrar incidencias, hashes y resultados,
- corregir instrucciones sin alterar resultados raw.

#### Fase 8F: manuscrito Q3

Objetivos:

- integrar metodología, resultados y discusión,
- completar related work,
- completar amenazas a la validez,
- revisar afirmaciones y limitaciones.

#### Fase 8G: artefacto de envío

Objetivos históricos generales:

- congelar release,
- crear snapshot editorial,
- publicar checksums,
- archivar el artefacto con identificador persistente.

#### Gates 8G-F: artifact y provenance

Esta descomposición es planificación operativa pendiente de validación en cada gate, no certifica entregables ni anticipa un release, DOI o URL persistente.

- 8G-F1, DONE: reconciliación documental.
- 8G-F2, DONE: definir inclusión y exclusión del artifact público.
- 8G-F3, PASS: reconciliar el README raíz y la frontera entre software general y estudio científico.
- 8G-F4, DONE: recuperar e inventariar el bundle 8E, raw evidence y dependencias.
- 8G-F5, DONE: cerrar provenance completa.
- 8G-F6, DONE: verificar la integridad de la evidencia preservada sin repetir la campaña experimental.
- 8G-F7, DEFERRED: construir el artifact determinista. No bloquea el cierre editorial del Paper 1.
- 8G-F8, DEFERRED: preparar release y depósito persistente. No bloquea el cierre editorial del Paper 1.

El bundle histórico 8E no se presume idéntico al artifact final. Primero se audita el archive y solo después se publica o deposita. DOI, release y URL persistente se registran únicamente cuando existen.

#### Gates 8G-G: cierre editorial SCP

- 8G-G1, PENDING: authorship, affiliations, ORCID, corresponding author y CRediT.
- 8G-G2, PENDING: funding, competing interests, AI declaration y Data Availability definitiva.
- 8G-G3, PENDING: rebuild final SCP, incluyendo manuscript PDF, LaTeX source ZIP, `Highlights.docx`, cover letter, declarations y hashes.
- 8G-G4, PENDING: cross-check de todas las cifras del submission contra evidencia regenerable.
- 8G-G5, NO REQUERIDO: no se exige una segunda máquina física para el cierre editorial.
- 8G-G6, INTEGRADO: la revisión final se realiza sobre el PDF y el paquete editorial definitivo.

El estado `READY_TO_SUBMIT` se declara después de completar G1 a G4, revisar el PDF final y congelar el paquete exacto en 8G-H. Hasta entonces permanece `NOT_READY_TO_CLICK_SUBMIT`.

#### Gate 8G-H: freeze exacto del submission

Después del gate final, registrar el commit exacto, tag editorial, checksum del artifact, checksum del manuscrito, DOI o URL persistente real cuando exista y submission record. Establecer una relación inequívoca entre artifact revision y submission revision. Ningún valor se asigna antes de disponer de la evidencia correspondiente.

#### Fase 9 opcional

Objetivos:

- invariantes inductivas,
- fairness explícita,
- liveness temporal,
- relación de refinamiento más fuerte.

Esta fase no debe retrasar el primer envío del Paper 1.
