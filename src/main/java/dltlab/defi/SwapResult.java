package dltlab.defi;

/** Resultado economico de un swap sobre un AMM constante. */
public record SwapResult(
        SwapOrder order,
        Token outputToken,
        double amountOut,
        double amountInAfterFee,
        double feeAmount,
        double executionPrice,
        double spotPriceBefore,
        double spotPriceAfter,
        double slippagePercent,
        double priceImpactPercent,
        double reserveInputBefore,
        double reserveOutputBefore,
        double reserveInputAfter,
        double reserveOutputAfter
) {
    public boolean satisfiesMinOutput() {
        return amountOut >= order.minAmountOut();
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("Swap de ").append(order.trader()).append('\n');
        sb.append("  Entrada: ").append(format(order.amountIn())).append(' ').append(order.inputToken().display()).append('\n');
        sb.append("  Salida: ").append(format(amountOut)).append(' ').append(outputToken.display()).append('\n');
        sb.append("  Slippage: ").append(format(slippagePercent)).append("%\n");
        sb.append("  Price impact: ").append(format(priceImpactPercent)).append("%\n");
        sb.append("  Reservas antes: ").append(format(reserveInputBefore)).append(" / ").append(format(reserveOutputBefore)).append('\n');
        sb.append("  Reservas despues: ").append(format(reserveInputAfter)).append(" / ").append(format(reserveOutputAfter)).append('\n');
        return sb.toString();
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.6f", value);
    }
}
