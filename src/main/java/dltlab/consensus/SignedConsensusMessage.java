package dltlab.consensus;

import dltlab.crypto.Hashing;

import java.nio.charset.StandardCharsets;

/** Mensaje firmado de consenso usado para detectar equivocacion. */
public record SignedConsensusMessage(
        int nodeId,
        int round,
        String topic,
        String value,
        String signature
) {
    public SignedConsensusMessage {
        if (nodeId < 0) throw new IllegalArgumentException("El nodo no puede ser negativo.");
        if (round < 0) throw new IllegalArgumentException("La ronda no puede ser negativa.");
        if (topic == null || topic.isBlank()) throw new IllegalArgumentException("El topico no puede estar vacio.");
        if (value == null || value.isBlank()) throw new IllegalArgumentException("El valor no puede estar vacio.");
        if (signature == null || signature.isBlank()) throw new IllegalArgumentException("La firma no puede estar vacia.");
    }

    public static SignedConsensusMessage sign(int nodeId, int round, String topic, String value) {
        String payload = nodeId + "|" + round + "|" + topic + "|" + value;
        String signature = Hashing.hex(Hashing.sha256(payload.getBytes(StandardCharsets.UTF_8)));
        return new SignedConsensusMessage(nodeId, round, topic, value, signature);
    }

    public boolean verifies() {
        return signature.equals(sign(nodeId, round, topic, value).signature());
    }
}
