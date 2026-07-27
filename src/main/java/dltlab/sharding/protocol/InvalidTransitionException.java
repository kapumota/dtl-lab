package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardStatus;

/** Indica que una accion intenta ejecutar una transicion no permitida. */
public class InvalidTransitionException extends IllegalStateException {
    private final CrossShardStatus currentStatus;
    private final ProtocolAction action;
    private final CrossShardStatus nextStatus;

    public InvalidTransitionException(CrossShardStatus currentStatus, ProtocolAction action,
                                      CrossShardStatus nextStatus) {
        super("La transicion " + currentStatus + " -> " + nextStatus
                + " con la accion " + action + " no esta permitida.");
        this.currentStatus = currentStatus;
        this.action = action;
        this.nextStatus = nextStatus;
    }

    public CrossShardStatus currentStatus() {
        return currentStatus;
    }

    public ProtocolAction action() {
        return action;
    }

    public CrossShardStatus nextStatus() {
        return nextStatus;
    }
}
