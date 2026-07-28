package dltlab.conformance;

import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.protocol.ProtocolAction;
import dltlab.simulation.ScenarioCatalog;
import dltlab.simulation.SimulationEventType;
import dltlab.simulation.SimulationRun;
import dltlab.simulation.SimulationScenario;
import dltlab.trace.TraceEvent;
import dltlab.trace.TraceExecution;
import dltlab.trace.TraceRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Pruebas de la función de abstracción implementada en la Fase 7B. */
public final class TraceAbstractionTest {
    private TraceAbstractionTest() {
    }

    public static void main(String[] args) {
        testAllScenariosAreAbstractedDeterministically();
        testNormalCommitExpandsVotes();
        testTimeoutAndQuorumFailure();
        testNetworkObservationsAreStutter();
        testIdentityMutationIsRejected();
        testFirstTerminalDecisionIsPreserved();
        testStatusVocabulary();
        System.out.println(
                "Las pruebas de la función de abstracción Java-TLA+ "
                        + "pasaron correctamente."
        );
    }

    private static void testAllScenariosAreAbstractedDeterministically() {
        JavaToTlaStateMapper mapper = new JavaToTlaStateMapper();
        for (SimulationScenario scenario : SimulationScenario.values()) {
            TraceExecution firstExecution = record(scenario, 2026L);
            TraceExecution secondExecution = record(scenario, 2026L);
            AbstractTrace first = mapper.map(firstExecution);
            AbstractTrace second = mapper.map(secondExecution);

            assertEquals(
                    first,
                    second,
                    "La misma seed debe producir la misma traza abstracta en "
                            + scenario
                            + "."
            );
            assertEquals(
                    firstExecution.finalStates().keySet(),
                    first.initialState().transfers().keySet(),
                    "El estado inicial debe conservar todas las transferencias."
            );
            assertTrue(
                    first.steps().size() >= firstExecution.events().size(),
                    "Cada evento concreto debe producir al menos un paso abstracto."
            );
            assertEquals(
                    firstExecution.events().size() - 1L,
                    first.steps().get(first.steps().size() - 1).concreteStep(),
                    "La traza abstracta debe conservar el último paso concreto."
            );
        }
    }

    private static void testNormalCommitExpandsVotes() {
        TraceExecution execution = record(
                SimulationScenario.S01_NORMAL_COMMIT,
                77L
        );
        AbstractTrace trace = new JavaToTlaStateMapper().map(execution);

        TraceEvent preparation = execution.events().stream()
                .filter(event -> event.javaAction()
                        == ProtocolAction.PREPARE_DESTINATION)
                .findFirst()
                .orElseThrow();
        List<AbstractTraceStep> expansion = trace.steps().stream()
                .filter(step -> step.concreteStep() == preparation.step())
                .toList();

        assertEquals(
                AbstractAction.Kind.CONSUME_RECEIPT,
                expansion.get(0).action().kind(),
                "La preparación debe comenzar con ConsumeReceipt."
        );
        assertEquals(
                preparation.targetApprovals() + 1,
                expansion.size(),
                "La preparación debe expandir todas las aprobaciones."
        );
        for (int index = 1; index < expansion.size(); index++) {
            AbstractAction action = expansion.get(index).action();
            assertEquals(
                    AbstractAction.Kind.CAST_VOTE,
                    action.kind(),
                    "Cada aprobación adicional debe ser CastVote."
            );
            assertEquals(
                    "v" + index,
                    action.validatorId(),
                    "Los validadores canónicos deben conservar un orden estable."
            );
            assertTrue(
                    action.synthetic(),
                    "Los votos expandidos deben declararse sintéticos."
            );
        }

        String transferId = execution.finalStates().keySet().iterator().next();
        AbstractProtocolState.TransferState finalTransfer = trace
                .finalState()
                .transfer(transferId);
        assertEquals(
                AbstractProtocolState.Status.COMMITTED,
                finalTransfer.status(),
                "El commit normal debe terminar en Committed."
        );
        assertEquals(
                AbstractProtocolState.TerminalStatus.COMMITTED,
                finalTransfer.terminalStatus(),
                "El commit normal debe conservar su primera decisión terminal."
        );
        assertEquals(
                1,
                finalTransfer.receiptUseCount(),
                "El recibo abstracto debe consumirse una vez."
        );
        assertTrue(
                finalTransfer.destinationCredit(),
                "El commit debe conservar el crédito abstracto del destino."
        );
        assertFalse(
                finalTransfer.fundsReleased(),
                "El commit no debe liberar fondos en el estado abstracto."
        );
        assertTrue(
                finalTransfer.votes().size() >= execution.quorum(),
                "El commit normal debe conservar al menos el quorum de votos."
        );
    }

