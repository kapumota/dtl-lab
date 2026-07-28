package dltlab.conformance;

import dltlab.simulation.ScenarioCatalog;
import dltlab.simulation.SimulationRun;
import dltlab.simulation.SimulationScenario;
import dltlab.trace.TraceExecution;
import dltlab.trace.TraceRecorder;

import java.nio.file.Path;

/** Pruebas estructurales del generador y parser de replay TLC. */
public final class TraceReplayGeneratorTest {
    private TraceReplayGeneratorTest() {
    }

    public static void main(String[] args) {
        testAllScenariosGenerateDeterministicReplay();
        testNormalCommitUsesFormalOperators();
        testTimeoutUsesFormalOperator();
        testGeneratorDoesNotReimplementNext();
        testAcceptedOutputIsParsed();
        testRejectedStepIsLocated();
        System.out.println(
                "Las pruebas del generador de replay TLC pasaron correctamente."
        );
    }

    private static void testAllScenariosGenerateDeterministicReplay() {
        TlcTraceReplayGenerator generator = new TlcTraceReplayGenerator();
        for (SimulationScenario scenario : SimulationScenario.values()) {
            AbstractTrace first = abstractTrace(scenario, 2026L);
            AbstractTrace second = abstractTrace(scenario, 2026L);
            TlcReplayArtifact firstArtifact = generator.generate(first);
            TlcReplayArtifact secondArtifact = generator.generate(second);

            assertEquals(
                    firstArtifact,
                    secondArtifact,
                    "La misma seed debe generar el mismo replay para "
                            + scenario
                            + "."
            );
            assertEquals(
                    first.steps().size(),
                    firstArtifact.stepCount(),
                    "El artefacto debe conservar todos los pasos abstractos."
            );
            assertTrue(
                    firstArtifact.moduleText().contains(
                            "ReplayEventuallyComplete"
                    ),
                    "El módulo debe exigir completar el replay."
            );
            assertTrue(
                    firstArtifact.configText().contains(
                            "PROPERTY ReplayEventuallyComplete"
                    ),
                    "La configuración debe comprobar la terminación del replay."
            );
            assertFalse(
                    firstArtifact.moduleText().contains("=".repeat(8)),
                    "El módulo no debe contener separadores no permitidos."
            );
            assertFalse(
                    firstArtifact.moduleText().contains("\u2013")
                            || firstArtifact.moduleText().contains("\u2014"),
                    "El módulo no debe contener guiones tipográficos."
            );
        }
    }

    private static void testNormalCommitUsesFormalOperators() {
        TlcReplayArtifact artifact = new TlcTraceReplayGenerator().generate(
                abstractTrace(SimulationScenario.S01_NORMAL_COMMIT, 77L)
        );
        String module = artifact.moduleText();
        assertTrue(
                module.contains("LockTransfer("),
                "El replay de commit debe invocar LockTransfer."
        );
        assertTrue(
                module.contains("ConsumeReceipt("),
                "El replay de commit debe invocar ConsumeReceipt."
        );
        assertTrue(
                module.contains("CastVote("),
                "El replay de commit debe invocar CastVote."
        );
        assertTrue(
                module.contains("CommitTransfer("),
                "El replay de commit debe invocar CommitTransfer."
        );
        assertTrue(
                artifact.configText().contains("ReceiptCopies = 1"),
                "La configuración debe conservar la copia canónica."
        );
    }

    private static void testTimeoutUsesFormalOperator() {
        TlcReplayArtifact artifact = new TlcTraceReplayGenerator().generate(
                abstractTrace(
                        SimulationScenario.S02_TIMEOUT_BEFORE_DELIVERY,
                        91L
                )
        );
        assertTrue(
                artifact.moduleText().contains("TimeoutTransfer("),
                "El replay de timeout debe invocar TimeoutTransfer."
        );
    }

    private static void testGeneratorDoesNotReimplementNext() {
        String module = new TlcTraceReplayGenerator()
                .generate(abstractTrace(
                        SimulationScenario.S01_NORMAL_COMMIT,
                        19L
                ))
                .moduleText();
        assertTrue(
                module.contains("EXTENDS CrossShardCommit"),
                "El replay debe reutilizar la especificación base."
        );
        assertFalse(
                module.contains("LockTransfer(t) =="),
                "El replay no debe redefinir LockTransfer."
        );
        assertFalse(
                module.contains("CanCommit(t) =="),
                "El replay no debe duplicar las guardas de commit."
        );
        assertFalse(
                module.contains("\nNext =="),
                "El replay no debe redefinir Next."
        );
    }

    private static void testAcceptedOutputIsParsed() {
        AbstractTrace trace = abstractTrace(
                SimulationScenario.S01_NORMAL_COMMIT,
                31L
        );
        ConformanceResult result = new TlcReplayResultParser().parse(
                trace,
                0,
                "Model checking completed. No error has been found.",
                "",
                Path.of("Replay.tla"),
                Path.of("Replay.cfg"),
                Path.of("stdout.txt"),
                Path.of("stderr.txt")
        );
        assertTrue(result.accepted(), "La salida correcta de TLC debe aceptarse.");
        assertEquals(
                trace.steps().size(),
                result.checkedAbstractSteps(),
                "TLC debe haber revisado todos los pasos."
        );
    }

    private static void testRejectedStepIsLocated() {
        AbstractTrace trace = abstractTrace(
                SimulationScenario.S01_NORMAL_COMMIT,
                41L
        );
        int rejectedIndex = Math.min(3, trace.steps().size() - 1);
        String output = "Error: Temporal properties were violated.\n"
                + "State 4: replay detenido\n"
                + "/\\ replayIndex = "
                + rejectedIndex
                + "\n";
        ConformanceResult result = new TlcReplayResultParser().parse(
                trace,
                12,
                output,
                "",
                Path.of("Replay.tla"),
                Path.of("Replay.cfg"),
                Path.of("stdout.txt"),
                Path.of("stderr.txt")
        );
        AbstractTraceStep expected = trace.steps().get(rejectedIndex);
        assertFalse(result.accepted(), "La violación temporal debe rechazarse.");
        assertEquals(
                rejectedIndex,
                result.rejectedAbstractStep(),
                "El parser debe recuperar el índice rechazado."
        );
        assertEquals(
                expected.concreteStep(),
                result.rejectedConcreteStep(),
                "El diagnóstico debe conservar el paso concreto."
        );
        assertEquals(
                expected.action().tlaName(),
                result.rejectedAction(),
                "El diagnóstico debe conservar la acción formal."
        );
    }

    private static AbstractTrace abstractTrace(
            SimulationScenario scenario,
            long seed
    ) {
        SimulationRun run = ScenarioCatalog.run(scenario, seed);
        TraceExecution execution = new TraceRecorder().record(scenario, run);
        return new JavaToTlaStateMapper().map(execution);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(
            Object expected,
            Object actual,
            String message
    ) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(
                    message
                            + " Esperado: "
                            + expected
                            + ". Actual: "
                            + actual
                            + "."
            );
        }
    }
}
