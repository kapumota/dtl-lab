package dltlab.defi;

/** Orden de swap enviada por un usuario, bot o productor de bloque. */
public record SwapOrder(String trader, Token inputToken, double amountIn, double minAmountOut) {
    public SwapOrder {
        if (trader == null || trader.isBlank()) {
            throw new IllegalArgumentException("El trader de la orden es obligatorio.");
        }
        if (inputToken == null) {
            throw new IllegalArgumentException("El token de entrada es obligatorio.");
        }
        if (amountIn <= 0.0) {
            throw new IllegalArgumentException("El monto de entrada debe ser positivo.");
        }
        if (minAmountOut < 0.0) {
            throw new IllegalArgumentException("El minimo de salida no puede ser negativo.");
        }
    }
}
