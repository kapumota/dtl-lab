package dltlab.defi;

/** Utilidades de slippage y price impact para swaps AMM. */
public final class SlippageCalculator {
    private SlippageCalculator() {
    }

    public static double slippagePercent(double spotPriceBefore, double executionPrice) {
        if (spotPriceBefore <= 0.0) throw new IllegalArgumentException("El precio spot debe ser positivo.");
        return Math.max(0.0, ((spotPriceBefore - executionPrice) / spotPriceBefore) * 100.0);
    }

    public static double priceImpactPercent(double spotPriceBefore, double spotPriceAfter) {
        if (spotPriceBefore <= 0.0) throw new IllegalArgumentException("El precio spot debe ser positivo.");
        return Math.abs((spotPriceAfter - spotPriceBefore) / spotPriceBefore) * 100.0;
    }
}
