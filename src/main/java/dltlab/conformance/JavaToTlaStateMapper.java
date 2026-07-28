package dltlab.conformance;

import dltlab.sharding.CrossShardStatus;
import dltlab.trace.TraceEvent;
import dltlab.trace.TraceExecution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/** Construye la función de abstracción sin evaluar precondiciones de `Next`. */
public final class JavaToTlaStateMapper {
    private final JavaToTlaActionMapper actionMapper;

    public JavaToTlaStateMapper() {
        this(new JavaToTlaActionMapper());
    }

    public JavaToTlaStateMapper(JavaToTlaActionMapper actionMapper) {
        this.actionMapper = Objects.requireNonNull(
                actionMapper,
                "El mapeador de acciones es obligatorio."
        );
    }

    public AbstractTrace map(TraceExecution execution) {
        Objects.requireNonNull(
                execution,
                "La ejecución concreta es obligatoria."
        );

        Map<String, ConcreteIdentity> identities = collectIdentities(execution);
        AbstractProtocolState state = initialState(execution, identities);
        AbstractProtocolState initialState = state;
        List<AbstractTraceStep> steps = new ArrayList<>();

        for (TraceEvent event : execution.events()) {
            validateIdentity(event, identities);
            List<AbstractAction> actions = actionMapper.map(event);
            for (AbstractAction action : actions) {
                state = apply(state, action);
                steps.add(new AbstractTraceStep(
                        steps.size(),
                        event.step(),
                        action.expansionIndex(),
                        action,
                        state
                ));
            }
        }

        validateFinalStates(execution, state);
        return new AbstractTrace(
                1,
                execution.scenarioId(),
                execution.seed(),
                execution.shardCount(),
                execution.quorum(),
                execution.simulationTraceHash(),
                initialState,
                steps
        );
    }

    public AbstractProtocolState.Status mapStatus(
            CrossShardStatus status
    ) {
        Objects.requireNonNull(
                status,
                "El estado Java es obligatorio."
        );
        return switch (status) {
            case CREATED -> AbstractProtocolState.Status.PENDING;
            case SOURCE_LOCKED,
                 RECEIPT_CREATED,
                 RECEIPT_DELIVERED -> AbstractProtocolState.Status.LOCKED;
            case DESTINATION_PREPARED ->
                    AbstractProtocolState.Status.PREPARED;
            case COMMITTED -> AbstractProtocolState.Status.COMMITTED;
            case ABORTED,
                 TIMED_OUT,
                 FAILED_VALIDATION -> AbstractProtocolState.Status.ABORTED;
        };
    }

