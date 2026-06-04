package dltlab.mev;

import dltlab.defi.SwapResult;
import dltlab.defi.Token;

/** Resultado de un ataque sandwich calculado desde swaps reales del AMM. */
public record SandwichAttackResult(
        String scenarioName,
        SwapResult attackerFrontRun,
        SwapResult victimWithoutAttack,
        SwapResult victimWithAttack,
        SwapResult attackerBackRun,
        double attackerProfit,
        Token profitToken,
        double victimOutputLoss,
        Token victimOutputToken,
        double victimLossInInputToken,
        Token victimInputToken,
        double builderPayment,
        double producerRevenue
) {
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("Escenario: sandwich sobre AMM constante\n");
        sb.append("  Nombre: ").append(scenarioName).append('\n');
        sb.append("  Swap victima: ").append(format(victimWithAttack.order().amountIn()))
                .append(' ').append(victimInputToken.display()).append(" -> ")
                .append(victimOutputToken.display()).append('\n');
        sb.append("  Slippage victima sin ataque: ").append(format(victimSlippageWithoutAttack())).append("%\n");
        sb.append("  Slippage victima con sandwich: ").append(format(victimSlippageWithAttack())).append("%\n");
        sb.append("  Ganancia atacante: ").append(format(attackerProfit)).append(' ').append(profitToken.display()).append('\n');
        sb.append("  Perdida adicional victima: ").append(format(victimOutputLoss)).append(' ')
                .append(victimOutputToken.display()).append(" equivalente a ")
                .append(format(victimLossInInputToken)).append(' ').append(victimInputToken.display()).append('\n');
        sb.append("  Pago al productor: ").append(format(builderPayment)).append(' ').append(profitToken.display()).append('\n');
        sb.append("  Revenue productor: fees + pago MEV = ").append(format(producerRevenue))
                .append(' ').append(profitToken.display()).append('\n');
        return sb.toString();
    }

    public double victimSlippageWithoutAttack() {
        return victimWithoutAttack.slippagePercent();
    }

    public double victimSlippageWithAttack() {
        double initialSpot = victimWithoutAttack.spotPriceBefore();
        return Math.max(0.0, ((initialSpot - victimWithAttack.executionPrice()) / initialSpot) * 100.0);
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.6f", value);
    }
}
