package dltlab.pow;

/** Metricas agregadas de una simulacion de selfish mining. */
public record MiningRewardMetrics(
        double attackerHashrate,
        double honestHashrate,
        int privateBlocksMined,
        int publicBlocksMined,
        int attackerAcceptedBlocks,
        int honestAcceptedBlocks,
        int orphanedBlocks,
        int maxPrivateLead,
        double honestExpectedRevenue,
        double attackerObservedRevenue,
        double relativeRevenue,
        double orphanRate,
        double profitabilityThreshold
) {
    public boolean attackerOutperformedHashrate() {
        return relativeRevenue > attackerHashrate;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("Selfish mining PoW\n");
        sb.append("-------------------\n");
        sb.append(String.format("Hashrate atacante: %.2f%%%n", attackerHashrate * 100.0));
        sb.append(String.format("Hashrate honesto: %.2f%%%n", honestHashrate * 100.0));
        sb.append("Bloques privados minados: ").append(privateBlocksMined).append('\n');
        sb.append("Bloques publicos honestos minados: ").append(publicBlocksMined).append('\n');
        sb.append("Bloques aceptados del atacante: ").append(attackerAcceptedBlocks).append('\n');
        sb.append("Bloques aceptados honestos: ").append(honestAcceptedBlocks).append('\n');
        sb.append("Bloques huerfanos: ").append(orphanedBlocks).append('\n');
        sb.append("Maximo lead privado: ").append(maxPrivateLead).append('\n');
        sb.append(String.format("Revenue honesto esperado: %.4f%n", honestExpectedRevenue));
        sb.append(String.format("Revenue atacante observado: %.4f%n", attackerObservedRevenue));
        sb.append(String.format("Revenue relativo atacante: %.2f%%%n", relativeRevenue * 100.0));
        sb.append(String.format("Orphan rate: %.2f%%%n", orphanRate * 100.0));
        sb.append(String.format("Umbral teorico aproximado: %.2f%%%n", profitabilityThreshold * 100.0));
        sb.append("Atacante supera su hashrate: ").append(attackerOutperformedHashrate()).append('\n');
        return sb.toString();
    }
}
