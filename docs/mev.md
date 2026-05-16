# MEV basico en DLT-Lab

MEV significa valor extraible por el productor del bloque. En esta version se modela de forma abstracta para ensenar el efecto del ordenamiento de transacciones.

## Escenarios incluidos

### Front-running

Un bot observa una transaccion de usuario en la mempool y logra que su propia transaccion sea ordenada antes.

```text
Orden honesto: usuario_trade -> tx_normal -> bot_front_run
Orden MEV:     bot_front_run -> usuario_trade -> tx_normal
```

### Back-running

Un bot se coloca inmediatamente despues de una transaccion que cambia el estado economico.

```text
Orden honesto: evento_objetivo -> tx_normal -> bot_back_run
Orden MEV:     evento_objetivo -> bot_back_run -> tx_normal
```

### Sandwich

Un bot rodea la transaccion del usuario con una transaccion antes y otra despues.

```text
Orden honesto: usuario_swap -> tx_normal -> bot_compra_antes -> bot_venta_despues
Orden MEV:     bot_compra_antes -> usuario_swap -> bot_venta_despues -> tx_normal
```

## Metrica principal

La demo compara:

```text
ingreso_minero_honesto = fees del orden honesto
ingreso_minero_mev     = fees del orden MEV + valor MEV extraido
```

Los resultados se exportan a:

```text
reports/mev_metrics.csv
```

## Limitacion intencional

Esta fase no modela AMMs, slippage real, pools de liquidez ni smart contracts. Es una capa pedagogica inicial para entender por que el orden de transacciones es un problema de seguridad/economia en DLT.
