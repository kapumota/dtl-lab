package dltlab.defi;

/** AMM de producto constante basado en x * y = k. */
public class ConstantProductMarketMaker {
    public SwapResult quote(AmmPool pool, SwapOrder order) {
        return simulate(pool, order, false);
    }

    public SwapResult execute(AmmPool pool, SwapOrder order) {
        return simulate(pool, order, true);
    }

    public double invariant(AmmPool pool) {
        return pool.invariant();
    }

    private SwapResult simulate(AmmPool pool, SwapOrder order, boolean mutate) {
        if (pool == null) throw new IllegalArgumentException("El pool es obligatorio.");
        if (order == null) throw new IllegalArgumentException("La orden de swap es obligatoria.");
        if (!pool.supports(order.inputToken())) {
            throw new IllegalArgumentException("El token de entrada no pertenece al pool.");
        }

        Token outputToken = pool.otherToken(order.inputToken());
        double reserveIn = pool.reserveOf(order.inputToken());
        double reserveOut = pool.reserveOf(outputToken);
        double feeAmount = order.amountIn() * ((double) pool.feeBps() / 10_000.0);
        double amountInAfterFee = order.amountIn() - feeAmount;
        double amountOut = (reserveOut * amountInAfterFee) / (reserveIn + amountInAfterFee);
        double reserveInputAfter = reserveIn + order.amountIn();
        double reserveOutputAfter = reserveOut - amountOut;

        if (reserveOutputAfter <= 0.0) {
            throw new IllegalArgumentException("El swap agotaria la reserva de salida.");
        }

        double executionPrice = amountOut / order.amountIn();
        double spotPriceBefore = reserveOut / reserveIn;
        double spotPriceAfter = reserveOutputAfter / reserveInputAfter;
        double slippage = SlippageCalculator.slippagePercent(spotPriceBefore, executionPrice);
        double priceImpact = SlippageCalculator.priceImpactPercent(spotPriceBefore, spotPriceAfter);

        SwapResult result = new SwapResult(
                order,
                outputToken,
                amountOut,
                amountInAfterFee,
                feeAmount,
                executionPrice,
                spotPriceBefore,
                spotPriceAfter,
                slippage,
                priceImpact,
                reserveIn,
                reserveOut,
                reserveInputAfter,
                reserveOutputAfter
        );

        if (!result.satisfiesMinOutput()) {
            throw new IllegalStateException("El swap no cumple el minimo de salida configurado.");
        }

        if (mutate) {
            pool.updateReserves(order.inputToken(), reserveInputAfter, reserveOutputAfter);
        }
        return result;
    }
}
