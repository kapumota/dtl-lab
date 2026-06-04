### Fase 2: Aplicaciones DeFi y MEV con AMM

#### Objetivo

Esta fase convierte el MEV abstracto en MEV economico medible. En lugar de registrar manualmente un valor extraible, el proyecto calcula ese valor desde un mercado AMM con reservas, fees, slippage y price impact.

#### Componentes agregados

Se agrega el paquete `dltlab.defi` con los siguientes componentes:

- `Token`: activo fungible usado por el AMM.
- `AmmPool`: pool con dos tokens, reservas y fee en basis points.
- `SwapOrder`: orden de swap enviada por usuario o bot.
- `SwapResult`: resultado completo del swap.
- `ConstantProductMarketMaker`: motor basado en `x * y = k`.
- `SlippageCalculator`: calculo de slippage y price impact.
- `ArbitrageScenario`: escenario de arbitraje entre dos pools.

Tambien se extiende el paquete `dltlab.mev` con:

- `DeFiMEVScenario`: escenario MEV construido sobre un AMM.
- `SandwichAttackSimulator`: simulador de sandwich attack.
- `BackrunArbitrageSimulator`: simulador de arbitraje por backrun.

#### Modelo economico

El AMM usa la relacion de producto constante:

```text
x * y = k
```

Para un swap con token de entrada, el monto efectivo considera la fee del pool:

```text
amountInAfterFee = amountIn * (1 - feeBps / 10000)
amountOut = reserveOut * amountInAfterFee / (reserveIn + amountInAfterFee)
```

Luego se calculan:

```text
executionPrice = amountOut / amountIn
spotPriceBefore = reserveOut / reserveIn
spotPriceAfter = reserveOutputAfter / reserveInputAfter
slippage = (spotPriceBefore - executionPrice) / spotPriceBefore
priceImpact = abs(spotPriceAfter - spotPriceBefore) / spotPriceBefore
```

#### Sandwich attack

El simulador ejecuta tres pasos:

1. El atacante compra antes de la victima.
2. La victima ejecuta su swap contra un precio peor.
3. El atacante vende despues y captura la diferencia.

El resultado incluye:

- Slippage de la victima sin ataque.
- Slippage de la victima con sandwich.
- Ganancia del atacante.
- Perdida adicional de la victima.
- Pago MEV al productor.

#### Backrun de arbitraje

El simulador de arbitraje compara dos pools con precios divergentes. El bot compra en el pool donde el activo esta relativamente barato y vende en el pool donde esta relativamente caro. La ganancia se calcula como la diferencia entre la salida final y el monto inicial.

#### Ejecucion

```bash
bash scripts/run_defi_mev_demo.sh
```

Tambien puede ejecutarse desde el CLI compilado:

```bash
java -cp build/classes dltlab.app.DltLabCLI demo defi
```

#### Validacion

La fase agrega pruebas para comprobar que:

- Un swap grande genera slippage positivo.
- El producto de reservas se conserva o aumenta por efecto de la fee.
- Un sandwich empeora el slippage de la victima.
- El atacante obtiene ganancia positiva.
- El productor recibe pago MEV positivo.
- El arbitraje entre pools desbalanceados produce ganancia positiva.
