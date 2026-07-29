### Hoja de ruta del Paper 1

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

- alinear RQ1 con las propiedades ejecutadas;
- agregar conservación de valor e irreversibilidad terminal;
- exigir propiedad objetivo en mutantes Alloy;
- registrar procedencia inequívoca;
- actualizar documentación y versión;
- evitar componentes de conformidad.

Gate:

- baseline `v1.1.0-beta.2` listo para iniciar trazas.

#### Fase 7A: exportación de trazas

Rama: `paper1/fase-7a-exportacion-trazas`

Objetivos cerrados:

- definir JSONL versionado;
- exportar configuración, seed, observaciones y estados finales;
- conservar transiciones reales sin duplicar la máquina de estados;
- conservar pérdida, retraso, duplicación, rechazo y disponibilidad de shards;
- garantizar serialización y hashes deterministas;
- mantener fuera de alcance la abstracción TLA+ y el replay TLC.

Gate:

- los diez escenarios deterministas exportan JSONL;
- la misma seed produce los mismos bytes;
- `contentHash` y `fileHash` son reproducibles;
- el esquema `trace-schema-v1.json` describe los tres tipos de registro;
- `TraceExportTest` verifica eventos de protocolo y observaciones de simulación;
- las pruebas previas permanecen en el mismo runner.

#### Fase 7B: función de abstracción

Rama: `paper1/fase-7b-funcion-abstraccion`

Objetivos cerrados:

- mapear estados Java al vocabulario de `CrossShardCommit.tla`;
- mapear cada evento a una o más acciones formales;
- expandir `targetApprovals` en votos canónicos y reproducibles;
- conservar identidades de transferencia, shard, UTXO y recibo;
- consumir el contrato de Fase 7A sin modificar JSONL;
- mantener fuera de alcance la evaluación de `Next` y TLC.

Gate:

- los diez escenarios generan abstracciones deterministas;
- cada evento concreto produce al menos un paso abstracto;
- la preparación se expande en consumo y votos;
- commit, timeout y fallo de quorum conservan su efecto abstracto;
- los eventos de red se proyectan como `Stutter`;
- los cambios de identidad o topología son rechazados;
- no se generan decisiones de conformidad.

#### Fase 7C: replay formal con TLC

Rama: `paper1/fase-7c-replay-tlc`

Objetivos cerrados:

- generar módulos TLA+ deterministas desde `AbstractTrace`;
- usar `Init` y los operadores reales de `CrossShardCommit`;
- exigir coincidencia exacta del estado posterior;
- ejecutar TLC con un solo worker;
- localizar índice abstracto, paso concreto, acción y transferencia;
- conservar fuera de alcance el corpus negativo de Fase 7D.

Gate:

- los diez escenarios válidos generan módulos reproducibles;
- TLC acepta el catálogo válido;
- `ReplayEventuallyComplete` detecta un replay detenido;
- Java no redefine `Next` ni sus guardas;
- TLA+, Alloy y JSONL permanecen sin cambios;
- las pruebas de 7A y 7B continúan en verde.

#### Fase 7D: corpus negativo

Rama: `paper1/fase-7d-trazas-corruptas`

Objetivos cerrados:

- construir diez mutaciones tipadas desde trazas válidas;
- conservar prefijos válidos y un único paso objetivo corrupto;
- reutilizar `TraceConformanceChecker` y el parser de Fase 7C;
- rechazar commit inválido, replay, crédito sin recibo y commit sin quorum;
- cubrir votos duplicados, cambio de sesión, topología y orden de acciones;
- reportar paso abstracto, paso concreto, acción y transferencia.

Gate:

- las diez mutaciones son únicas y deterministas;
- TLC rechaza las diez trazas corruptas;
- cada diagnóstico coincide con su paso objetivo;
- el catálogo válido de Fase 7C continúa siendo aceptado;
- Java no duplica las guardas de `Next`;
- TLA+, Alloy, JSONL y los mapeadores permanecen sin cambios.

#### Fase 7E: integración de conformidad

Rama: `paper1/fase-7e-integracion-conformidad`

Objetivos cerrados:

- ejecutar catálogo válido y corpus negativo con una misma seed;
- generar un manifiesto científico con procedencia y hashes;
- publicar resumen y matriz de conformidad;
- integrar `make conformance-research` en CI;
- publicar el artefacto de conformidad;
- cerrar RQ3 con afirmaciones acotadas.

Gate Q3:

- TLC acepta diez escenarios válidos;
- TLC rechaza diez trazas corruptas;
- los diez diagnósticos coinciden con su objetivo;
- la procedencia identifica commit fuente y commit ejecutado;
- el workflow publica el artefacto integrado;
- TLA+, Alloy, JSONL, abstracción, replay y mutaciones no cambian de semántica.

#### Fase 8A: protocolo experimental Q3

Rama: `paper1/fase-8a-protocolo-experimental-q3`

Objetivos:

- elevar la pregunta de escalabilidad a RQ4;
- declarar H1 a H4 como hipótesis pendientes;
- congelar factores, niveles y configuraciones válidas;
- fijar treinta seeds, calentamientos y repeticiones;
- declarar timeout, memoria, hardware y versiones;
- congelar métricas, análisis estadístico, tablas y figuras;
- mantener fuera de alcance los resultados definitivos.

Gate:

- ninguna configuración queda implícita;
- cada RQ tiene unidad y métricas;
- H1 a H4 permanecen pendientes;
- seeds, repeticiones y recursos están fijados;
- criterios de exclusión y ejecuciones incompletas están declarados;
- el protocolo pasa `make experiment-protocol`;
- Java, TLA+, Alloy y conformidad no cambian;
- no se generan resultados definitivos.

#### Fase 8B: infraestructura experimental

Objetivos:

- implementar el runner reanudable;
- capturar ambiente y procedencia;
- validar configuraciones antes de ejecutar;
- separar resultados raw y derivados.

#### Fase 8C: matriz experimental

Objetivos:

- ejecutar propiedades y mutantes;
- ejecutar conformidad multiseed;
- ejecutar perfiles de escalabilidad y fallos;
- conservar timeout y falta de memoria.

#### Fase 8D: análisis, tablas y figuras

Objetivos:

- generar resultados derivados;
- producir tablas y figuras desde raw;
- responder RQ1 a RQ4;
- documentar amenazas a la validez.

#### Fase 8E: reproducción independiente

Objetivos:

- ejecutar el artefacto en un ambiente limpio;
- registrar incidencias, hashes y resultados;
- corregir instrucciones sin alterar resultados raw.

#### Fase 8F: manuscrito Q3

Objetivos:

- integrar metodología, resultados y discusión;
- completar related work;
- completar amenazas a la validez;
- revisar afirmaciones y limitaciones.

#### Fase 8G: artefacto de envío

Objetivos:

- congelar release;
- crear snapshot editorial;
- publicar checksums;
- archivar el artefacto con identificador persistente.

#### Fase 9 opcional

Objetivos:

- invariantes inductivas;
- fairness explícita;
- liveness temporal;
- relación de refinamiento más fuerte.

Esta fase no debe retrasar el primer envío Q3 o Q4.
