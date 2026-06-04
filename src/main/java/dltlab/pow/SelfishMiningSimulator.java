package dltlab.pow;

import java.util.Random;

/** Simulador compacto de selfish mining con lead privado y carreras publicas. */
public class SelfishMiningSimulator {
    private final SelfishMiningStrategy strategy;

    public SelfishMiningSimulator(SelfishMiningStrategy strategy) {
        this.strategy = strategy;
    }

    public MiningRewardMetrics simulate(HashPowerDistribution distribution, int rounds, long seed) {
        if (rounds < 1) {
            throw new IllegalArgumentException("La simulacion necesita al menos una ronda.");
        }

        Random random = new Random(seed);
        int privateLead = 0;
        int maxPrivateLead = 0;
        int privateBlocksMined = 0;
        int publicBlocksMined = 0;
        int attackerAccepted = 0;
        int honestAccepted = 0;
        int orphaned = 0;

        for (int round = 0; round < rounds; round++) {
            boolean attackerMines = random.nextDouble() < distribution.attackerShare();
            if (attackerMines) {
                privateLead++;
                privateBlocksMined++;
                maxPrivateLead = Math.max(maxPrivateLead, privateLead);
                continue;
            }

            publicBlocksMined++;
            SelfishMiningState state = strategy.stateForLead(privateLead);
            if (state == SelfishMiningState.PUBLIC_CHAIN_AHEAD) {
                honestAccepted++;
            } else if (state == SelfishMiningState.PUBLIC_RACE) {
                boolean attackerWinsRace = random.nextDouble() < strategy.networkAdvantage();
                if (attackerWinsRace) {
                    attackerAccepted++;
                    orphaned++;
                } else {
                    honestAccepted++;
                    orphaned++;
                }
                privateLead = 0;
            } else {
                attackerAccepted++;
                orphaned++;
                privateLead--;
            }
        }

        if (privateLead > 0) {
            attackerAccepted += privateLead;
        }

        int accepted = attackerAccepted + honestAccepted;
        int totalMined = privateBlocksMined + publicBlocksMined;
        double relativeRevenue = accepted == 0 ? 0.0 : attackerAccepted / (double) accepted;
        double orphanRate = totalMined == 0 ? 0.0 : orphaned / (double) totalMined;
        double expectedHonest = distribution.attackerShare();

        return new MiningRewardMetrics(
                distribution.attackerShare(),
                distribution.honestShare(),
                privateBlocksMined,
                publicBlocksMined,
                attackerAccepted,
                honestAccepted,
                orphaned,
                maxPrivateLead,
                expectedHonest,
                relativeRevenue,
                relativeRevenue,
                orphanRate,
                strategy.profitabilityThreshold()
        );
    }
}
