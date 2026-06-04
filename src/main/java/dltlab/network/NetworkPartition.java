package dltlab.network;

import java.util.Set;

/** Resultado agregado de una particion o aislamiento P2P. */
public record NetworkPartition(
        Set<Integer> victimNodeIds,
        Set<Integer> controlledPeerIds,
        int isolatedNodes,
        int hiddenBlocks,
        int censoredTransactions,
        double averageLatencyMs,
        double partitionProbability
) {
    public NetworkPartition {
        victimNodeIds = Set.copyOf(victimNodeIds);
        controlledPeerIds = Set.copyOf(controlledPeerIds);
        if (isolatedNodes < 0 || hiddenBlocks < 0 || censoredTransactions < 0) {
            throw new IllegalArgumentException("Las metricas de ataque no pueden ser negativas.");
        }
    }
}
