package dltlab.mev;

import dltlab.defi.AmmPool;
import dltlab.defi.ConstantProductMarketMaker;
import dltlab.defi.SwapOrder;
import dltlab.defi.SwapResult;
import dltlab.defi.Token;

/** Simula un sandwich realista usando el estado secuencial de un AMM. */
public class SandwichAttackSimulator {
    private final ConstantProductMarketMaker marketMaker = new ConstantProductMarketMaker();

    public SandwichAttackResult simulate(DeFiMEVScenario scenario) {
        AmmPool poolWithoutAttack = scenario.initialPool().copy();
        SwapResult victimWithoutAttack = marketMaker.execute(poolWithoutAttack, scenario.victimOrder());

        AmmPool attackedPool = scenario.initialPool().copy();
        SwapResult frontRun = marketMaker.execute(attackedPool, scenario.attackerFrontRunOrder());
        SwapResult victimWithAttack = marketMaker.execute(attackedPool, scenario.victimOrder());
        SwapOrder backRunOrder = new SwapOrder("bot_venta_despues", frontRun.outputToken(), frontRun.amountOut(), 0.0);
        SwapResult backRun = marketMaker.execute(attackedPool, backRunOrder);

        double attackerProfit = backRun.amountOut() - scenario.attackerFrontRunOrder().amountIn();
        Token profitToken = scenario.attackerFrontRunOrder().inputToken();
        double victimOutputLoss = victimWithoutAttack.amountOut() - victimWithAttack.amountOut();
        double inputPerOutputAtStart = scenario.initialPool().reserveOf(scenario.victimOrder().inputToken())
                / scenario.initialPool().reserveOf(victimWithAttack.outputToken());
        double victimLossInInputToken = victimOutputLoss * inputPerOutputAtStart;
        double builderPayment = Math.max(0.0, attackerProfit * scenario.builderPaymentRatio());

        return new SandwichAttackResult(
                scenario.name(),
                frontRun,
                victimWithoutAttack,
                victimWithAttack,
                backRun,
                attackerProfit,
                profitToken,
                victimOutputLoss,
                victimWithAttack.outputToken(),
                victimLossInInputToken,
                scenario.victimOrder().inputToken(),
                builderPayment,
                builderPayment
        );
    }
}
