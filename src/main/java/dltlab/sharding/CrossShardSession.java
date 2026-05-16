package dltlab.sharding;

import java.util.Objects;

/** Estado rastreable de una transferencia cross-shard atomica. */
public class CrossShardSession {
    private final CrossShardTransfer transfer;
    private final Receipt receipt;
    private final int startRound;
    private final int timeoutRound;
    private final int sourceApprovals;
    private final int sourceValidators;
    private int targetApprovals;
    private int targetValidators;
    private CrossShardStatus status;
    private String reason;

    public CrossShardSession(CrossShardTransfer transfer, Receipt receipt, int startRound, int timeoutRound,
                             int sourceApprovals, int sourceValidators) {
        this.transfer = Objects.requireNonNull(transfer);
        this.receipt = Objects.requireNonNull(receipt);
        this.startRound = startRound;
        this.timeoutRound = timeoutRound;
        this.sourceApprovals = sourceApprovals;
        this.sourceValidators = sourceValidators;
        this.status = CrossShardStatus.PENDING;
        this.reason = "Transferencia bloqueada en shard origen; esperando commit destino.";
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

    public boolean isTerminal() {
        return status == CrossShardStatus.COMMITTED
                || status == CrossShardStatus.ABORTED
                || status == CrossShardStatus.TIMED_OUT
                || status == CrossShardStatus.FAILED_VALIDATION;
    }

    public void markCommitted(int approvals, int validators) {
        this.targetApprovals = approvals;
        this.targetValidators = validators;
        this.status = CrossShardStatus.COMMITTED;
        this.reason = "Commit atomico completado en shard destino.";
    }

    public void markAborted(String reason) {
        this.status = CrossShardStatus.ABORTED;
        this.reason = reason;
    }

    public void markTimedOut(String reason) {
        this.status = CrossShardStatus.TIMED_OUT;
        this.reason = reason;
    }

    public void markFailedValidation(String reason) {
        this.status = CrossShardStatus.FAILED_VALIDATION;
        this.reason = reason;
    }
}
