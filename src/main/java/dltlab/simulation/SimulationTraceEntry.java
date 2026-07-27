package dltlab.simulation;

import java.util.Locale;

/** Entrada inmutable de una traza de simulacion. */
public record SimulationTraceEntry(
        long sequence,
        int round,
        SimulationEventType type,
        String transferId,
        String outcome,
        String detail
) {
    public SimulationTraceEntry {
        if (sequence < 0 || round < 0) {
            throw new IllegalArgumentException("La secuencia y la ronda deben ser no negativas.");
        }
        if (type == null) {
            throw new IllegalArgumentException("El tipo de traza es obligatorio.");
        }
        transferId = normalize(transferId, "sin-transferencia");
        outcome = normalize(outcome, "sin-resultado");
        detail = normalize(detail, "Sin detalle.");
    }

    public String render() {
        return String.format(Locale.ROOT, "%05d|r=%04d|%s|%s|%s|%s",
                sequence, round, type, transferId, outcome, detail);
    }

    private static String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.replace('\n', ' ');
    }
}
