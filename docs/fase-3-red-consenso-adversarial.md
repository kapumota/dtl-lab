### Fase 3: Resiliencia de red y consenso robusto

#### Objetivo

La Fase 3 extiende DLT-Lab desde un simulador economico y DeFi hacia un laboratorio adversarial de red y consenso. La fase no mezcla todos los ataques dentro del consenso existente. En su lugar separa tres capas:

- `dltlab.pow`: selfish mining y metricas de recompensa.
- `dltlab.network`: topologia P2P, propagacion y eclipse attack.
- `dltlab.consensus`: reputacion, evidencia de equivocacion y slashing implicito.

Esta separacion mantiene el proyecto modular y permite probar cada amenaza de forma aislada antes de integrarla en la demo completa.

#### 3.1 Selfish mining

El paquete `dltlab.pow` agrega una simulacion educativa de selfish mining. El atacante no publica todos sus bloques inmediatamente. En cambio, conserva una cadena privada y decide cuando revelar bloques segun su lead privado.

Archivos principales:

```text
src/main/java/dltlab/pow/HashPowerDistribution.java
src/main/java/dltlab/pow/SelfishMiningState.java
src/main/java/dltlab/pow/SelfishMiningStrategy.java
src/main/java/dltlab/pow/SelfishMiningSimulator.java
src/main/java/dltlab/pow/MiningRewardMetrics.java
```

Metricas calculadas:

```text
hashrate atacante
hashrate honesto
bloques privados minados
bloques publicos minados
bloques aceptados del atacante
bloques aceptados honestos
bloques huerfanos
lead privado maximo
revenue relativo atacante
orphan rate
umbral aproximado de rentabilidad
```

La simulacion permite observar que la seguridad economica no depende solo de que el atacante tenga menos de 50 por ciento de hashrate. Una estrategia de publicacion puede cambiar el revenue relativo observado.

#### 3.2 Eclipse attack

El paquete `dltlab.network` modela una red P2P con peers, vecinos y propagacion. El ataque eclipse se representa como control de los vecinos visibles de una victima. Si todos los vecinos de una victima son controlados, la victima queda aislada.

Archivos principales:

```text
src/main/java/dltlab/network/Peer.java
src/main/java/dltlab/network/PeerTable.java
src/main/java/dltlab/network/NetworkPartition.java
src/main/java/dltlab/network/EclipseAttackSimulator.java
src/main/java/dltlab/network/MessagePropagationSimulator.java
src/main/java/dltlab/network/EclipseAttackResult.java
```

Metricas calculadas:

```text
peers totales
peers controlados
nodos aislados
latencia promedio estimada
bloques ocultos
transacciones censuradas
probabilidad de particion
```

Esta capa hace explicita la diferencia entre consenso local y red de propagacion. Un nodo puede ejecutar reglas correctas y aun asi tomar decisiones malas si su vista de red fue manipulada.

#### 3.3 Reputacion y deteccion de equivocacion

El paquete `dltlab.consensus` ahora contiene estructuras explicitas para detectar mensajes contradictorios firmados por el mismo nodo. La regla central es simple:

```text
Si un nodo firma dos mensajes incompatibles para la misma ronda y el mismo topico, se genera evidencia de equivocacion y baja su reputacion.
```

Archivos principales:

```text
src/main/java/dltlab/consensus/SignedConsensusMessage.java
src/main/java/dltlab/consensus/EquivocationEvidence.java
src/main/java/dltlab/consensus/ReputationScore.java
src/main/java/dltlab/consensus/ReputationWeightedConsensus.java
src/main/java/dltlab/consensus/SlashingEvent.java
src/main/java/dltlab/consensus/ReputationConsensusResult.java
```

El consenso ponderado por reputacion no reemplaza al simulador de consenso previo. Lo complementa. Su objetivo es demostrar como la evidencia verificable puede afectar el peso de un nodo en rondas posteriores.

#### Demo

La fase agrega un script especifico:

```bash
bash scripts/run_adversarial_demo.sh
```

Tambien se puede ejecutar desde el CLI:

```bash
mkdir -p build/classes
javac -d build/classes $(find src/main/java -name "*.java")
java -cp build/classes dltlab.app.DltLabCLI demo adversarial
```

La demo genera:

```text
reports/adversarial_network_report.txt
```

#### Validacion

Comandos recomendados:

```bash
bash scripts/run_tests.sh
bash scripts/run_adversarial_demo.sh
bash scripts/run_defi_mev_demo.sh
bash scripts/run_mempool_demo.sh
bash scripts/run_demo.sh
bash scripts/run_security_checks.sh
```

#### Flujo de ramas

```bash
git checkout main
git pull origin main
git checkout -b fase-3-red-consenso-adversarial
git apply --check fase-3-red-consenso-adversarial.patch
git apply fase-3-red-consenso-adversarial.patch
bash scripts/run_tests.sh
bash scripts/run_adversarial_demo.sh
git add README.md pom.xml docs scripts src
git commit -m "Fase 3: agregar selfish mining, eclipse attack y consenso ponderado por reputacion"
git push -u origin fase-3-red-consenso-adversarial
```
