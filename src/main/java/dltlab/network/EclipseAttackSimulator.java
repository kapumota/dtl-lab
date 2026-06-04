package dltlab.network;

import java.util.Set;

/** Simula aislamiento de victimas cuando sus vecinos visibles son controlados. */
public class EclipseAttackSimulator {
    private final MessagePropagationSimulator propagationSimulator = new MessagePropagationSimulator();

    public EclipseAttackResult simulate(PeerTable table,
                                        Set<Integer> victimNodeIds,
                                        Set<Integer> controlledPeerIds,
                                        int hiddenBlocks,
                                        int censoredTransactions,
                                        long baseLatencyMs) {
        if (victimNodeIds.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos una victima.");
        }
        for (int victim : victimNodeIds) table.peer(victim);
        for (int controlled : controlledPeerIds) table.peer(controlled);

        int isolated = 0;
        for (int victim : victimNodeIds) {
            Set<Integer> neighbors = table.neighborsOf(victim);
            if (!neighbors.isEmpty() && controlledPeerIds.containsAll(neighbors)) {
                isolated++;
            }
        }

        double partitionProbability = isolated / (double) victimNodeIds.size();
        int source = victimNodeIds.iterator().next();
        double averageLatency = propagationSimulator.estimateAverageLatency(table, source, controlledPeerIds, baseLatencyMs);

        return new EclipseAttackResult(
                table.peerCount(),
                controlledPeerIds.size(),
                victimNodeIds.size(),
                isolated,
                hiddenBlocks,
                censoredTransactions,
                averageLatency,
                partitionProbability
        );
    }

    public NetworkPartition partition(PeerTable table,
                                      Set<Integer> victimNodeIds,
                                      Set<Integer> controlledPeerIds,
                                      int hiddenBlocks,
                                      int censoredTransactions,
                                      long baseLatencyMs) {
        EclipseAttackResult result = simulate(table, victimNodeIds, controlledPeerIds, hiddenBlocks, censoredTransactions, baseLatencyMs);
        return new NetworkPartition(
                victimNodeIds,
                controlledPeerIds,
                result.isolatedNodes(),
                result.hiddenBlocks(),
                result.censoredTransactions(),
                result.averageLatencyMs(),
                result.partitionProbability()
        );
    }
}
