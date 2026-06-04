package dltlab.pow;

/** Estrategia educativa para decidir cuando publicar bloques privados. */
public class SelfishMiningStrategy {
    private final double networkAdvantage;

    public SelfishMiningStrategy(double networkAdvantage) {
        if (networkAdvantage < 0.0 || networkAdvantage > 1.0) {
            throw new IllegalArgumentException("La ventaja de red debe estar entre 0 y 1.");
        }
        this.networkAdvantage = networkAdvantage;
    }

    public static SelfishMiningStrategy educationalDefault() {
        return new SelfishMiningStrategy(0.60);
    }

    public double networkAdvantage() {
        return networkAdvantage;
    }

    public SelfishMiningState stateForLead(int privateLead) {
        if (privateLead <= 0) return SelfishMiningState.PUBLIC_CHAIN_AHEAD;
        if (privateLead == 1) return SelfishMiningState.PUBLIC_RACE;
        return SelfishMiningState.PRIVATE_LEAD;
    }

    public double profitabilityThreshold() {
        return (1.0 - networkAdvantage) / (3.0 - 2.0 * networkAdvantage);
    }
}
