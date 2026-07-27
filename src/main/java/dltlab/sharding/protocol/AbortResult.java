package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardStatus;

/** Resultado de una operacion de abort o timeout. */
public record AbortResult(
        String transferId,
        boolean success,
        CrossShardStatus status,
        String message,
        boolean fundsReleased
) implements ProtocolResult {
    public AbortResult {
        if (transferId == null || transferId.isBlank()) {
            throw new IllegalArgumentException("El identificador de transferencia es obligatorio.");
        }
        if (status == null) {
            throw new IllegalArgumentException("El estado del resultado es obligatorio.");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("El mensaje del resultado es obligatorio.");
        }
    }
}
