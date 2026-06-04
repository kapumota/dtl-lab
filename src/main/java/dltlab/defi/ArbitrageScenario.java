package dltlab.defi;

/** Escenario de arbitraje entre dos pools con precios distintos. */
public record ArbitrageScenario(
        String name,
        AmmPool buyPool,
        AmmPool sellPool,
        Token inputToken,
        double amountIn
) {
    public ArbitrageScenario {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("El nombre del escenario es obligatorio.");
        if (buyPool == null || sellPool == null) throw new IllegalArgumentException("Los pools son obligatorios.");
        if (inputToken == null) throw new IllegalArgumentException("El token de entrada es obligatorio.");
        if (amountIn <= 0.0) throw new IllegalArgumentException("El monto de arbitraje debe ser positivo.");
        if (!buyPool.supports(inputToken)) throw new IllegalArgumentException("El primer pool no soporta el token de entrada.");
        Token intermediate = buyPool.otherToken(inputToken);
        if (!sellPool.supports(intermediate)) throw new IllegalArgumentException("El segundo pool no soporta el token intermedio.");
    }
}
