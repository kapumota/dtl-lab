package dltlab.consensus;

/** Metrica por ronda para observar convergencia y propagacion. */
public record ConsensusRoundMetric(
        int round,
        int totalMessages,
        int uniqueTransactionsPropagated,
        int largestHonestGroup,
        int honestNodes,
        double honestAgreementRatio,
        int honestOutputsContainingCensoredTx,
        int consensusGroups
) {}