    private static void testTimeoutAndQuorumFailure() {
        JavaToTlaStateMapper mapper = new JavaToTlaStateMapper();
        AbstractTrace timeout = mapper.map(record(
                SimulationScenario.S02_TIMEOUT_BEFORE_DELIVERY,
                91L
        ));
        assertTrue(
                containsAction(timeout, AbstractAction.Kind.TIMEOUT_TRANSFER),
                "El timeout concreto debe proyectarse como TimeoutTransfer."
        );
        assertAbortedAndReleased(timeout);

        AbstractTrace quorumFailure = mapper.map(record(
                SimulationScenario.S09_INSUFFICIENT_QUORUM,
                91L
        ));
        assertTrue(
                containsAction(
                        quorumFailure,
                        AbstractAction.Kind.TIMEOUT_TRANSFER
                ),
                "El fallo terminal de quorum debe proyectarse por sus efectos."
        );
        assertFalse(
                containsAction(
                        quorumFailure,
                        AbstractAction.Kind.COMMIT_TRANSFER
                ),
                "El escenario sin quorum no debe producir CommitTransfer."
        );
        assertAbortedAndReleased(quorumFailure);
    }

    private static void testNetworkObservationsAreStutter() {
        assertSimulationTypeMapsToStutter(
                SimulationScenario.S02_TIMEOUT_BEFORE_DELIVERY,
                SimulationEventType.DROP_MESSAGE
        );
        assertSimulationTypeMapsToStutter(
                SimulationScenario.S03_DUPLICATED_RECEIPT,
                SimulationEventType.DUPLICATE_MESSAGE
        );
        assertSimulationTypeMapsToStutter(
                SimulationScenario.S04_DELAYED_AFTER_TIMEOUT,
                SimulationEventType.DELAY_MESSAGE
        );
        assertSimulationTypeMapsToStutter(
                SimulationScenario.S08_TEMPORARY_TARGET_OUTAGE,
                SimulationEventType.SHARD_OFFLINE
        );
        assertSimulationTypeMapsToStutter(
                SimulationScenario.S08_TEMPORARY_TARGET_OUTAGE,
                SimulationEventType.SHARD_ONLINE
        );
    }

    private static void testIdentityMutationIsRejected() {
        TraceEvent created = event(
                0L,
                ProtocolAction.CREATE_SESSION,
                null,
                CrossShardStatus.CREATED,
                0,
                1
        );
        TraceEvent lockedWithChangedTarget = event(
                1L,
                ProtocolAction.LOCK_SOURCE,
                CrossShardStatus.CREATED,
                CrossShardStatus.SOURCE_LOCKED,
                0,
                2
        );
        TraceExecution execution = new TraceExecution(
                1,
                "ESCENARIO_IDENTIDAD_MUTADA",
                1L,
                3,
                2,
                "sha256:" + "0".repeat(64),
                List.of(created, lockedWithChangedTarget),
                Map.of("transferencia-prueba", CrossShardStatus.SOURCE_LOCKED)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new JavaToTlaStateMapper().map(execution),
                "La abstracción debe rechazar cambios de shard."
        );
    }

    private static void testFirstTerminalDecisionIsPreserved() {
        List<TraceEvent> events = new ArrayList<>();
        events.add(event(
                0L,
                ProtocolAction.CREATE_SESSION,
                null,
                CrossShardStatus.CREATED,
                0,
                1
        ));
        events.add(event(
                1L,
                ProtocolAction.LOCK_SOURCE,
                CrossShardStatus.CREATED,
                CrossShardStatus.SOURCE_LOCKED,
                0,
                1
        ));
        events.add(event(
                2L,
                ProtocolAction.EXPIRE_TRANSFER,
                CrossShardStatus.SOURCE_LOCKED,
                CrossShardStatus.TIMED_OUT,
                0,
                1
        ));
        events.add(event(
                3L,
                ProtocolAction.COMMIT_DESTINATION,
                CrossShardStatus.TIMED_OUT,
                CrossShardStatus.COMMITTED,
                0,
                1
        ));
        TraceExecution execution = new TraceExecution(
                1,
                "ESCENARIO_DECISION_TERMINAL",
                1L,
                2,
                2,
                "sha256:" + "0".repeat(64),
                events,
                Map.of("transferencia-prueba", CrossShardStatus.COMMITTED)
        );

        AbstractProtocolState.TransferState transfer =
                new JavaToTlaStateMapper()
                        .map(execution)
                        .finalState()
                        .transfer("transferencia-prueba");
        assertEquals(
                AbstractProtocolState.Status.COMMITTED,
                transfer.status(),
                "La proyección debe conservar el estado final observado."
        );
        assertEquals(
                AbstractProtocolState.TerminalStatus.ABORTED,
                transfer.terminalStatus(),
                "La primera decisión terminal no debe sobrescribirse."
        );
    }

