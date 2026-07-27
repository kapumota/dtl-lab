package dltlab.sharding;

/** Estado de una transferencia cross-shard dentro del protocolo atomico educativo. */
public enum CrossShardStatus {
    CREATED(false),
    SOURCE_LOCKED(false),
    RECEIPT_CREATED(false),
    RECEIPT_DELIVERED(false),
    DESTINATION_PREPARED(false),
    COMMITTED(true),
    ABORTED(true),
    TIMED_OUT(true),
    FAILED_VALIDATION(true);

    private final boolean terminal;

    CrossShardStatus(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isTerminal() {
        return terminal;
    }
}
