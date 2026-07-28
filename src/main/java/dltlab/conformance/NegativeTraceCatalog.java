package dltlab.conformance;

import dltlab.simulation.ScenarioCatalog;
import dltlab.simulation.SimulationRun;
import dltlab.simulation.SimulationScenario;
import dltlab.trace.TraceExecution;
import dltlab.trace.TraceRecorder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/** Construye mutaciones negativas sobre trazas abstractas ya validadas. */
public final class NegativeTraceCatalog {
    public List<NegativeTraceCase> create(long seed) {
        AbstractTrace normal = source(
                SimulationScenario.S01_NORMAL_COMMIT,
                seed
        );
        AbstractTrace timeout = source(
                SimulationScenario.S02_TIMEOUT_BEFORE_DELIVERY,
                seed
        );
        AbstractTrace concurrent = source(
                SimulationScenario.S10_MULTIPLE_CONCURRENT_SESSIONS,
                seed
        );

        return List.of(
                commitFromPending(normal),
                commitAfterAbort(timeout),
                receiptReplay(normal),
                creditWithoutReceipt(normal),
                timeoutWithoutRelease(timeout),
                commitWithoutQuorum(normal),
                duplicateVote(normal),
                transferIdentityChange(concurrent),
                shardTopologyChange(normal),
                consumeBeforeLock(normal)
        );
    }

    private static NegativeTraceCase commitFromPending(AbstractTrace source) {
        String transferId = firstTransferId(source);
        AbstractProtocolState.TransferState current = source
                .initialState()
                .transfer(transferId);
        AbstractProtocolState expectedState = source.initialState().withTransfer(
                committed(
                        current,
                        AbstractProtocolState.TerminalStatus.COMMITTED
                )
        );
        List<AbstractTraceStep> steps = new ArrayList<>();
        AbstractTraceStep rejected = appendStep(
                steps,
                0L,
                0,
                AbstractAction.Kind.COMMIT_TRANSFER,
                transferId,
                null,
                null,
                true,
                "La mutacion intenta confirmar una transferencia todavia Pending.",
                expectedState
        );
        return negativeCase(
                "M01_COMMIT_FROM_PENDING",
                source,
                "Guardia de CommitTransfer",
                "Commit directo desde Pending sin bloqueo, recibo ni votos.",
                steps,
                rejected
        );
    }

    private static NegativeTraceCase commitAfterAbort(AbstractTrace source) {
        List<AbstractTraceStep> steps = new ArrayList<>(source.steps());
        AbstractProtocolState currentState = source.finalState();
        String transferId = firstTransferId(source);
        AbstractProtocolState.TransferState current = currentState.transfer(
                transferId
        );
        AbstractProtocolState expectedState = currentState.withTransfer(
                committed(current, current.terminalStatus())
        );
        long concreteStep = lastConcreteStep(steps) + 1L;
        AbstractTraceStep rejected = appendStep(
                steps,
                concreteStep,
                0,
                AbstractAction.Kind.COMMIT_TRANSFER,
                transferId,
                null,
                null,
                true,
                "La mutacion intenta confirmar despues de una decision Aborted.",
                expectedState
        );
        return negativeCase(
                "M02_COMMIT_AFTER_ABORT",
                source,
                "TerminalStateIrreversibility",
                "Commit posterior a una primera decision terminal Aborted.",
                steps,
                rejected
        );
    }

    private static NegativeTraceCase receiptReplay(AbstractTrace source) {
        int consumeIndex = indexOf(
                source,
                step -> step.action().kind()
                        == AbstractAction.Kind.CONSUME_RECEIPT
        );
        List<AbstractTraceStep> steps = prefix(source, consumeIndex + 1);
        AbstractTraceStep consumed = source.steps().get(consumeIndex);
        String transferId = consumed.action().transferId();
        AbstractProtocolState currentState = consumed.state();
        AbstractProtocolState.TransferState current = currentState.transfer(
                transferId
        );
        AbstractProtocolState expectedState = currentState.withTransfer(
                replayedReceipt(current)
        );
        AbstractTraceStep rejected = appendStep(
                steps,
                consumed.concreteStep(),
                consumed.expansionIndex() + 100,
                AbstractAction.Kind.CONSUME_RECEIPT,
                transferId,
                consumed.action().receiptCopy(),
                null,
                true,
                "La mutacion intenta consumir por segunda vez la misma copia de recibo.",
                expectedState
        );
        return negativeCase(
                "M03_RECEIPT_REPLAY",
                source,
                "NoReceiptReplay",
                "Segundo consumo de la copia canonica del recibo.",
                steps,
                rejected
        );
    }

