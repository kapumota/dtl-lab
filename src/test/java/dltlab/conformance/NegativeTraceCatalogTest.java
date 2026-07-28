package dltlab.conformance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Pruebas estructurales y deterministas del corpus negativo. */
public final class NegativeTraceCatalogTest {
    private NegativeTraceCatalogTest() {
    }

    public static void main(String[] args) {
        testCatalogIsDeterministicAndComplete();
        testExpectedRejectionMatchesMutatedStep();
        testMutationSpecificEvidence();
        testReplayArtifactsReuseFormalOperators();
        System.out.println(
                "Las pruebas del corpus negativo de trazas pasaron correctamente."
        );
    }

    private static void testCatalogIsDeterministicAndComplete() {
        NegativeTraceCatalog catalog = new NegativeTraceCatalog();
        List<NegativeTraceCase> first = catalog.create(2026L);
        List<NegativeTraceCase> second = catalog.create(2026L);

        assertEquals(
                first,
                second,
                "La misma seed debe producir el mismo corpus negativo."
        );
        assertEquals(
                10,
                first.size(),
                "El corpus debe contener diez mutaciones."
        );

        Set<String> identifiers = new HashSet<>();
        Set<String> scenarios = new HashSet<>();
        for (NegativeTraceCase negativeCase : first) {
            assertTrue(
                    identifiers.add(negativeCase.mutationId()),
                    "Cada mutacion debe tener un identificador unico."
            );
            assertTrue(
                    scenarios.add(negativeCase.trace().scenarioId()),
                    "Cada traza negativa debe tener un escenario unico."
            );
            assertTrue(
                    negativeCase.trace().scenarioId().startsWith(
                            negativeCase.mutationId() + "__"
                    ),
                    "El escenario debe conservar la identidad de la mutacion."
            );
        }
    }

    private static void testExpectedRejectionMatchesMutatedStep() {
        for (NegativeTraceCase negativeCase
                : new NegativeTraceCatalog().create(77L)) {
            AbstractTraceStep rejected = negativeCase.trace()
                    .steps()
                    .get(negativeCase.expectedRejectedAbstractStep());
            assertEquals(
                    negativeCase.expectedRejectedConcreteStep(),
                    rejected.concreteStep(),
                    "El caso debe conservar el paso concreto esperado."
            );
            assertEquals(
                    negativeCase.expectedRejectedAction(),
                    rejected.action().kind(),
                    "El caso debe conservar la accion esperada."
            );
            assertEquals(
                    negativeCase.expectedTransferId(),
                    rejected.action().transferId(),
                    "El caso debe conservar la transferencia esperada."
            );
        }
    }

