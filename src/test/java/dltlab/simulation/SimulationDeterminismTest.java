package dltlab.simulation;

/** Pruebas de repetibilidad exacta para reloj, orden y traza. */
public final class SimulationDeterminismTest {
    private SimulationDeterminismTest() {
    }

    public static void main(String[] args) {
        testSameSeedProducesSameTrace();
        testSchedulerUsesStableTieBreaking();
        testAbortEventReleasesSource();
        testClockRejectsBackwardMovement();
        System.out.println("Las pruebas de determinismo de simulacion pasaron correctamente.");
    }

    private static void testSameSeedProducesSameTrace() {
        for (SimulationScenario scenario : SimulationScenario.values()) {
            for (long seed = 0; seed < 10; seed++) {
                SimulationRun first = ScenarioCatalog.run(scenario, seed);
                SimulationRun second = ScenarioCatalog.run(scenario, seed);
                assertEquals(first.traceText(), second.traceText(),
                        "La misma seed debe producir la misma traza en " + scenario + ".");
                assertEquals(first.traceHash(), second.traceHash(),
                        "La misma seed debe producir el mismo hash en " + scenario + ".");
                assertEquals(first.finalStateText(), second.finalStateText(),
                        "La misma seed debe producir el mismo estado final en " + scenario + ".");
            }
        }
    }

    private static void testSchedulerUsesStableTieBreaking() {
        SimulationRun run = new SimulationRun(2, 4, 3, 42L, new NoFaultModel());
        StringBuilder order = new StringBuilder();
        run.schedule(1, 0, SimulationEventType.SEND_RECEIPT, "a", "Primero.", () -> order.append('A'));
        run.schedule(1, 0, SimulationEventType.SEND_RECEIPT, "b", "Segundo.", () -> order.append('B'));
        run.schedule(1, -1, SimulationEventType.SEND_RECEIPT, "c", "Prioridad menor.", () -> order.append('C'));
        run.runAll();
        assertEquals("CAB", order.toString(),
                "La cola debe ordenar por ronda, prioridad y secuencia.");
    }

    private static void testAbortEventReleasesSource() {
        SimulationRun run = ScenarioCatalog.create(SimulationScenario.S01_NORMAL_COMMIT, 77L);
        String transferId = run.scheduler().queue().snapshot().stream()
                .filter(event -> event.type() == SimulationEventType.BEGIN_TRANSFER)
                .findFirst()
                .orElseThrow()
                .transferId();
        run.scheduleAbort(transferId, "Abort de prueba determinista.", 1, -20);
        run.runAll();
        assertTrue(run.traceContains(SimulationEventType.ABORT_TRANSFER),
                "La simulacion debe registrar el abort.");
        assertTrue(run.traceContains(SimulationEventType.RELEASE_SOURCE),
                "El abort debe registrar la liberacion del origen.");
    }

    private static void testClockRejectsBackwardMovement() {
        SimulationClock clock = new SimulationClock();
        clock.advanceTo(3);
        assertThrows(IllegalArgumentException.class, () -> clock.advanceTo(2),
                "El reloj no debe depender de correcciones hacia atras.");
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

    private static void assertThrows(Class<? extends Throwable> expected, Runnable action,
                                     String message) {
        try {
            action.run();
        } catch (Throwable error) {
            if (expected.isInstance(error)) {
                return;
            }
            throw new AssertionError(message + " Se obtuvo " + error.getClass().getSimpleName() + ".", error);
        }
        throw new AssertionError(message + " No se produjo la excepcion esperada.");
    }
}
