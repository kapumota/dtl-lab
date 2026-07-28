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

Rama prevista: `paper1/fase-7c-replay-tlc`

Objetivos:

- convertir trazas abstractas en restricciones formales;
- usar TLC como oráculo;
- aceptar trazas conformantes;
- localizar transiciones rechazadas.

#### Fase 7D: corpus negativo

Rama prevista: `paper1/fase-7d-trazas-corruptas`

Objetivos:

- generar mutaciones de trazas;
- rechazar commit después de abort, replay, crédito sin recibo y commit sin quorum;
- reportar paso, transferencia y causa.

#### Fase 7E: integración de conformidad

Rama prevista: `paper1/fase-7e-integracion-conformidad`

Objetivos:

- agregar comando científico;
- integrar CI;
- publicar resultados;
- cerrar RQ3 como conformidad acotada basada en trazas.

Gate Q3:

- modelo formal relacionado y contrastado con la implementación ejecutable.

#### Fase 8: evaluación y artefacto

Objetivos:

- ejecutar la matriz experimental;
- generar tablas y figuras;
- congelar configuraciones;
- preparar release y snapshot del envío.

#### Fase 9 opcional

Objetivos:

- invariantes inductivas;
- fairness explícita;
- liveness temporal;
- relación de refinamiento más fuerte.

Esta fase no debe retrasar el primer envío Q3 o Q4.
