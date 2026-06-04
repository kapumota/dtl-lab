package dltlab.defi;

import java.util.Locale;

/** Representa un activo fungible usado por los escenarios DeFi. */
public record Token(String symbol, int decimals) {
    public Token {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("El simbolo del token es obligatorio.");
        }
        if (decimals < 0 || decimals > 18) {
            throw new IllegalArgumentException("Los decimales del token deben estar entre 0 y 18.");
        }
        symbol = symbol.trim().toUpperCase(Locale.ROOT);
    }

    public static Token of(String symbol, int decimals) {
        return new Token(symbol, decimals);
    }

    public String display() {
        return symbol;
    }
}
