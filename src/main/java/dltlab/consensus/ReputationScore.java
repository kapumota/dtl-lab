package dltlab.consensus;

/** Score reputacional acumulado para consenso ponderado. */
public record ReputationScore(int nodeId, double score, int slashingEvents) {
    public ReputationScore {
        if (nodeId < 0) throw new IllegalArgumentException("El nodo no puede ser negativo.");
        if (score < 0.0 || score > 1.0) throw new IllegalArgumentException("El score debe estar entre 0 y 1.");
        if (slashingEvents < 0) throw new IllegalArgumentException("Los eventos de slashing no pueden ser negativos.");
    }

    public static ReputationScore initial(int nodeId) {
        return new ReputationScore(nodeId, 1.0, 0);
    }

    public ReputationScore penalize(double penalty) {
        if (penalty < 0.0 || penalty > 1.0) {
            throw new IllegalArgumentException("La penalidad debe estar entre 0 y 1.");
        }
        return new ReputationScore(nodeId, Math.max(0.0, score - penalty), slashingEvents + 1);
    }
}
