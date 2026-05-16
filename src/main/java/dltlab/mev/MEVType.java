package dltlab.mev;

/** Tipos educativos de extraccion de valor por ordenamiento de transacciones. */
public enum MEVType {
    FRONT_RUNNING("front-running"),
    BACK_RUNNING("back-running"),
    SANDWICH("sandwich");

    private final String spanishName;

    MEVType(String spanishName) {
        this.spanishName = spanishName;
    }

    public String spanishName() {
        return spanishName;
    }
}
