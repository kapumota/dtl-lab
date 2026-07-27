package dltlab.sharding.protocol;

/** Acciones observables de la maquina de estados cross-shard. */
public enum ProtocolAction {
    CREATE_SESSION,
    LOCK_SOURCE,
    CREATE_RECEIPT,
    DELIVER_RECEIPT,
    PREPARE_DESTINATION,
    COMMIT_DESTINATION,
    ABORT_TRANSFER,
    EXPIRE_TRANSFER,
    FAIL_VALIDATION
}
