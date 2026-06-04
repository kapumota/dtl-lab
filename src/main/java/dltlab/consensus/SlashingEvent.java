package dltlab.consensus;

/** Penalizacion reputacional por evidencia verificable. */
public record SlashingEvent(int nodeId, int round, double penalty, String reason) {
    public SlashingEvent {
        if (nodeId < 0) throw new IllegalArgumentException("El nodo no puede ser negativo.");
        if (round < 0) throw new IllegalArgumentException("La ronda no puede ser negativa.");
        if (penalty <= 0.0 || penalty > 1.0) throw new IllegalArgumentException("La penalidad debe estar en el rango valido.");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("La razon no puede estar vacia.");
    }
}
