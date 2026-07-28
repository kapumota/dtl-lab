### Formato determinista de trazas Java

#### Propósito

La Fase 7A define una representación JSONL versionada de ejecuciones concretas del protocolo cross-shard. Este formato conserva el orden lógico del simulador, las transiciones ya registradas por `CrossShardSession` y las observaciones de red necesarias para reproducir cada escenario.

El formato no incluye acciones TLA+, estados abstractos, resultados TLC ni decisiones de conformidad. La Fase 7B consume este contrato sin agregar campos al JSONL y la Fase 7C incorporará el replay formal.

#### Integración con el código existente

La exportación usa dos fuentes ya presentes en el baseline:

- `SimulationRun.trace()` aporta el orden global, la ronda lógica, los fallos de red y los resultados aceptados o rechazados;
- `CrossShardSession.events()` aporta las acciones Java y los estados anterior y siguiente de cada transición real del protocolo.

`TraceRecorder` combina ambas fuentes sin volver a ejecutar el protocolo y sin duplicar la tabla de transiciones. Una observación del simulador se enriquece con un `ProtocolEvent` únicamente cuando ambas fuentes coinciden en transferencia, orden y acción.

La exportación falla si quedan eventos del protocolo sin una observación correspondiente o si la ronda del simulador no coincide con el tiempo lógico del evento.

#### Archivos principales

- `src/main/java/dltlab/trace/TraceEvent.java`;
- `src/main/java/dltlab/trace/TraceExecution.java`;
- `src/main/java/dltlab/trace/TraceRecorder.java`;
- `src/main/java/dltlab/trace/TraceExporter.java`;
- `src/main/java/dltlab/trace/JsonlTraceExporter.java`;
- `src/main/java/dltlab/trace/TraceCatalogExporter.java`;
- `specs/trace/trace-schema-v1.json`.

#### Estructura JSONL

Cada archivo contiene:

1. una línea de configuración;
2. una línea por evento observado;
3. una línea de resultado.

No se escriben marcas de tiempo físicas. Cada línea termina con `LF` y se codifica en UTF-8.

#### Registro de configuración

```json
{"recordType":"configuracion","schemaVersion":1,"scenarioId":"S01_NORMAL_COMMIT","seed":2026,"shardCount":2,"quorum":3,"simulationTraceHash":"sha256:..."}
```

Campos:

- `schemaVersion`: versión del contrato JSONL;
- `scenarioId`: escenario de `SimulationScenario`;
- `seed`: semilla determinista;
- `shardCount`: cantidad de shards;
- `quorum`: quorum configurado;
- `simulationTraceHash`: hash de la traza textual previa del simulador.

#### Registro de evento

```json
{"recordType":"evento","schemaVersion":1,"step":0,"simulationSequence":0,"sessionSequence":0,"logicalRound":0,"kind":"PROTOCOL_TRANSITION","subjectId":"transfer-id","transferId":"transfer-id","simulationEventType":"BEGIN_TRANSFER","outcome":"EJECUTADO","javaAction":"CREATE_SESSION","previousStatus":null,"nextStatus":"CREATED","sourceShard":0,"targetShard":1,"sourceUtxoKey":"...","receiptId":"...","amount":4000,"sourceApprovals":4,"sourceValidators":4,"targetApprovals":null,"targetValidators":null,"detail":"Inicio de transferencia cross-shard."}
```

`kind` distingue:

- `PROTOCOL_TRANSITION`: la observación corresponde a una transición registrada por `CrossShardSession`;
- `SIMULATION_EVENT`: la observación pertenece al planificador, a la red o a una operación sin transición de estado.

Los eventos de simulación conservan casos como pérdida, retraso, duplicación, entrega rechazada, indisponibilidad de shard y recuperación. Sus campos de transición se serializan como `null`.

`targetApprovals` y `targetValidators` se incluyen únicamente cuando la sesión materializó evidencia de preparación o commit en destino. La ausencia de esos valores se representa con `null`, no con cero.

#### Registro de resultado

```json
{"recordType":"resultado","schemaVersion":1,"scenarioId":"S01_NORMAL_COMMIT","seed":2026,"eventCount":12,"finalStates":{"transfer-id":"COMMITTED"},"contentHash":"sha256:..."}
```

`finalStates` se ordena por identificador de transferencia.

#### Definición de hashes

`contentHash` se calcula sobre los bytes de:

- la línea de configuración;
- todas las líneas de eventos;
- el salto de línea posterior a cada registro.

La línea de resultado no participa en `contentHash` porque contiene ese mismo valor.

`fileHash` se calcula sobre el archivo JSONL completo y se publica en `manifest.csv`.

#### Reglas de determinismo

La misma combinación de escenario y seed debe producir:

- los mismos eventos;
- el mismo orden;
- los mismos bytes JSONL;
- el mismo `contentHash`;
- el mismo `fileHash`.

La implementación:

- usa orden de campos fijo;
- ordena estados finales por `transferId`;
- evita `HashMap` para datos serializados;
- no usa hora del sistema;
- no usa identificadores aleatorios;
- escapa texto JSON de forma estable;
- escribe siempre UTF-8 y `LF`.

#### Exportación del catálogo

```bash
make trace-export
```

La seed predeterminada es `2026` y la salida predeterminada es:

```text
results/traces/catalog-v1/
```

También puede indicarse una ruta y una seed:

```bash
bash scripts/export_trace_catalog.sh /tmp/dtl-traces 77
```

El catálogo contiene un JSONL por escenario y un `manifest.csv`.

#### Cobertura

La prueba `TraceExportTest` ejecuta los diez escenarios deterministas y verifica:

- repetibilidad por seed;
- pasos contiguos;
- creación de sesión observable;
- commit normal;
- timeout;
- fallo por quorum;
- multisesión;
- pérdida, duplicación y rechazo de mensajes;
- indisponibilidad y recuperación de shard;
- escritura exacta de bytes;
- ausencia de campos TLA+ o de conformidad.

#### Límite científico

La Fase 7A demuestra exportación concreta, determinista y reproducible. No demuestra que una traza sea aceptada por la especificación formal.

La Fase 7B implementa la función de abstracción sobre los objetos tipados de este contrato, sin modificar sus bytes ni su esquema. La Fase 7C realizará replay con TLC.