    private static void testMutationSpecificEvidence() {
        List<NegativeTraceCase> cases = new NegativeTraceCatalog().create(91L);

        NegativeTraceCase commitPending = find(
                cases,
                "M01_COMMIT_FROM_PENDING"
        );
        assertEquals(
                0,
                commitPending.expectedRejectedAbstractStep(),
                "El commit desde Pending debe fallar en el primer paso."
        );

        NegativeTraceCase commitAfterAbort = find(
                cases,
                "M02_COMMIT_AFTER_ABORT"
        );
        AbstractProtocolState.TransferState afterAbort = rejectedState(
                commitAfterAbort
        ).transfer(commitAfterAbort.expectedTransferId());
        assertEquals(
                AbstractProtocolState.TerminalStatus.ABORTED,
                afterAbort.terminalStatus(),
                "La mutacion debe conservar la primera decision Aborted."
        );
        assertEquals(
                AbstractProtocolState.Status.COMMITTED,
                afterAbort.status(),
                "La mutacion debe intentar cambiar el estado a Committed."
        );

        NegativeTraceCase replay = find(cases, "M03_RECEIPT_REPLAY");
        AbstractProtocolState.TransferState replayed = rejectedState(replay)
                .transfer(replay.expectedTransferId());
        assertEquals(
                2,
                replayed.receiptUseCount(),
                "El replay debe intentar un segundo consumo."
        );

        NegativeTraceCase credit = find(
                cases,
                "M04_CREDIT_WITHOUT_RECEIPT"
        );
        AbstractProtocolState.TransferState credited = rejectedState(credit)
                .transfer(credit.expectedTransferId());
        assertTrue(
                credited.destinationCredit(),
                "La mutacion debe crear credito en destino."
        );
        assertEquals(
                0,
                credited.receiptUseCount(),
                "La mutacion no debe consumir el recibo."
        );

        NegativeTraceCase timeout = find(
                cases,
                "M05_TIMEOUT_WITHOUT_RELEASE"
        );
        AbstractProtocolState.TransferState timedOut = rejectedState(timeout)
                .transfer(timeout.expectedTransferId());
        assertTrue(
                timedOut.locked(),
                "La mutacion debe conservar el origen bloqueado."
        );
        assertFalse(
                timedOut.fundsReleased(),
                "La mutacion debe omitir la liberacion de fondos."
        );

        NegativeTraceCase noQuorum = find(
                cases,
                "M06_COMMIT_WITHOUT_QUORUM"
        );
        AbstractProtocolState beforeCommit = stateBeforeRejected(noQuorum);
        assertTrue(
                beforeCommit
                        .transfer(noQuorum.expectedTransferId())
                        .votes()
                        .size() < noQuorum.trace().quorum(),
                "El commit mutado debe tener menos votos que el quorum."
        );

        NegativeTraceCase duplicateVote = find(
                cases,
                "M07_DUPLICATE_VOTE"
        );
        AbstractAction duplicateAction = rejectedStep(duplicateVote).action();
        assertTrue(
                stateBeforeRejected(duplicateVote)
                        .transfer(duplicateVote.expectedTransferId())
                        .votes()
                        .contains(duplicateAction.validatorId()),
                "El validador duplicado debe existir antes del paso mutado."
        );

        NegativeTraceCase topology = find(
                cases,
                "M09_SHARD_TOPOLOGY_CHANGE"
        );
        AbstractProtocolState.TransferState initial = topology.trace()
                .initialState()
                .transfer(topology.expectedTransferId());
        AbstractProtocolState.TransferState changed = rejectedState(topology)
                .transfer(topology.expectedTransferId());
        assertEquals(
                initial.sourceShard(),
                changed.targetShard(),
                "La mutacion debe intercambiar el shard de origen."
        );
        assertEquals(
                initial.targetShard(),
                changed.sourceShard(),
                "La mutacion debe intercambiar el shard de destino."
        );
    }

    private static void testReplayArtifactsReuseFormalOperators() {
        TlcTraceReplayGenerator generator = new TlcTraceReplayGenerator();
        for (NegativeTraceCase negativeCase
                : new NegativeTraceCatalog().create(31L)) {
            TlcReplayArtifact artifact = generator.generate(
                    negativeCase.trace()
            );
            String module = artifact.moduleText();
            assertTrue(
                    module.contains("EXTENDS CrossShardCommit"),
                    "Cada mutacion debe reutilizar la especificacion base."
            );
            assertFalse(
                    module.contains("\nNext =="),
                    "El corpus no debe redefinir Next."
            );
            assertFalse(
                    module.contains("=".repeat(8)),
                    "El modulo no debe contener separadores no permitidos."
            );
            assertFalse(
                    module.contains("\u2013") || module.contains("\u2014"),
                    "El modulo no debe contener guiones tipograficos."
            );
            assertTrue(
                    module.contains(
                            negativeCase.expectedRejectedAction().tlaName()
                    ),
                    "El replay debe contener la accion mutada esperada."
            );
        }
    }

    private static NegativeTraceCase find(
            List<NegativeTraceCase> cases,
            String mutationId
    ) {
        return cases.stream()
                .filter(value -> value.mutationId().equals(mutationId))
                .findFirst()
                .orElseThrow();
    }

    private static AbstractTraceStep rejectedStep(
            NegativeTraceCase negativeCase
    ) {
        return negativeCase.trace()
                .steps()
                .get(negativeCase.expectedRejectedAbstractStep());
    }

    private static AbstractProtocolState rejectedState(
            NegativeTraceCase negativeCase
    ) {
        return rejectedStep(negativeCase).state();
    }

    private static AbstractProtocolState stateBeforeRejected(
            NegativeTraceCase negativeCase
    ) {
        int index = negativeCase.expectedRejectedAbstractStep();
        return index == 0
                ? negativeCase.trace().initialState()
                : negativeCase.trace().steps().get(index - 1).state();
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
