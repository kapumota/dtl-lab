package dltlab.consensus;

/** Metricas finales de la simulacion de consenso. */
public record ConsensusResult(int totalNodes, int maliciousNodes, int honestNodes,
                              int largestConsensusGroup, int agreedTransactionCount) {
    public double honestAgreementRatio() {
        return honestNodes == 0 ? 0.0 : largestConsensusGroup / (double) honestNodes;
    }
}
