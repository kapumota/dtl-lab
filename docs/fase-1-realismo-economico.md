### Fase 1: realismo economico de la capa base

#### Objetivo

Esta fase convierte la mempool en un mercado economico mas realista. Antes, las politicas principales comparaban transacciones por cantidad o por fee absoluto. Ahora el proyecto tambien puede comparar transacciones por fee rate, estimar tamano virtual en vBytes, limitar bloques por capacidad de espacio, aplicar eviction cuando la mempool esta llena, reemplazar transacciones conflictivas mediante RBF y seleccionar paquetes padre-hijo mediante CPFP.

#### Modulos agregados

```text
src/main/java/dltlab/transaction/
  FeeRate.java
  TransactionSizeEstimator.java

src/main/java/dltlab/mempool/
  MempoolConfig.java
  MempoolEntry.java
  MempoolAdmissionResult.java
  EvictionPolicy.java
  LowestFeeRateEvictionPolicy.java
  RbfPolicy.java
  FeeRatePolicy.java

src/main/java/dltlab/mining/
  BlockTemplateBuilder.java
```

#### Cambios principales

La clase `TransactionSizeEstimator` estima el tamano virtual de una transaccion. En esta version educativa no existe witness separado, por lo que el tamano virtual se aproxima con los bytes serializados de la transaccion.

La clase `FeeRate` representa la relacion entre fee y tamano virtual. Esto permite comparar transacciones por sats/vByte, que es mas realista que ordenar solo por fee absoluto.

La clase `TransactionMempool` conserva el metodo simple `add` para compatibilidad, pero agrega `admit`, que aplica reglas economicas configurables mediante `MempoolConfig`.

La politica `FeeRatePolicy` construye bloques respetando una capacidad en vBytes. La politica `PackageAwarePolicy` ahora tambien puede seleccionar paquetes por capacidad virtual, lo que permite modelar CPFP con mayor precision.

#### RBF

RBF significa reemplazo por fee. En esta fase, una transaccion candidata puede reemplazar una transaccion conflictiva si gasta el mismo UTXO y mejora el fee total y el fee rate. Si no mejora ambas condiciones, la mempool rechaza el reemplazo.

#### Eviction

Cuando la mempool supera su capacidad en vBytes, `LowestFeeRateEvictionPolicy` descarta primero las transacciones con menor fee rate. Si la nueva transaccion no sobrevive al descarte, se rechaza y se restaura el estado anterior.

#### CPFP

CPFP se modela con paquetes padre-hijo. Una transaccion hija puede pagar un fee alto para hacer economicamente atractiva la inclusion de una transaccion padre de bajo fee. La politica package-aware construye el paquete en orden ancestro a descendiente y compara el fee rate total del paquete.

#### Pruebas agregadas

La Fase 1 agrega pruebas para tres propiedades economicas:

```text
1. Una transaccion grande con fee absoluto alto pierde contra una transaccion pequena con mejor fee rate.
2. Una mempool llena descarta transacciones de bajo fee rate.
3. Una transaccion conflictiva puede ser reemplazada por otra con mayor fee y mayor fee rate.
```

Tambien se mantiene la prueba de paquete padre-hijo para verificar CPFP.

#### Comandos de validacion

```bash
bash scripts/run_tests.sh
bash scripts/run_mempool_demo.sh
bash scripts/run_demo.sh
bash scripts/run_security_checks.sh
```

#### Trabajo por ramas

Para trabajar esta fase en GitHub sin tocar directamente `main`, se recomienda:

```bash
git checkout main
git pull origin main
git checkout -b fase-1-realismo-economico-mempool

bash scripts/run_tests.sh
bash scripts/run_mempool_demo.sh

git add .
git commit -m "Fase 1: agregar realismo economico de mempool"
git push -u origin fase-1-realismo-economico-mempool
```

Luego se abre un Pull Request desde `fase-1-realismo-economico-mempool` hacia `main`. Si las pruebas pasan, la rama se puede fusionar.

#### Uso mediante parche

Tambien se puede trabajar con parches. Desde una copia limpia del repositorio:

```bash
git checkout -b fase-1-realismo-economico-mempool
git apply patches/fase-1-realismo-economico-mempool.patch
bash scripts/run_tests.sh
git add .
git commit -m "Fase 1: agregar realismo economico de mempool"
```

Este flujo es util si se quiere revisar los cambios antes de integrarlos al repositorio principal.
