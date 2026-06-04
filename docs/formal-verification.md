### Fase 4: Verificacion formal de commit cross-shard

#### Proposito

La Fase 4 agrega especificaciones formales para el componente mas delicado del proyecto: el commit cross-shard. Las fases anteriores validan ejecuciones concretas mediante pruebas, simulaciones, ataques e invariantes runtime. Esta fase agrega una capa complementaria: modelar estados y transiciones para explorar interleavings posibles con herramientas de model checking.

#### Alcance

La especificacion formal no reemplaza al codigo Java. Su objetivo es capturar el protocolo de alto nivel:

```text
1. El shard origen bloquea o debita un UTXO.
2. Se crea un recibo cross-shard.
3. El shard destino consume el recibo y acredita valor.
4. Si la operacion vence, el origen libera los fondos.
5. Una sesion no puede quedar confirmada y abortada a la vez.
```

#### Archivos agregados

```text
specs/tla/CrossShardCommit.tla
specs/tla/CrossShardCommit.cfg
specs/tla/README.md
specs/alloy/CrossShardCommit.als
scripts/run_formal_checks.sh
docs/formal-verification.md
```

#### Invariantes centrales

##### NoDoubleMint

El destino no puede crear valor sin un recibo valido. En el modelo, si `destinationCredited` es verdadero, entonces `receiptCreated` debe ser verdadero y el recibo debe haberse consumido exactamente una vez.

##### NoValueLoss

Si una sesion termina y el origen ya debito fondos, entonces el valor debe aparecer en el destino o debe liberarse en el origen. Esto evita terminales donde el sistema destruye valor.

##### NoReceiptReplay

Un recibo cross-shard no puede consumirse mas de una vez. Esta propiedad modela la defensa contra replay de recibos.

##### AtomicCommit

Una transferencia no puede quedar simultaneamente confirmada y abortada. Esta invariante preserva una unica decision final.

##### TimeoutReleasesFunds

Toda sesion vencida debe liberar los fondos bloqueados del origen.

#### Ejecucion

Verificacion estructural y ejecucion opcional con TLC:

```bash
bash scripts/run_formal_checks.sh
```

El script valida que existan las especificaciones y que contengan las invariantes esperadas. Si encuentra `tools/tla2tools.jar` o la variable `TLA_TOOLS_JAR`, ejecuta TLC sobre `CrossShardCommit.tla`.

#### Instalacion opcional de herramientas

Para ejecutar TLC de forma local, descarga `tla2tools.jar` y colocalo en:

```text
tools/tla2tools.jar
```

Tambien puedes definir:

```bash
export TLA_TOOLS_JAR=/ruta/a/tla2tools.jar
```

Para Alloy, abre el archivo siguiente con Alloy Analyzer:

```text
specs/alloy/CrossShardCommit.als
```

#### Relacion con el codigo Java

La especificacion se alinea con el modulo `sharding`. En Java, el sistema maneja sesiones, recibos, estados, quorum y timeouts. En TLA+ y Alloy se abstraen detalles criptograficos y de implementacion para concentrarse en las propiedades atomicas del protocolo.

#### Limitacion honesta

Esta fase no demuestra que toda la implementacion Java sea correcta. Demuestra que el protocolo abstracto modelado mantiene las invariantes bajo el espacio de estados configurado. Para una garantia mas fuerte, el siguiente paso seria instrumentar trazas del codigo Java y compararlas contra las acciones formales del modelo.
