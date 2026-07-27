package dltlab.simulation;

/** Mensaje de red inmutable asociado a la entrega de un recibo cross-shard. */
public record NetworkMessage(
        String messageId,
        String transferId,
        int sourceShardId,
        int targetShardId,
        int sentRound,
        int timeoutRound,
        int copyNumber
) {
    public NetworkMessage {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("El identificador del mensaje es obligatorio.");
        }
        if (transferId == null || transferId.isBlank()) {
            throw new IllegalArgumentException("El identificador de transferencia es obligatorio.");
        }
        if (sourceShardId < 0 || targetShardId < 0 || sourceShardId == targetShardId) {
            throw new IllegalArgumentException("Los shards del mensaje son invalidos.");
        }
        if (sentRound < 0 || timeoutRound < sentRound) {
            throw new IllegalArgumentException("Las rondas del mensaje son invalidas.");
        }
        if (copyNumber < 0) {
            throw new IllegalArgumentException("El numero de copia no puede ser negativo.");
        }
    }

    public NetworkMessage copy(int number) {
        return new NetworkMessage(messageId + "-c" + number, transferId, sourceShardId,
                targetShardId, sentRound, timeoutRound, number);
    }
}
