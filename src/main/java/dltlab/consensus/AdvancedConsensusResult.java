package dltlab.consensus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Resultado detallado de consenso avanzado. */
public record AdvancedConsensusResult(
        int totalNodes,
        int honestNodes,
        int maliciousNodes,
        int censoringNodes,
        int equivocatingNodes,
        int silentNodes,
        int largestConsensusGroup,
        int agreedTransactionCount,
        String censoredTransactionId,
        int honestOutputsContainingCensoredTx,
        TrustGraph trustGraph,
        List<NodeProfile> nodeProfiles,
        List<ConsensusRoundMetric> roundMetrics,
        Map<String, Integer> finalGroupSizes
) {
    public AdvancedConsensusResult {
        nodeProfiles = List.copyOf(nodeProfiles);
        roundMetrics = List.copyOf(roundMetrics);
        finalGroupSizes = Map.copyOf(finalGroupSizes);
    }

    public double honestAgreementRatio() {
        return honestNodes == 0 ? 0.0 : largestConsensusGroup / (double) honestNodes;
    }

    public double censorshipSuccessRatio() {
        return honestNodes == 0 ? 0.0 : 1.0 - (honestOutputsContainingCensoredTx / (double) honestNodes);
    }

    public List<ConsensusRoundMetric> immutableRoundMetrics() {
        return Collections.unmodifiableList(roundMetrics);
    }
}
