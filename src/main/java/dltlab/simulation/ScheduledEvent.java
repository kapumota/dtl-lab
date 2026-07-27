package dltlab.simulation;

import java.util.Objects;

/** Evento programado con orden total por ronda, prioridad y secuencia. */
public record ScheduledEvent(
        long sequence,
        int round,
        int priority,
        SimulationEventType type,
        String transferId,
        String description,
        Runnable action
) {
    public ScheduledEvent {
        if (sequence < 0) {
            throw new IllegalArgumentException("La secuencia del evento no puede ser negativa.");
        }
        if (round < 0) {
            throw new IllegalArgumentException("La ronda del evento no puede ser negativa.");
        }
        type = Objects.requireNonNull(type, "El tipo de evento es obligatorio.");
        transferId = normalize(transferId, "sin-transferencia");
        description = normalize(description, "Evento de simulacion.");
        action = Objects.requireNonNull(action, "La accion del evento es obligatoria.");
    }

    private static String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
