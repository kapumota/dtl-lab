package dltlab.consensus;

/** Evidencia de dos mensajes incompatibles firmados por el mismo nodo. */
public record EquivocationEvidence(SignedConsensusMessage first, SignedConsensusMessage second) {
    public EquivocationEvidence {
        if (first == null || second == null) {
            throw new IllegalArgumentException("La evidencia necesita dos mensajes.");
        }
        if (!isEquivocation(first, second)) {
            throw new IllegalArgumentException("Los mensajes no forman evidencia de equivocacion.");
        }
    }

    public static boolean isEquivocation(SignedConsensusMessage first, SignedConsensusMessage second) {
        return first.verifies()
                && second.verifies()
                && first.nodeId() == second.nodeId()
                && first.round() == second.round()
                && first.topic().equals(second.topic())
                && !first.value().equals(second.value());
    }

    public int nodeId() {
        return first.nodeId();
    }

    public int round() {
        return first.round();
    }

    public String render() {
        return "Equivocacion detectada: nodo=" + nodeId()
                + ", ronda=" + round()
                + ", topico=" + first.topic()
                + ", valor_a=" + first.value()
                + ", valor_b=" + second.value();
    }
}
