package dltlab.simulation;

import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;

import java.util.EnumSet;
import java.util.Set;

/** Ejecuta la matriz reducida o local de seeds para los diez escenarios. */
public final class SimulationScenarioMatrixTest {
    private SimulationScenarioMatrixTest() {
    }

    public static void main(String[] args) {
        int seeds = resolveSeeds(args);
        Set<CrossShardStatus> raceOutcomes = EnumSet.noneOf(CrossShardStatus.class);
        for (SimulationScenario scenario : SimulationScenario.values()) {
            for (long seed = 0; seed < seeds; seed++) {
                SimulationRun run = ScenarioCatalog.run(scenario, seed);
                verifyCommonProperties(scenario, run);
                verifyScenario(scenario, run);
                if (scenario == SimulationScenario.S05_COMMIT_TIMEOUT_SAME_ROUND) {
                    raceOutcomes.add(run.manager().getSessions().get(0).status());
                }
            }
        }
        if (seeds >= 2) {
            assertTrue(raceOutcomes.contains(CrossShardStatus.COMMITTED),
                    "La carrera debe explorar una ejecucion donde gana el commit.");
            assertTrue(raceOutcomes.contains(CrossShardStatus.TIMED_OUT),
                    "La carrera debe explorar una ejecucion donde gana el timeout.");
        }
        System.out.println("La matriz determinista paso " + seeds
                + " seeds por cada uno de los diez escenarios.");
    }

    private static int resolveSeeds(String[] args) {
        String value = args.length > 0 ? args[0] : System.getenv("DTL_SIMULATION_SEEDS");
        int seeds = value == null || value.isBlank() ? 100 : Integer.parseInt(value);
        if (seeds <= 0) {
            throw new IllegalArgumentException("La cantidad de seeds debe ser positiva.");
        }
        return seeds;
    }

    private static void verifyCommonProperties(SimulationScenario scenario, SimulationRun run) {
        assertTrue(!run.trace().isEmpty(), "La traza no debe estar vacia en " + scenario + ".");
        assertEquals(run.clock().now(), run.manager().getCurrentRound(),
                "El reloj de simulacion y el protocolo deben coincidir en " + scenario + ".");
        for (CrossShardSession session : run.manager().getSessions()) {
            assertTrue(session.isTerminal(),
                    "Toda sesion debe terminar en un estado terminal en " + scenario + ".");
            boolean locked = run.manager().getShard(session.transfer().sourceShardId())
                    .isLocked(session.transfer().sourceUtxo().key());
            assertTrue(!locked, "Una sesion terminal no debe dejar bloqueos en " + scenario + ".");
        }
    }

    private static void verifyScenario(SimulationScenario scenario, SimulationRun run) {
        switch (scenario) {
            case S01_NORMAL_COMMIT -> {
                assertSessionCount(run, 1);
                assertAllStatus(run, CrossShardStatus.COMMITTED);
            }
            case S02_TIMEOUT_BEFORE_DELIVERY -> {
                assertSessionCount(run, 1);
                assertAllStatus(run, CrossShardStatus.TIMED_OUT);
                assertTrue(run.traceContains(SimulationEventType.DROP_MESSAGE),
                        "S02 debe registrar la perdida del recibo.");
            }
            case S03_DUPLICATED_RECEIPT -> {
                assertSessionCount(run, 1);
                assertAllStatus(run, CrossShardStatus.COMMITTED);
                assertTrue(run.traceContains(SimulationEventType.DUPLICATE_MESSAGE),
                        "S03 debe registrar duplicacion de mensaje.");
            }
            case S04_DELAYED_AFTER_TIMEOUT -> {
                assertSessionCount(run, 1);
                assertAllStatus(run, CrossShardStatus.TIMED_OUT);
                assertTrue(run.traceContains(SimulationEventType.DELAY_MESSAGE),
                        "S04 debe registrar retraso de mensaje.");
            }
            case S05_COMMIT_TIMEOUT_SAME_ROUND -> {
                assertSessionCount(run, 1);
                CrossShardStatus status = run.manager().getSessions().get(0).status();
                assertTrue(status == CrossShardStatus.COMMITTED || status == CrossShardStatus.TIMED_OUT,
                        "S05 debe tener una unica decision terminal valida.");
            }
            case S06_SAME_UTXO_CONFLICT -> {
                assertSessionCount(run, 1);
                assertAllStatus(run, CrossShardStatus.COMMITTED);
                assertTrue(run.trace().stream().anyMatch(entry -> "ERROR".equals(entry.outcome())),
                        "S06 debe rechazar uno de los inicios sobre el mismo UTXO.");
            }
            case S07_BIDIRECTIONAL_TRANSFERS -> {
                assertSessionCount(run, 2);
                assertAllStatus(run, CrossShardStatus.COMMITTED);
            }
            case S08_TEMPORARY_TARGET_OUTAGE -> {
                assertSessionCount(run, 1);
                assertAllStatus(run, CrossShardStatus.COMMITTED);
                assertTrue(run.traceContains(SimulationEventType.SHARD_OFFLINE),
                        "S08 debe registrar la caida temporal.");
                assertTrue(run.traceContains(SimulationEventType.SHARD_ONLINE),
                        "S08 debe registrar la recuperacion del shard.");
            }
            case S09_INSUFFICIENT_QUORUM -> {
                assertSessionCount(run, 1);
                assertAllStatus(run, CrossShardStatus.FAILED_VALIDATION);
            }
            case S10_MULTIPLE_CONCURRENT_SESSIONS -> {
                assertSessionCount(run, 6);
                assertAllStatus(run, CrossShardStatus.COMMITTED);
            }
        }
    }

    private static void assertSessionCount(SimulationRun run, int expected) {
        assertEquals(expected, run.manager().getSessions().size(),
                "La cantidad de sesiones no coincide.");
    }

    private static void assertAllStatus(SimulationRun run, CrossShardStatus expected) {
        for (CrossShardSession session : run.manager().getSessions()) {
            assertEquals(expected, session.status(),
                    "El estado final de la sesion no coincide.");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " Esperado: " + expected + ". Actual: " + actual + ".");
        }
    }
}
