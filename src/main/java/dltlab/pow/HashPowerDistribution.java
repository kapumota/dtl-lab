package dltlab.pow;

/** Distribucion de poder de hash entre atacante y red honesta. */
public record HashPowerDistribution(double attackerShare, double honestShare) {
    public HashPowerDistribution {
        if (attackerShare <= 0.0 || attackerShare >= 1.0) {
            throw new IllegalArgumentException("El atacante debe tener una fraccion de hash entre 0 y 1.");
        }
        if (honestShare <= 0.0 || honestShare >= 1.0) {
            throw new IllegalArgumentException("La red honesta debe tener una fraccion de hash entre 0 y 1.");
        }
        double total = attackerShare + honestShare;
        if (Math.abs(total - 1.0) > 0.000001) {
            throw new IllegalArgumentException("Las fracciones de hash deben sumar 1.");
        }
    }

    public static HashPowerDistribution fromAttackerShare(double attackerShare) {
        return new HashPowerDistribution(attackerShare, 1.0 - attackerShare);
    }
}
