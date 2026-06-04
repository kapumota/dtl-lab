package dltlab.mev;

import dltlab.defi.SwapResult;
import dltlab.defi.Token;

/** Resultado de arbitraje backrun entre dos pools AMM. */
public record BackrunArbitrageResult(
        String scenarioName,
        SwapResult buyLeg,
        SwapResult sellLeg,
        double profit,
        Token profitToken
) {
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("Escenario: backrun de arbitraje entre AMMs\n");
        sb.append("  Nombre: ").append(scenarioName).append('\n');
        sb.append("  Compra intermedia: ").append(format(buyLeg.amountOut())).append(' ')
                .append(buyLeg.outputToken().display()).append('\n');
        sb.append("  Venta final: ").append(format(sellLeg.amountOut())).append(' ')
                .append(profitToken.display()).append('\n');
        sb.append("  Ganancia neta: ").append(format(profit)).append(' ')
                .append(profitToken.display()).append('\n');
        return sb.toString();
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.6f", value);
    }
}
