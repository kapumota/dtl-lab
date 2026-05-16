package dltlab.consensus;

/** Tipo de comportamiento usado en la simulacion avanzada de consenso. */
public enum NodeBehavior {
    HONEST("honesto"),
    CENSORING("censor"),
    EQUIVOCATING("equivocador"),
    SILENT("silencioso");

    private final String spanishName;

    NodeBehavior(String spanishName) {
        this.spanishName = spanishName;
    }

    public String spanishName() {
        return spanishName;
    }
}
