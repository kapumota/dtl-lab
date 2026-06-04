package dltlab.mev;

import dltlab.defi.ArbitrageScenario;
import dltlab.defi.ConstantProductMarketMaker;
import dltlab.defi.SwapOrder;
import dltlab.defi.SwapResult;

/** Simula backrunning por arbitraje despues de detectar precios divergentes. */
public class BackrunArbitrageSimulator {
    private final ConstantProductMarketMaker marketMaker = new ConstantProductMarketMaker();

    public BackrunArbitrageResult simulate(ArbitrageScenario scenario) {
        SwapOrder buyOrder = new SwapOrder("bot_arbitraje_compra", scenario.inputToken(), scenario.amountIn(), 0.0);
        SwapResult buy = marketMaker.execute(scenario.buyPool().copy(), buyOrder);
        SwapOrder sellOrder = new SwapOrder("bot_arbitraje_venta", buy.outputToken(), buy.amountOut(), 0.0);
        SwapResult sell = marketMaker.execute(scenario.sellPool().copy(), sellOrder);
        double profit = sell.amountOut() - scenario.amountIn();
        return new BackrunArbitrageResult(scenario.name(), buy, sell, profit, scenario.inputToken());
    }
}
