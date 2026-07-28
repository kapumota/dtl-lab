package dltlab.trace;

import dltlab.sharding.CrossShardStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Ejecucion concreta que agrupa configuracion, eventos y estados finales. */
public record TraceExecution(
        int schemaVersion,
        String scenarioId,
        long seed,
        int shardCount,
        int quorum,
        String simulationTraceHash,
        List<TraceEvent> events,
        Map<String, CrossShardStatus> finalStates
) {
    public TraceExecution {
        if (schemaVersion != 1) {
            throw new IllegalArgumentException(
                    "La version del esquema de ejecucion debe ser 1.");
        }
        scenarioId = requireText(
                scenarioId,
                "El identificador de escenario es obligatorio."
        );
        if (shardCount <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de shards debe ser positiva.");
        }
        if (quorum <= 0) {
            throw new IllegalArgumentException(
                    "El quorum debe ser positivo.");
        }
        simulationTraceHash = requireSha256(
                simulationTraceHash,
                "El hash de la traza de simulacion es invalido."
        );

        List<TraceEvent> copiedEvents = new ArrayList<>(
                Objects.requireNonNull(
                        events,
                        "La lista de eventos es obligatoria."
                )
        );
        if (copiedEvents.isEmpty()) {
            throw new IllegalArgumentException(
                    "La ejecucion debe contener al menos un evento.");
        }
        for (int index = 0; index < copiedEvents.size(); index++) {
            TraceEvent event = Objects.requireNonNull(
                    copiedEvents.get(index),
                    "La lista de eventos no admite valores nulos."
            );
            if (event.schemaVersion() != schemaVersion) {
                throw new IllegalArgumentException(
                        "Todos los eventos deben usar la version del esquema de la ejecucion.");
            }
            if (event.step() != index) {
                throw new IllegalArgumentException(
                        "Los pasos de la traza deben ser contiguos desde cero.");
            }
        }
        events = List.copyOf(copiedEvents);

        TreeMap<String, CrossShardStatus> sortedStates = new TreeMap<>();
        for (Map.Entry<String, CrossShardStatus> entry
                : Objects.requireNonNull(
                        finalStates,
                        "Los estados finales son obligatorios."
                ).entrySet()) {
            String transferId = requireText(
                    entry.getKey(),
                    "El identificador final de transferencia es obligatorio."
            );
            CrossShardStatus status = Objects.requireNonNull(
                    entry.getValue(),
                    "El estado final de transferencia es obligatorio."
            );
            sortedStates.put(transferId, status);
        }
        if (sortedStates.isEmpty()) {
            throw new IllegalArgumentException(
                    "La ejecucion debe contener al menos un estado final.");
        }

        for (TraceEvent event : events) {
            String transferId = event.transferId();
            if (transferId != null
                    && !sortedStates.containsKey(transferId)) {
                throw new IllegalArgumentException(
                        "Cada evento de transferencia debe pertenecer "
                                + "a una sesion con estado final.");
            }
        }

        finalStates = Collections.unmodifiableMap(
                new LinkedHashMap<>(sortedStates)
        );
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static String requireSha256(String value, String message) {
        String normalized = requireText(value, message);
        if (!normalized.matches("sha256:[0-9a-f]{64}")) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }
}
