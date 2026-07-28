### Replay formal de trazas con TLC

#### Propósito

La Fase 7C ejecuta las trazas abstractas producidas por la Fase 7B contra los operadores reales de `CrossShardCommit.tla`. TLC actúa como oráculo para decidir si cada acción observada está habilitada y si produce exactamente el estado abstracto esperado.

Esta fase no modifica el modelo formal y no reimplementa `Next` en Java. La Fase 7D reutiliza este mismo replay para ejecutar un corpus separado de trazas corruptas.

#### Flujo

El flujo ejecutable es:

```text
TraceExecution
    -> JavaToTlaStateMapper
    -> AbstractTrace
    -> TlcTraceReplayGenerator
    -> modulo TLA+ de replay
    -> TLC
    -> ConformanceResult
```

#### Componentes

- `TlcReplayArtifact` conserva el módulo, la configuración y la cantidad de pasos;
- `TlcTraceReplayGenerator` serializa estados y acciones abstractas;
- `TraceConformanceChecker` ejecuta TLC con un solo worker;
- `TlcReplayResultParser` interpreta aceptación o rechazo;
- `ConformanceResult` conserva el diagnóstico estructurado;
- `TraceReplayCatalogRunner` ejecuta los diez escenarios válidos.

#### Módulo generado

Cada módulo generado:

- extiende `CrossShardCommit`;
- usa `Init` para el estado inicial;
- invoca `LockTransfer`, `ConsumeReceipt`, `CastVote`, `CommitTransfer`, `TimeoutTransfer` o `Stutter`;
- fija el estado posterior esperado para cada acción;
- conserva un contador `replayIndex`;
- exige alcanzar el último índice mediante `ReplayEventuallyComplete`.

El generador no copia las definiciones de esos operadores y no traduce sus guardas a condiciones Java.

#### Detección de una transición inválida

`ReplaySpec` permite stuttering técnico, pero aplica fairness débil sobre `ReplayNext`. Si el operador esperado está habilitado, el replay debe avanzar. Si no está habilitado o no produce el estado esperado, el contador queda detenido y la propiedad `ReplayEventuallyComplete` falla.

TLC reporta el último valor de `replayIndex`. El parser lo relaciona con:

- índice abstracto;
- paso concreto de procedencia;
- acción TLA+;
- transferencia afectada.

#### Constantes

La configuración generada conserva:

- `Transfers` desde las identidades de `AbstractTrace`;
- `Shards` como `s0`, `s1` y siguientes;
- `Validators` desde los votos canónicos;
- `Quorum` desde la simulación;
- `ReceiptCopies = 1` por la abstracción canónica de Fase 7B;
- `DelayedCopies = {}` porque los retrasos sin identidad estructurada se proyectan como `Stutter`;
- `EnableTimeout = TRUE`.

#### Ejecución

```bash
make conformance-replay
```

La salida predeterminada se escribe en:

```text
results/conformance/replay-v1/
```

También puede indicarse una ruta y una seed:

```bash
bash scripts/conformance/run_trace_replay.sh /tmp/dtl-replay 77
```

#### Resultados

Cada escenario genera:

- módulo `.tla`;
- configuración `.cfg`;
- copia exacta de `CrossShardCommit.tla` usada por TLC;
- salida estándar;
- salida de error;
- metadatos internos de TLC.

El catálogo agrega `manifest.csv` con aceptación, código de salida y diagnóstico por escenario.

#### Gate de Fase 7C

La fase queda cerrada cuando:

- los diez escenarios válidos generan módulos deterministas;
- TLC acepta los diez escenarios;
- cada módulo invoca los operadores del modelo base;
- el estado inicial satisface `Init`;
- cada estado posterior coincide con la acción ejecutada;
- el parser localiza el paso cuando TLC reporta una violación;
- las pruebas de 7A y 7B permanecen en verde;
- TLA+, Alloy y el contrato JSONL no son modificados.

#### Alcance científico

La aceptación demuestra conformidad acotada para el catálogo y las seeds ejecutadas. No demuestra equivalencia total entre Java y TLA+, ni refinamiento completo de todas las ejecuciones posibles.

La Fase 7D agrega trazas negativas y mutaciones controladas sin cambiar este oráculo. La Fase 7E integra el perfil científico, CI y los resultados finales de conformidad.
