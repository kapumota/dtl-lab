### Verificación formal de commit cross-shard

#### Propósito

DLT-Lab combina especificaciones TLA+ y Alloy para estudiar propiedades del commit cross-shard. La verificación formal complementa las pruebas Java, los escenarios deterministas y las invariantes runtime.

#### Modelos

```text
specs/tla/CrossShardCommit.tla
specs/tla/CrossShardCommit.cfg
specs/alloy/CrossShardCommit.als
```

Los modelos comprueban:

- `NoDoubleMint`;
- `NoValueLoss`;
- `NoReceiptReplay`;
- `AtomicCommit`;
- `TimeoutReleasesFunds`.

#### Perfil educativo

```bash
make validate
```

Este perfil valida la estructura del repositorio y no requiere descargar herramientas formales. La comprobación se ejecuta mediante:

```bash
bash scripts/run_formal_checks.sh
```

El resultado de este perfil no debe describirse como model checking ejecutado.

#### Perfil científico

```bash
make formal-research
```

Este perfil requiere TLC y Alloy. Falla si falta una herramienta, si no puede identificarse su versión, si una propiedad válida falla o si no se genera un reporte estructurado.

Instalación local:

```bash
bash scripts/formal/install_tla_tools.sh
bash scripts/formal/install_alloy.sh
```

Las versiones fijadas se encuentran en:

```text
scripts/formal/tool_versions.env
```

#### Resultados estructurados

```text
results/formal/
├── tool_versions.txt
├── environment.json
├── tla_runs.csv
├── alloy_runs.csv
├── execution_manifest.json
└── logs/
```

TLC registra estados generados, estados distintos, profundidad, tiempo, memoria y resultado de cada invariante.

Alloy registra solver, alcance, contraejemplos, duración, tiempo total, memoria y resultado de cada assertion. Las métricas de estados y profundidad no aplican directamente a Alloy y quedan vacías en su CSV.

#### Integración continua

El workflow científico es:

```text
.github/workflows/formal-verification.yml
```

El workflow instala versiones concretas, ejecuta modelos válidos, ejecuta controles negativos temporales, valida los reportes y publica `results/formal/` como artefacto.

La validación general existente continúa publicando `reports/`. Los dos workflows tienen responsabilidades distintas.

#### Controles negativos

La Fase 5 genera copias temporales defectuosas de `AtomicCommit` para confirmar que TLC y Alloy rechacen una propiedad inválida.

Estos controles no son el catálogo de mutantes científicos. El modelo multisesión, los operadores de mutación y los contraejemplos versionados corresponden a la Fase 6.

#### Relación con Java

TLA+ y Alloy modelan el protocolo abstracto. No ejecutan el código Java ni demuestran por sí solos su conformidad.

La Fase 4 proporciona interleavings Java reproducibles. La Fase 6 ampliará los modelos formales y la Fase 7 relacionará trazas Java con acciones TLA+.

#### Limitación

Un check exitoso significa que no se encontró un contraejemplo dentro del alcance y configuración ejecutados. No significa verificación ilimitada ni equivalencia automática con toda la implementación Java.
