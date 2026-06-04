package dltlab.defi;

/** Pool AMM de dos tokens con reservas mutables para simular swaps secuenciales. */
public class AmmPool {
    private final String name;
    private final Token token0;
    private final Token token1;
    private double reserve0;
    private double reserve1;
    private final int feeBps;

    public AmmPool(String name, Token token0, Token token1, double reserve0, double reserve1, int feeBps) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("El nombre del pool es obligatorio.");
        if (token0 == null || token1 == null) throw new IllegalArgumentException("Los tokens del pool son obligatorios.");
        if (token0.equals(token1)) throw new IllegalArgumentException("El pool necesita dos tokens distintos.");
        if (reserve0 <= 0.0 || reserve1 <= 0.0) throw new IllegalArgumentException("Las reservas deben ser positivas.");
        if (feeBps < 0 || feeBps >= 10_000) throw new IllegalArgumentException("La fee en bps debe estar entre 0 y 9999.");
        this.name = name;
        this.token0 = token0;
        this.token1 = token1;
        this.reserve0 = reserve0;
        this.reserve1 = reserve1;
        this.feeBps = feeBps;
    }

    public String name() {
        return name;
    }

    public Token token0() {
        return token0;
    }

    public Token token1() {
        return token1;
    }

    public double reserve0() {
        return reserve0;
    }

    public double reserve1() {
        return reserve1;
    }

    public int feeBps() {
        return feeBps;
    }

    public boolean supports(Token token) {
        return token0.equals(token) || token1.equals(token);
    }

    public Token otherToken(Token token) {
        if (token0.equals(token)) return token1;
        if (token1.equals(token)) return token0;
        throw new IllegalArgumentException("El token no pertenece al pool.");
    }

    public double reserveOf(Token token) {
        if (token0.equals(token)) return reserve0;
        if (token1.equals(token)) return reserve1;
        throw new IllegalArgumentException("El token no pertenece al pool.");
    }

    public double invariant() {
        return reserve0 * reserve1;
    }

    public AmmPool copy() {
        return new AmmPool(name, token0, token1, reserve0, reserve1, feeBps);
    }

    void updateReserves(Token inputToken, double newInputReserve, double newOutputReserve) {
        if (token0.equals(inputToken)) {
            reserve0 = newInputReserve;
            reserve1 = newOutputReserve;
            return;
        }
        if (token1.equals(inputToken)) {
            reserve1 = newInputReserve;
            reserve0 = newOutputReserve;
            return;
        }
        throw new IllegalArgumentException("El token de entrada no pertenece al pool.");
    }
}