    private static NegativeTraceCase creditWithoutReceipt(
            AbstractTrace source
    ) {
        int lockIndex = indexOf(
                source,
                step -> step.action().kind()
                        == AbstractAction.Kind.LOCK_TRANSFER
        );
        List<AbstractTraceStep> steps = prefix(source, lockIndex + 1);
        AbstractTraceStep locked = source.steps().get(lockIndex);
        String transferId = locked.action().transferId();
        AbstractProtocolState currentState = locked.state();
        AbstractProtocolState.TransferState current = currentState.transfer(
                transferId
        );
        AbstractProtocolState expectedState = currentState.withTransfer(
                creditWithoutReceipt(current)
        );
        AbstractTraceStep rejected = appendStep(
                steps,
                locked.concreteStep(),
                locked.expansionIndex() + 100,
                AbstractAction.Kind.STUTTER,
                transferId,
                null,
                null,
                true,
                "La mutacion crea credito de destino sin consumir el recibo.",
                expectedState
        );
        return negativeCase(
                "M04_CREDIT_WITHOUT_RECEIPT",
                source,
                "DestinationCreditRequiresValidReceipt",
                "Credito de destino sin propietario ni consumo de recibo.",
                steps,
                rejected
        );
    }

    private static NegativeTraceCase timeoutWithoutRelease(
            AbstractTrace source
    ) {
        int timeoutIndex = indexOf(
                source,
                step -> step.action().kind()
                        == AbstractAction.Kind.TIMEOUT_TRANSFER
        );
        List<AbstractTraceStep> steps = prefix(source, timeoutIndex);
        AbstractTraceStep original = source.steps().get(timeoutIndex);
        String transferId = original.action().transferId();
        AbstractProtocolState previousState = stateBefore(source, timeoutIndex);
        AbstractProtocolState.TransferState previous = previousState.transfer(
                transferId
        );
        AbstractProtocolState expectedState = previousState.withTransfer(
                timeoutWithoutRelease(previous)
        );
        AbstractTraceStep rejected = appendStep(
                steps,
                original.concreteStep(),
                original.expansionIndex(),
                AbstractAction.Kind.TIMEOUT_TRANSFER,
                transferId,
                null,
                null,
                true,
                "La mutacion mantiene el origen bloqueado y omite la liberacion.",
                expectedState
        );
        return negativeCase(
                "M05_TIMEOUT_WITHOUT_RELEASE",
                source,
                "EventuallyReleasedAfterTimeout",
                "Timeout que conserva el bloqueo y no libera los fondos.",
                steps,
                rejected
        );
    }

    private static NegativeTraceCase commitWithoutQuorum(
            AbstractTrace source
    ) {
        int consumeIndex = indexOf(
                source,
                step -> step.action().kind()
                        == AbstractAction.Kind.CONSUME_RECEIPT
        );
        int commitIndex = indexOf(
                source,
                step -> step.action().kind()
                        == AbstractAction.Kind.COMMIT_TRANSFER
        );
        List<AbstractTraceStep> steps = prefix(source, consumeIndex + 1);
        AbstractProtocolState state = source.steps().get(consumeIndex).state();
        String transferId = source.steps()
                .get(consumeIndex)
                .action()
                .transferId();
        int votesBeforeCommit = Math.max(0, source.quorum() - 1);
        long voteConcreteStep = source.steps().get(consumeIndex).concreteStep();

        for (int index = 1; index <= votesBeforeCommit; index++) {
            String validatorId = "v" + index;
            AbstractProtocolState.TransferState updated = state
                    .transfer(transferId)
                    .withVote(validatorId);
            state = state.withTransfer(updated);
            appendStep(
                    steps,
                    voteConcreteStep,
                    index,
                    AbstractAction.Kind.CAST_VOTE,
                    transferId,
                    null,
                    validatorId,
                    true,
                    "La mutacion conserva un conjunto de votos inferior al quorum.",
                    state
            );
        }

        AbstractTraceStep originalCommit = source.steps().get(commitIndex);
        AbstractProtocolState expectedState = state.withTransfer(
                committed(
                        state.transfer(transferId),
                        AbstractProtocolState.TerminalStatus.COMMITTED
                )
        );
        AbstractTraceStep rejected = appendStep(
                steps,
                originalCommit.concreteStep(),
                originalCommit.expansionIndex(),
                AbstractAction.Kind.COMMIT_TRANSFER,
                transferId,
                null,
                null,
                true,
                "La mutacion intenta confirmar con menos votos que el quorum.",
                expectedState
        );
        return negativeCase(
                "M06_COMMIT_WITHOUT_QUORUM",
                source,
                "QuorumRequired",
                "Commit con una cardinalidad de votos menor que Quorum.",
                steps,
                rejected
        );
    }

