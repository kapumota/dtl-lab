package dltlab.sharding;

import dltlab.sharding.protocol.InvalidTransitionException;
import dltlab.sharding.protocol.ProtocolAction;
import dltlab.sharding.protocol.ProtocolEvent;
import dltlab.sharding.protocol.TransitionTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Estado rastreable de una transferencia cross-shard atomica. */
public class CrossShardSession {
    private final CrossShardTransfer transfer;
    private final Receipt receipt;
    private final int startRound;
    private final int timeoutRound;
    private final int sourceApprovals;
    private final int sourceValidators;
    private final List<ProtocolEvent> events = new ArrayList<>();
    private int targetApprovals;
    private int targetValidators;
    private CrossShardStatus status;
    private String reason;
    private long lastLogicalTime;
    private long nextEventSequence;

    public CrossShardSession(CrossShardTransfer transfer, Receipt receipt, int startRound, int timeoutRound,
                             int sourceApprovals, int sourceValidators) {
        this.transfer = Objects.requireNonNull(transfer, "La transferencia es obligatoria.");
        this.receipt = Objects.requireNonNull(receipt, "El recibo es obligatorio.");
        validateRounds(startRound, timeoutRound);
        validateApprovals(sourceApprovals, sourceValidators, "origen");
        if (!transfer.id().equals(receipt.transferId())) {
            throw new IllegalArgumentException("El recibo debe pertenecer a la transferencia de la sesion.");
        }
        this.startRound = startRound;
        this.timeoutRound = timeoutRound;
        this.sourceApprovals = sourceApprovals;
        this.sourceValidators = sourceValidators;
        this.status = CrossShardStatus.CREATED;
        this.reason = "Sesion cross-shard creada.";
        this.lastLogicalTime = startRound;
        this.nextEventSequence = 1L;
        events.add(new ProtocolEvent(0L, startRound, transfer.id(), ProtocolAction.CREATE_SESSION,
                null, CrossShardStatus.CREATED, reason));
    }

    public CrossShardTransfer transfer() { return transfer; }
    public Receipt receipt() { return receipt; }
    public int startRound() { return startRound; }
    public int timeoutRound() { return timeoutRound; }
    public int sourceApprovals() { return sourceApprovals; }
    public int sourceValidators() { return sourceValidators; }
    public int targetApprovals() { return targetApprovals; }
    public int targetValidators() { return targetValidators; }
    public CrossShardStatus status() { return status; }
    public String reason() { return reason; }
    public long lastLogicalTime() { return lastLogicalTime; }
    public long stateVersion() { return events.size() - 1L; }

    public boolean isTerminal() {
        return status.isTerminal();
    }

    public boolean canTransitionTo(CrossShardStatus next) {
        return TransitionTable.canTransition(status,
                Objects.requireNonNull(next, "El estado siguiente es obligatorio."));
    }

    public void transitionTo(CrossShardStatus next, long logicalTime, ProtocolAction action,
                             String transitionReason) {
        Objects.requireNonNull(next, "El estado siguiente es obligatorio.");
        Objects.requireNonNull(action, "La accion del protocolo es obligatoria.");
        String normalizedReason = requireReason(transitionReason);
        if (logicalTime < lastLogicalTime) {
            throw new IllegalArgumentException("El tiempo logico no puede retroceder.");
        }
        if (!TransitionTable.isAllowed(status, action, next)) {
            throw new InvalidTransitionException(status, action, next);
        }
        CrossShardStatus previous = status;
        status = next;
        reason = normalizedReason;
        lastLogicalTime = logicalTime;
        events.add(new ProtocolEvent(nextEventSequence++, logicalTime, transfer.id(), action,
                previous, next, normalizedReason));
    }

    public List<ProtocolEvent> events() {
        return List.copyOf(events);
    }

    public SessionCheckpoint checkpoint() {
        return new SessionCheckpoint(transfer.id(), targetApprovals, targetValidators, status,
                reason, lastLogicalTime, nextEventSequence, events);
    }

    public void restore(SessionCheckpoint checkpoint) {
        Objects.requireNonNull(checkpoint, "El checkpoint de la sesion es obligatorio.");
        if (!transfer.id().equals(checkpoint.transferId())) {
            throw new IllegalArgumentException("El checkpoint pertenece a otra transferencia.");
        }
        targetApprovals = checkpoint.targetApprovals();
        targetValidators = checkpoint.targetValidators();
        status = checkpoint.status();
        reason = checkpoint.reason();
        lastLogicalTime = checkpoint.lastLogicalTime();
        nextEventSequence = checkpoint.nextEventSequence();
        events.clear();
        events.addAll(checkpoint.events());
    }