    private static void testStatusVocabulary() {
        JavaToTlaStateMapper mapper = new JavaToTlaStateMapper();
        assertEquals(
                AbstractProtocolState.Status.PENDING,
                mapper.mapStatus(CrossShardStatus.CREATED),
                "CREATED debe proyectarse como Pending."
        );
        assertEquals(
                AbstractProtocolState.Status.LOCKED,
                mapper.mapStatus(CrossShardStatus.RECEIPT_DELIVERED),
                "RECEIPT_DELIVERED debe proyectarse como Locked."
        );
        assertEquals(
                AbstractProtocolState.Status.PREPARED,
                mapper.mapStatus(CrossShardStatus.DESTINATION_PREPARED),
                "DESTINATION_PREPARED debe proyectarse como Prepared."
        );
        assertEquals(
                AbstractProtocolState.Status.COMMITTED,
                mapper.mapStatus(CrossShardStatus.COMMITTED),
                "COMMITTED debe proyectarse como Committed."
        );
        assertEquals(
                AbstractProtocolState.Status.ABORTED,
                mapper.mapStatus(CrossShardStatus.FAILED_VALIDATION),
                "FAILED_VALIDATION debe proyectarse como Aborted."
        );
    }

    private static void assertSimulationTypeMapsToStutter(
            SimulationScenario scenario,
            SimulationEventType type
    ) {
        TraceExecution execution = record(scenario, 71L);
        AbstractTrace trace = new JavaToTlaStateMapper().map(execution);
        List<Long> concreteSteps = execution.events().stream()
                .filter(event -> event.simulationEventType() == type)
                .map(TraceEvent::step)
                .toList();
        assertTrue(
                !concreteSteps.isEmpty(),
                "El escenario debe contener el evento " + type + "."
        );
        for (long concreteStep : concreteSteps) {
            List<AbstractTraceStep> mapped = trace.steps().stream()
                    .filter(step -> step.concreteStep() == concreteStep)
                    .toList();
            assertEquals(
                    1,
                    mapped.size(),
                    "Una observación de red debe producir un solo paso."
            );
            assertEquals(
                    AbstractAction.Kind.STUTTER,
                    mapped.get(0).action().kind(),
                    "La observación de red debe proyectarse como Stutter."
            );
        }
    }

    private static void assertAbortedAndReleased(AbstractTrace trace) {
        for (AbstractProtocolState.TransferState transfer
                : trace.finalState().transfers().values()) {
            assertEquals(
                    AbstractProtocolState.Status.ABORTED,
                    transfer.status(),
                    "La decisión terminal sin commit debe terminar en Aborted."
            );
            assertEquals(
                    AbstractProtocolState.TerminalStatus.ABORTED,
                    transfer.terminalStatus(),
                    "La primera decisión terminal debe ser Aborted."
            );
            assertFalse(
                    transfer.locked(),
                    "El estado abstracto abortado debe liberar el origen."
            );
            assertTrue(
                    transfer.fundsReleased(),
                    "El estado abstracto abortado debe registrar la liberación."
            );
        }
    }

    private static boolean containsAction(
            AbstractTrace trace,
            AbstractAction.Kind kind
    ) {
        return trace.steps().stream().anyMatch(
                step -> step.action().kind() == kind
        );
    }

    private static TraceExecution record(
            SimulationScenario scenario,
            long seed
    ) {
        SimulationRun run = ScenarioCatalog.run(scenario, seed);
        return new TraceRecorder().record(scenario, run);
    }

    private static TraceEvent event(
            long step,
            ProtocolAction action,
            CrossShardStatus previousStatus,
            CrossShardStatus nextStatus,
            int sourceShard,
            int targetShard
    ) {
        return new TraceEvent(
                1,
                step,
                step,
                step,
                (int) step,
                TraceEvent.Kind.PROTOCOL_TRANSITION,
                "transferencia-prueba",
                "transferencia-prueba",
                action == ProtocolAction.CREATE_SESSION
                        ? SimulationEventType.BEGIN_TRANSFER
                        : SimulationEventType.LOCK_SOURCE,
                action == ProtocolAction.CREATE_SESSION
                        ? "EJECUTADO"
                        : "APLICADO",
                action,
                previousStatus,
                nextStatus,
                sourceShard,
                targetShard,
                "utxo-prueba",
                "recibo-prueba",
                10L,
                3,
                4,
                null,
                null,
                "Evento de prueba de identidad."
        );
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
        if (expected == null
                ? actual != null
                : !expected.equals(actual)) {
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

    private static void assertThrows(
            Class<? extends Throwable> expected,
            Runnable action,
            String message
    ) {
        try {
            action.run();
        } catch (Throwable error) {
            if (expected.isInstance(error)) {
                return;
            }
            throw new AssertionError(
                    message
                            + " Se obtuvo "
                            + error.getClass().getSimpleName()
                            + ".",
                    error
            );
        }
        throw new AssertionError(
                message + " No se produjo la excepción esperada."
        );
    }
}
