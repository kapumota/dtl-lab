package dltlab.consensus;

/** Parametros reproducibles de una simulacion avanzada de consenso. */
public record ConsensusConfig(
        int nodeCount,
        int rounds,
        double connectivityProbability,
        double maliciousProbability,
        double initialTransactionProbability,
        double honestThresholdRatio,
        double censoringShareAmongMalicious,
        double equivocatingShareAmongMalicious
) {
    public ConsensusConfig {
        if (nodeCount < 2) throw new IllegalArgumentException("La red debe tener al menos dos nodos.");
        if (rounds < 1) throw new IllegalArgumentException("La simulacion necesita al menos una ronda.");
        validateProbability(connectivityProbability, "connectivityProbability");
        validateProbability(maliciousProbability, "maliciousProbability");
        validateProbability(initialTransactionProbability, "initialTransactionProbability");
        validateProbability(honestThresholdRatio, "honestThresholdRatio");
        validateProbability(censoringShareAmongMalicious, "censoringShareAmongMalicious");
        validateProbability(equivocatingShareAmongMalicious, "equivocatingShareAmongMalicious");
        if (censoringShareAmongMalicious + equivocatingShareAmongMalicious > 1.0) {
            throw new IllegalArgumentException("Las proporciones maliciosas no pueden sumar mas de 1.");
        }
    }

    public static ConsensusConfig educationalDefault() {
        return new ConsensusConfig(
                48,
                10,
                0.38,
                0.30,
                0.50,
                0.30,
                0.45,
                0.35
        );
    }

    private static void validateProbability(double value, String name) {
        if (Double.isNaN(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " debe estar entre 0 y 1.");
        }
    }
}