    /** Copia inmutable del estado mutable de una sesion para rollback. */
    public record SessionCheckpoint(
            String transferId,
            int targetApprovals,
            int targetValidators,
            CrossShardStatus status,
            String reason,
            long lastLogicalTime,
            long nextEventSequence,
            List<ProtocolEvent> events
    ) {
        public SessionCheckpoint {
            if (transferId == null || transferId.isBlank()) {
                throw new IllegalArgumentException("El identificador de transferencia es obligatorio.");
            }
            status = Objects.requireNonNull(status, "El estado del checkpoint es obligatorio.");
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("La razon del checkpoint es obligatoria.");
            }
            if (lastLogicalTime < 0 || nextEventSequence < 1) {
                throw new IllegalArgumentException("Los contadores del checkpoint son invalidos.");
            }
            events = List.copyOf(Objects.requireNonNull(events,
                    "La lista de eventos del checkpoint es obligatoria."));
        }
    }

    public void markSourceLocked(long logicalTime) {
        transitionTo(CrossShardStatus.SOURCE_LOCKED, logicalTime, ProtocolAction.LOCK_SOURCE,
                "UTXO origen bloqueado con quorum suficiente.");
    }

    public void markReceiptCreated(long logicalTime) {
        transitionTo(CrossShardStatus.RECEIPT_CREATED, logicalTime, ProtocolAction.CREATE_RECEIPT,
                "Recibo cross-shard creado para la transferencia.");
    }

    public void markReceiptDelivered(long logicalTime) {
        transitionTo(CrossShardStatus.RECEIPT_DELIVERED, logicalTime, ProtocolAction.DELIVER_RECEIPT,
                "Recibo entregado al shard destino.");
    }

    public void markDestinationPrepared(int approvals, int validators, long logicalTime) {
        validateApprovals(approvals, validators, "destino");
        transitionTo(CrossShardStatus.DESTINATION_PREPARED, logicalTime,
                ProtocolAction.PREPARE_DESTINATION,
                "Shard destino preparado para completar el commit.");
        this.targetApprovals = approvals;
        this.targetValidators = validators;
    }

    public void markCommitted(int approvals, int validators) {
        markCommitted(approvals, validators, lastLogicalTime);
    }

    public void markCommitted(int approvals, int validators, long logicalTime) {
        validateApprovals(approvals, validators, "destino");
        transitionTo(CrossShardStatus.COMMITTED, logicalTime, ProtocolAction.COMMIT_DESTINATION,
                "Commit atomico completado en shard destino.");
        this.targetApprovals = approvals;
        this.targetValidators = validators;
    }

    public void markAborted(String abortReason) {
        markAborted(abortReason, lastLogicalTime);
    }

    public void markAborted(String abortReason, long logicalTime) {
        transitionTo(CrossShardStatus.ABORTED, logicalTime, ProtocolAction.ABORT_TRANSFER,
                normalizeReason(abortReason, "Transferencia abortada manualmente."));
    }

    public void markTimedOut(String timeoutReason) {
        markTimedOut(timeoutReason, lastLogicalTime);
    }

    public void markTimedOut(String timeoutReason, long logicalTime) {
        transitionTo(CrossShardStatus.TIMED_OUT, logicalTime, ProtocolAction.EXPIRE_TRANSFER,
                normalizeReason(timeoutReason, "La transferencia vencio antes del commit."));
    }

    public void markFailedValidation(String failureReason) {
        markFailedValidation(failureReason, lastLogicalTime);
    }

    public void markFailedValidation(String failureReason, long logicalTime) {
        transitionTo(CrossShardStatus.FAILED_VALIDATION, logicalTime,
                ProtocolAction.FAIL_VALIDATION,
                normalizeReason(failureReason, "La transferencia fallo durante la validacion."));
    }

    private static void validateRounds(int startRound, int timeoutRound) {
        if (startRound < 0) {
            throw new IllegalArgumentException("La ronda inicial no puede ser negativa.");
        }
        if (timeoutRound <= startRound) {
            throw new IllegalArgumentException("La ronda de timeout debe ser posterior a la ronda inicial.");
        }
    }

    private static void validateApprovals(int approvals, int validators, String shardName) {
        if (validators <= 0) {
            throw new IllegalArgumentException("La cantidad de validadores del shard " + shardName
                    + " debe ser positiva.");
        }
        if (approvals < 0 || approvals > validators) {
            throw new IllegalArgumentException("Las aprobaciones del shard " + shardName
                    + " deben estar entre cero y la cantidad de validadores.");
        }
    }

    private static String normalizeReason(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String requireReason(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("La razon de la transicion es obligatoria.");
        }
        return value;
    }
}
