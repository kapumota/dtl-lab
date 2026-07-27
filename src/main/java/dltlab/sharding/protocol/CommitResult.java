package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardStatus;

/** Resultado de un intento de commit atomico. */
public record CommitResult(
        String transferId,
        boolean success,
        CrossShardStatus status,
        String message,
        boolean rolledBack
) implements ProtocolResult {
    public CommitResult {
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
