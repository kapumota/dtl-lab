package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardStatus;

/** Resultado general para inicio y entrega de recibos. */
public record OperationResult(
        String transferId,
        boolean success,
        CrossShardStatus status,
        String message
) implements ProtocolResult {
    public OperationResult {
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
