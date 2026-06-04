package dltlab.network;

/** Metricas legibles de un ataque eclipse simulado. */
public record EclipseAttackResult(
        int totalPeers,
        int controlledPeers,
        int victimNodes,
        int isolatedNodes,
        int hiddenBlocks,
        int censoredTransactions,
        double averageLatencyMs,
        double partitionProbability
) {
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("Eclipse attack P2P\n");
        sb.append("------------------\n");
        sb.append("Peers totales: ").append(totalPeers).append('\n');
        sb.append("Peers controlados: ").append(controlledPeers).append('\n');
        sb.append("Victimas observadas: ").append(victimNodes).append('\n');
        sb.append("Nodos aislados: ").append(isolatedNodes).append('\n');
        sb.append("Bloques ocultos: ").append(hiddenBlocks).append('\n');
        sb.append("Transacciones censuradas: ").append(censoredTransactions).append('\n');
        sb.append(String.format("Latencia promedio estimada: %.2f ms%n", averageLatencyMs));
        sb.append(String.format("Probabilidad de particion: %.2f%%%n", partitionProbability * 100.0));
        return sb.toString();
    }
}
