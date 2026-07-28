### Corpus negativo de trazas abstractas

#### Propósito

La Fase 7D agrega un corpus determinista de trazas corruptas para comprobar que el replay de la Fase 7C rechaza transiciones que no pertenecen a los operadores reales de `CrossShardCommit.tla`.

El corpus no modifica las trazas JSONL de la Fase 7A, la función de abstracción de la Fase 7B, el generador de replay de la Fase 7C ni la especificación formal. Cada mutación se aplica sobre una instancia inmutable de `AbstractTrace` ya construida desde un escenario válido.

#### Flujo

```text
escenario valido
    -> TraceExecution
    -> AbstractTrace
    -> mutacion tipada
    -> TraceConformanceChecker
    -> TLC
    -> rechazo con procedencia
```

#### Componentes

- `NegativeTraceCase` declara la mutación, la propiedad relacionada y el rechazo esperado;
- `NegativeTraceCatalog` construye las diez trazas corruptas;
- `NegativeTraceCatalogRunner` ejecuta TLC y valida el diagnóstico;
- `NegativeTraceCatalogTest` verifica estructura, determinismo y cobertura;
- `scripts/conformance/run_negative_trace_corpus.sh` ejecuta el corpus completo.

#### Mutaciones incluidas

| Identificador | Corrupción | Propiedad o guardia relacionada |
|---|---|---|
| `M01_COMMIT_FROM_PENDING` | commit directo desde `Pending` | guardia de `CommitTransfer` |
| `M02_COMMIT_AFTER_ABORT` | commit después de `Aborted` | `TerminalStateIrreversibility` |
| `M03_RECEIPT_REPLAY` | segundo consumo del recibo | `NoReceiptReplay` |
| `M04_CREDIT_WITHOUT_RECEIPT` | crédito sin consumo válido | `DestinationCreditRequiresValidReceipt` |
| `M05_TIMEOUT_WITHOUT_RELEASE` | timeout sin liberar origen | `EventuallyReleasedAfterTimeout` |
| `M06_COMMIT_WITHOUT_QUORUM` | commit con votos insuficientes | `QuorumRequired` |
| `M07_DUPLICATE_VOTE` | voto repetido del mismo validador | guardia de `CastVote` |
| `M08_TRANSFER_ID_CHANGE` | acción aplicada a otra sesión | identidad de transferencia |
| `M09_SHARD_TOPOLOGY_CHANGE` | intercambio de shards mediante `Stutter` | topología inmutable |
| `M10_CONSUME_BEFORE_LOCK` | consumo antes del bloqueo | orden de acciones |

#### Construcción de mutaciones

Las mutaciones se construyen mediante prefijos válidos y un único paso objetivo corrupto. Los pasos anteriores permanecen idénticos a la traza fuente. Esto permite asociar el rechazo con:

- índice abstracto;
- paso concreto;
- acción TLA+;
- transferencia;
- propiedad o guardia esperada.

La mutación no repara ni normaliza el estado posterior. El estado corrupto permanece visible para que TLC compare la acción real con la expectativa declarada.

#### Reutilización de la Fase 7C

El corpus usa sin modificaciones:

- `TlcTraceReplayGenerator`;
- `TraceConformanceChecker`;
- `TlcReplayResultParser`;
- `ConformanceResult`.

Java no evalúa las guardas de `Next`. El catálogo solo comprueba que el rechazo observado coincide con el paso, la acción y la transferencia declarados por cada caso negativo.

#### Ejecución

```bash
make conformance-negative
```

La salida predeterminada se escribe en:

```text
results/conformance/negative-v1/
```

También puede indicarse una ruta y una seed:

```bash
bash scripts/conformance/run_negative_trace_corpus.sh /tmp/dtl-negative 77
```

#### Manifiesto

`manifest.csv` registra:

- identificador de mutación;
- escenario fuente;
- seed;
- propiedad relacionada;
- paso y acción esperados;
- aceptación o rechazo;
- coincidencia del diagnóstico;
- código de salida;
- rutas de módulo, configuración y salidas de TLC.

El comando falla si TLC acepta una traza corrupta o si el diagnóstico corresponde a otro paso, acción o transferencia.

#### Gate de Fase 7D

La fase queda cerrada cuando:

- el catálogo contiene diez mutaciones únicas y deterministas;
- las diez trazas son rechazadas por TLC;
- cada rechazo coincide con su paso abstracto objetivo;
- cada rechazo conserva paso concreto, acción y transferencia;
- el catálogo válido de Fase 7C continúa siendo aceptado;
- las pruebas de Fases 7A, 7B y 7C permanecen en verde;
- TLA+, Alloy, JSONL y los mapeadores no son modificados.

#### Alcance científico

El corpus demuestra capacidad de detección para las mutaciones declaradas y las seeds ejecutadas. No demuestra completitud frente a todas las corrupciones posibles ni una relación general de refinamiento.

La Fase 7E integra el catálogo válido, el corpus negativo, CI y los resultados científicos de conformidad.
