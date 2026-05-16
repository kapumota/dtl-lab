package dltlab.sharding;

/** Estado de una transferencia cross-shard dentro del protocolo atomico educativo. */
public enum CrossShardStatus {
    PENDING,
    COMMITTED,
    ABORTED,
    TIMED_OUT,
    FAILED_VALIDATION
}
