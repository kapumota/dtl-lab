### Especificacion TLA+ de commit cross-shard

#### Objetivo

Esta carpeta contiene una especificacion TLA+ acotada del protocolo de commit cross-shard usado por DLT-Lab. El objetivo es modelar interleavings de alto nivel que no quedan cubiertos por las pruebas unitarias ni por la verificacion runtime.

#### Archivos

```text
CrossShardCommit.tla
CrossShardCommit.cfg
```

#### Variables principales

```text
originDebited
receiptCreated
receiptUseCount
destinationCredited
fundsReleased
committed
aborted
expired
```

#### Invariantes verificadas

```text
NoDoubleMint
NoValueLoss
NoReceiptReplay
AtomicCommit
TimeoutReleasesFunds
```

#### Ejecucion con TLC

El script principal detecta automaticamente `tools/tla2tools.jar` o la variable `TLA_TOOLS_JAR`.

```bash
bash scripts/run_formal_checks.sh
```

Tambien se puede ejecutar manualmente:

```bash
java -cp tools/tla2tools.jar tlc2.TLC -config specs/tla/CrossShardCommit.cfg specs/tla/CrossShardCommit.tla
```

#### Nota sobre sintaxis TLA+

TLA+ usa un cierre de modulo propio. Ese cierre pertenece a la sintaxis formal del lenguaje y no debe confundirse con separadores decorativos de consola o documentacion.
