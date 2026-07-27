package dltlab.simulation;

/** Eventos observables de una ejecucion cross-shard determinista. */
public enum SimulationEventType {
    BEGIN_TRANSFER,
    LOCK_SOURCE,
    CREATE_RECEIPT,
    SEND_RECEIPT,
    DELIVER_RECEIPT,
    PREPARE_DESTINATION,
    COMMIT_DESTINATION,
    EXPIRE_TRANSFER,
    RELEASE_SOURCE,
    ABORT_TRANSFER,
    DUPLICATE_MESSAGE,
    DROP_MESSAGE,
    DELAY_MESSAGE,
    FAIL_VALIDATION,
    SHARD_OFFLINE,
    SHARD_ONLINE
}
