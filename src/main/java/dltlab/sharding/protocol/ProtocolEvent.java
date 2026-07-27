package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardStatus;

import java.util.Objects;

/** Evento inmutable registrado durante la evolucion de una sesion cross-shard. */
public record ProtocolEvent(
        long sequence,
        long logicalTime,
        String transferId,
        ProtocolAction action,
        CrossShardStatus previousStatus,
        CrossShardStatus nextStatus,
        String reason
) {
    public ProtocolEvent {
        if (sequence < 0) {
            throw new IllegalArgumentException("La secuencia del evento no puede ser negativa.");
        }
        if (logicalTime < 0) {
            throw new IllegalArgumentException("El tiempo logico no puede ser negativo.");
        }
        transferId = requireText(transferId, "El identificador de transferencia es obligatorio.");
        action = Objects.requireNonNull(action, "La accion del protocolo es obligatoria.");
        nextStatus = Objects.requireNonNull(nextStatus, "El estado siguiente es obligatorio.");
        reason = requireText(reason, "La razon del evento es obligatoria.");
        if (action == ProtocolAction.CREATE_SESSION && previousStatus != null) {
            throw new IllegalArgumentException("La creacion de sesion no debe tener un estado anterior.");
        }
        if (action != ProtocolAction.CREATE_SESSION && previousStatus == null) {
            throw new IllegalArgumentException("Una transicion debe indicar su estado anterior.");
        }
    }

    public boolean isInitial() {
        return action == ProtocolAction.CREATE_SESSION;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