    private static Map<String, ConcreteIdentity> collectIdentities(
            TraceExecution execution
    ) {
        Map<String, ConcreteIdentity> identities = new TreeMap<>();
        for (TraceEvent event : execution.events()) {
            if (event.transferId() == null) {
                continue;
            }
            ConcreteIdentity candidate = ConcreteIdentity.from(event);
            ConcreteIdentity previous = identities.putIfAbsent(
                    event.transferId(),
                    candidate
            );
            if (previous != null && !previous.equals(candidate)) {
                throw new IllegalArgumentException(
                        "La topología o identidad concreta cambió dentro de la transferencia "
                                + event.transferId()
                                + "."
                );
            }
        }

        if (!identities.keySet().equals(execution.finalStates().keySet())) {
            throw new IllegalArgumentException(
                    "Las identidades observadas deben coincidir con las sesiones finales."
            );
        }
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(identities)
        );
    }

    private static AbstractProtocolState initialState(
            TraceExecution execution,
            Map<String, ConcreteIdentity> identities
    ) {
        Map<String, AbstractProtocolState.TransferState> transfers =
                new TreeMap<>();
        for (Map.Entry<String, ConcreteIdentity> entry
                : identities.entrySet()) {
            ConcreteIdentity identity = entry.getValue();
            transfers.put(
                    entry.getKey(),
                    new AbstractProtocolState.TransferState(
                            entry.getKey(),
                            identity.sourceShard(),
                            identity.targetShard(),
                            AbstractProtocolState.Status.PENDING,
                            AbstractProtocolState.TerminalStatus.NONE,
                            false,
                            null,
                            0,
                            false,
                            false,
                            Set.of()
                    )
            );
        }
        if (transfers.size() != execution.finalStates().size()) {
            throw new IllegalArgumentException(
                    "El estado inicial debe incluir todas las transferencias concretas."
            );
        }
        return new AbstractProtocolState(1, transfers, Set.of());
    }

    private static void validateIdentity(
            TraceEvent event,
            Map<String, ConcreteIdentity> identities
    ) {
        if (event.transferId() == null) {
            return;
        }
        ConcreteIdentity expected = identities.get(event.transferId());
        ConcreteIdentity actual = ConcreteIdentity.from(event);
        if (!actual.equals(expected)) {
            throw new IllegalArgumentException(
                    "El evento concreto altera la identidad de la transferencia "
                            + event.transferId()
                            + "."
            );
        }
    }

    private static AbstractProtocolState apply(
            AbstractProtocolState state,
            AbstractAction action
    ) {
        return switch (action.kind()) {
            case STUTTER -> state;
            case LOCK_TRANSFER -> lockTransfer(state, action.transferId());
            case RELEASE_DELAYED_RECEIPT -> releaseDelayedReceipt(
                    state,
                    action.transferId(),
                    action.receiptCopy()
            );
            case CONSUME_RECEIPT -> consumeReceipt(
                    state,
                    action.transferId(),
                    action.receiptCopy()
            );
            case CAST_VOTE -> castVote(
                    state,
                    action.transferId(),
                    action.validatorId()
            );
            case COMMIT_TRANSFER -> commitTransfer(
                    state,
                    action.transferId()
            );
            case TIMEOUT_TRANSFER -> timeoutTransfer(
                    state,
                    action.transferId()
            );
        };
    }

    private static AbstractProtocolState lockTransfer(
            AbstractProtocolState state,
            String transferId
    ) {
        AbstractProtocolState.TransferState current = state.transfer(transferId);
        AbstractProtocolState.TransferState updated = new AbstractProtocolState.TransferState(
                current.transferId(),
                current.sourceShard(),
                current.targetShard(),
                AbstractProtocolState.Status.LOCKED,
                current.terminalStatus(),
                true,
                current.receiptOwnerShard(),
                current.receiptUseCount(),
                current.destinationCredit(),
                current.fundsReleased(),
                current.votes()
        );
        Set<AbstractProtocolState.ReceiptMessage> messages =
                new LinkedHashSet<>(state.messages());
        messages.add(new AbstractProtocolState.ReceiptMessage(
                transferId,
                1,
                false
        ));
        return state.withTransfer(updated).withMessages(messages);
    }

    private static AbstractProtocolState releaseDelayedReceipt(
            AbstractProtocolState state,
            String transferId,
            int copy
    ) {
        Set<AbstractProtocolState.ReceiptMessage> messages =
                new LinkedHashSet<>();
        boolean replaced = false;
        for (AbstractProtocolState.ReceiptMessage message : state.messages()) {
            if (message.transferId().equals(transferId)
                    && message.copy() == copy
                    && message.delayed()) {
                messages.add(new AbstractProtocolState.ReceiptMessage(
                        transferId,
                        copy,
                        false
                ));
                replaced = true;
            } else {
                messages.add(message);
            }
        }
        if (!replaced) {
            messages.add(new AbstractProtocolState.ReceiptMessage(
                    transferId,
                    copy,
                    false
            ));
        }
        return state.withMessages(messages);
    }

    private static AbstractProtocolState consumeReceipt(
            AbstractProtocolState state,
            String transferId,
            int copy
    ) {
        AbstractProtocolState.TransferState current = state.transfer(transferId);
        AbstractProtocolState.TransferState updated = new AbstractProtocolState.TransferState(
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
        Set<AbstractProtocolState.ReceiptMessage> messages =
                new LinkedHashSet<>();
        for (AbstractProtocolState.ReceiptMessage message : state.messages()) {
            if (!message.transferId().equals(transferId)
                    || message.copy() != copy
                    || message.delayed()) {
                messages.add(message);
            }
        }
        return state.withTransfer(updated).withMessages(messages);
    }

    private static AbstractProtocolState castVote(
            AbstractProtocolState state,
            String transferId,
            String validatorId
    ) {
        AbstractProtocolState.TransferState updated = state
                .transfer(transferId)
                .withVote(validatorId);
        return state.withTransfer(updated);
    }

    private static AbstractProtocolState commitTransfer(
            AbstractProtocolState state,
            String transferId
    ) {
        AbstractProtocolState.TransferState current = state.transfer(transferId);
        AbstractProtocolState.TransferState updated = new AbstractProtocolState.TransferState(
                current.transferId(),
                current.sourceShard(),
                current.targetShard(),
                AbstractProtocolState.Status.COMMITTED,
                current.terminalStatus()
                        == AbstractProtocolState.TerminalStatus.NONE
                        ? AbstractProtocolState.TerminalStatus.COMMITTED
                        : current.terminalStatus(),
                current.locked(),
                current.receiptOwnerShard(),
                current.receiptUseCount(),
                current.destinationCredit(),
                current.fundsReleased(),
                current.votes()
        );
        return state.withTransfer(updated);
    }

    private static AbstractProtocolState timeoutTransfer(
            AbstractProtocolState state,
            String transferId
    ) {
        AbstractProtocolState.TransferState current = state.transfer(transferId);
        AbstractProtocolState.TransferState updated = new AbstractProtocolState.TransferState(
                current.transferId(),
                current.sourceShard(),
                current.targetShard(),
                AbstractProtocolState.Status.ABORTED,
                current.terminalStatus()
                        == AbstractProtocolState.TerminalStatus.NONE
                        ? AbstractProtocolState.TerminalStatus.ABORTED
                        : current.terminalStatus(),
                false,
                current.receiptOwnerShard(),
                current.receiptUseCount(),
                current.destinationCredit(),
                true,
                current.votes()
        );
        Set<AbstractProtocolState.ReceiptMessage> messages =
                new LinkedHashSet<>();
        for (AbstractProtocolState.ReceiptMessage message : state.messages()) {
            if (!message.transferId().equals(transferId)) {
                messages.add(message);
            }
        }
        return state.withTransfer(updated).withMessages(messages);
    }

    private void validateFinalStates(
            TraceExecution execution,
            AbstractProtocolState state
    ) {
        if (!state.transfers().keySet().equals(
                execution.finalStates().keySet()
        )) {
            throw new IllegalStateException(
                    "La abstracción final perdió identidades de transferencia."
            );
        }

        for (Map.Entry<String, CrossShardStatus> entry
                : execution.finalStates().entrySet()) {
            AbstractProtocolState.TransferState transfer = state.transfer(
                    entry.getKey()
            );
            AbstractProtocolState.Status expected = mapStatus(entry.getValue());
            if (transfer.status() != expected) {
                throw new IllegalStateException(
                        "El estado abstracto final no coincide para "
                                + entry.getKey()
                                + ". Esperado: "
                                + expected
                                + ". Actual: "
                                + transfer.status()
                                + "."
                );
            }

            boolean terminalExpected = expected
                    == AbstractProtocolState.Status.COMMITTED
                    || expected == AbstractProtocolState.Status.ABORTED;
            if (terminalExpected
                    && transfer.terminalStatus()
                    == AbstractProtocolState.TerminalStatus.NONE) {
                throw new IllegalStateException(
                        "La transferencia terminal carece de una primera decisión para "
                                + entry.getKey()
                                + "."
                );
            }
            if (!terminalExpected
                    && transfer.terminalStatus()
                    != AbstractProtocolState.TerminalStatus.NONE) {
                throw new IllegalStateException(
                        "La transferencia no terminal contiene una decisión previa para "
                                + entry.getKey()
                                + "."
                );
            }
        }
    }

    private record ConcreteIdentity(
            int sourceShard,
            int targetShard,
            String sourceUtxoKey,
            String receiptId,
            long amount
    ) {
        private ConcreteIdentity {
            if (sourceShard < 0
                    || targetShard < 0
                    || sourceShard == targetShard) {
                throw new IllegalArgumentException(
                        "La identidad concreta contiene shards inválidos."
                );
            }
            sourceUtxoKey = requireText(
                    sourceUtxoKey,
                    "La identidad concreta requiere el UTXO origen."
            );
            receiptId = requireText(
                    receiptId,
                    "La identidad concreta requiere el recibo."
            );
            if (amount <= 0) {
                throw new IllegalArgumentException(
                        "La identidad concreta requiere un monto positivo."
                );
            }
        }

        private static ConcreteIdentity from(TraceEvent event) {
            if (event.sourceShard() == null
                    || event.targetShard() == null
                    || event.amount() == null) {
                throw new IllegalArgumentException(
                        "El evento de transferencia carece de identidad concreta."
                );
            }
            return new ConcreteIdentity(
                    event.sourceShard(),
                    event.targetShard(),
                    event.sourceUtxoKey(),
                    event.receiptId(),
                    event.amount()
            );
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
