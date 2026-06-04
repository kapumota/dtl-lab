package dltlab.pow;

/** Estado agregado de la cadena publica frente a la cadena privada del atacante. */
public enum SelfishMiningState {
    PUBLIC_CHAIN_AHEAD("cadena publica adelante"),
    PRIVATE_LEAD("atacante con ventaja privada"),
    PUBLIC_RACE("carrera publica"),
    PRIVATE_CHAIN_PUBLISHED("cadena privada publicada");

    private final String spanishName;

    SelfishMiningState(String spanishName) {
        this.spanishName = spanishName;
    }

    public String spanishName() {
        return spanishName;
    }
}