    private static NegativeTraceCase duplicateVote(AbstractTrace source) {
        int voteIndex = indexOf(
                source,
                step -> step.action().kind()
                        == AbstractAction.Kind.CAST_VOTE
        );
        List<AbstractTraceStep> steps = prefix(source, voteIndex + 1);
        AbstractTraceStep firstVote = source.steps().get(voteIndex);
        AbstractTraceStep rejected = appendStep(
                steps,
                firstVote.concreteStep(),
                firstVote.expansionIndex() + 100,
                AbstractAction.Kind.CAST_VOTE,
                firstVote.action().transferId(),
                null,
                firstVote.action().validatorId(),
                true,
                "La mutacion repite un validador ya presente en votes.",
                firstVote.state()
        );
        return negativeCase(
                "M07_DUPLICATE_VOTE",
                source,
                "Guardia de CastVote",
                "Segundo voto del mismo validador canonico.",
                steps,
                rejected
        );
    }

    private static NegativeTraceCase transferIdentityChange(
            AbstractTrace source
    ) {
        int lockIndex = indexOf(
                source,
                step -> step.action().kind()
                        == AbstractAction.Kind.LOCK_TRANSFER
        );
        AbstractTraceStep original = source.steps().get(lockIndex);
        String originalTransfer = original.action().transferId();
        String changedTransfer = source.initialState()
                .transfers()
                .keySet()
                .stream()
                .filter(value -> !value.equals(originalTransfer))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "El escenario multisesion debe contener otra transferencia."
                ));
        List<AbstractTraceStep> steps = prefix(source, lockIndex);
        AbstractTraceStep rejected = appendStep(
                steps,
                original.concreteStep(),
                original.expansionIndex(),
                AbstractAction.Kind.LOCK_TRANSFER,
                changedTransfer,
                null,
                null,
                true,
                "La mutacion aplica la accion a otra transferencia del mismo escenario.",
                original.state()
        );
        return negativeCase(
                "M08_TRANSFER_ID_CHANGE",
                source,
                "Identidad de transferencia",
                "La accion identifica una sesion distinta del estado esperado.",
                steps,
                rejected
        );
    }

    private static NegativeTraceCase shardTopologyChange(
            AbstractTrace source
    ) {
        String transferId = firstTransferId(source);
        AbstractProtocolState.TransferState initial = source
                .initialState()
                .transfer(transferId);
        AbstractProtocolState expectedState = source.initialState().withTransfer(
                swapShards(initial)
        );
        List<AbstractTraceStep> steps = new ArrayList<>();
        AbstractTraceStep rejected = appendStep(
                steps,
                0L,
                0,
                AbstractAction.Kind.STUTTER,
                transferId,
                null,
                null,
                true,
                "La mutacion intercambia los shards sin una accion formal.",
                expectedState
        );
        return negativeCase(
                "M09_SHARD_TOPOLOGY_CHANGE",
                source,
                "Topologia inmutable",
                "Cambio de sourceShard y targetShard mediante Stutter.",
                steps,
                rejected
        );
    }

    private static NegativeTraceCase consumeBeforeLock(AbstractTrace source) {
        int consumeIndex = indexOf(
                source,
                step -> step.action().kind()
                        == AbstractAction.Kind.CONSUME_RECEIPT
        );
        AbstractTraceStep original = source.steps().get(consumeIndex);
        List<AbstractTraceStep> steps = new ArrayList<>();
        AbstractTraceStep rejected = appendStep(
                steps,
                original.concreteStep(),
                original.expansionIndex(),
                AbstractAction.Kind.CONSUME_RECEIPT,
                original.action().transferId(),
                original.action().receiptCopy(),
                null,
                true,
                "La mutacion consume el recibo antes de LockTransfer.",
                original.state()
        );
        return negativeCase(
                "M10_CONSUME_BEFORE_LOCK",
                source,
                "Orden de acciones",
                "ConsumeReceipt ejecutado desde el estado inicial Pending.",
                steps,
                rejected
        );
    }

    private static NegativeTraceCase negativeCase(
            String mutationId,
            AbstractTrace source,
            String expectedProperty,
            String description,
            List<AbstractTraceStep> steps,
            AbstractTraceStep rejected
    ) {
        AbstractTrace trace = new AbstractTrace(
                1,
                mutationId + "__" + source.scenarioId(),
                source.seed(),
                source.shardCount(),
                source.quorum(),
                source.simulationTraceHash(),
                source.initialState(),
                steps
        );
        return new NegativeTraceCase(
                1,
                mutationId,
                source.scenarioId(),
                expectedProperty,
                description,
                Math.toIntExact(rejected.abstractStep()),
                rejected.concreteStep(),
                rejected.action().kind(),
                rejected.action().transferId(),
                trace
        );
    }

    private static AbstractTraceStep appendStep(
            List<AbstractTraceStep> steps,
            long concreteStep,
            int expansionIndex,
            AbstractAction.Kind kind,
            String transferId,
            Integer receiptCopy,
            String validatorId,
            boolean synthetic,
            String rationale,
            AbstractProtocolState state
    ) {
        AbstractAction action = new AbstractAction(
                concreteStep,
                expansionIndex,
                kind,
                transferId,
                receiptCopy,
                validatorId,
                synthetic,
                rationale
        );
        AbstractTraceStep step = new AbstractTraceStep(
                steps.size(),
                concreteStep,
                expansionIndex,
                action,
                state
        );
        steps.add(step);
        return step;
    }

    private static List<AbstractTraceStep> prefix(
            AbstractTrace source,
            int endExclusive
    ) {
        if (endExclusive < 0 || endExclusive > source.steps().size()) {
            throw new IllegalArgumentException(
                    "El limite del prefijo es invalido."
            );
        }
        return new ArrayList<>(source.steps().subList(0, endExclusive));
    }

    private static AbstractProtocolState stateBefore(
            AbstractTrace source,
            int stepIndex
    ) {
        return stepIndex == 0
                ? source.initialState()
                : source.steps().get(stepIndex - 1).state();
    }

    private static int indexOf(
            AbstractTrace source,
            Predicate<AbstractTraceStep> predicate
    ) {
        Objects.requireNonNull(predicate, "El predicado de busqueda es obligatorio.");
        for (int index = 0; index < source.steps().size(); index++) {
            if (predicate.test(source.steps().get(index))) {
                return index;
            }
        }
        throw new IllegalStateException(
                "La traza fuente no contiene el paso requerido."
        );
    }

    private static String firstTransferId(AbstractTrace source) {
        return source.initialState()
                .transfers()
                .keySet()
                .stream()
                .findFirst()
                .orElseThrow();
    }

    private static long lastConcreteStep(List<AbstractTraceStep> steps) {
        if (steps.isEmpty()) {
            return -1L;
        }
        return steps.get(steps.size() - 1).concreteStep();
    }

    private static AbstractTrace source(
            SimulationScenario scenario,
            long seed
    ) {
        SimulationRun run = ScenarioCatalog.run(scenario, seed);
        TraceExecution concrete = new TraceRecorder().record(scenario, run);
        return new JavaToTlaStateMapper().map(concrete);
    }

    private static AbstractProtocolState.TransferState committed(
            AbstractProtocolState.TransferState current,
            AbstractProtocolState.TerminalStatus terminalStatus
    ) {
        return new AbstractProtocolState.TransferState(
                current.transferId(),
                current.sourceShard(),
                current.targetShard(),
                AbstractProtocolState.Status.COMMITTED,
                terminalStatus,
                current.locked(),
                current.receiptOwnerShard(),
                current.receiptUseCount(),
                current.destinationCredit(),
                current.fundsReleased(),
                current.votes()
        );
    }

    private static AbstractProtocolState.TransferState replayedReceipt(
            AbstractProtocolState.TransferState current
    ) {
        return new AbstractProtocolState.TransferState(
                current.transferId(),
                current.sourceShard(),
                current.targetShard(),
                AbstractProtocolState.Status.PREPARED,
                current.terminalStatus(),
                current.locked(),
                current.targetShard(),
                current.receiptUseCount() + 1,
                true,
                current.fundsReleased(),
                current.votes()
        );
    }

    private static AbstractProtocolState.TransferState creditWithoutReceipt(
            AbstractProtocolState.TransferState current
    ) {
        return new AbstractProtocolState.TransferState(
                current.transferId(),
                current.sourceShard(),
                current.targetShard(),
                AbstractProtocolState.Status.PREPARED,
                current.terminalStatus(),
                current.locked(),
                null,
                0,
                true,
                current.fundsReleased(),
                current.votes()
        );
    }

    private static AbstractProtocolState.TransferState timeoutWithoutRelease(
            AbstractProtocolState.TransferState current
    ) {
        return new AbstractProtocolState.TransferState(
                current.transferId(),
                current.sourceShard(),
                current.targetShard(),
                AbstractProtocolState.Status.ABORTED,
                AbstractProtocolState.TerminalStatus.ABORTED,
                true,
                current.receiptOwnerShard(),
                current.receiptUseCount(),
                current.destinationCredit(),
                false,
                current.votes()
        );
    }

    private static AbstractProtocolState.TransferState swapShards(
            AbstractProtocolState.TransferState current
    ) {
        return new AbstractProtocolState.TransferState(
                current.transferId(),
                current.targetShard(),
                current.sourceShard(),
                current.status(),
                current.terminalStatus(),
                current.locked(),
                current.receiptOwnerShard(),
                current.receiptUseCount(),
                current.destinationCredit(),
                current.fundsReleased(),
                new LinkedHashSet<>(current.votes())
        );
    }
}
