package dltlab.conformance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Traza abstracta resultante de proyectar una ejecución concreta de Fase 7A. */
public record AbstractTrace(
        int schemaVersion,
        String scenarioId,
        long seed,
        int shardCount,
        int quorum,
        String simulationTraceHash,
        AbstractProtocolState initialState,
        List<AbstractTraceStep> steps
) {
    public AbstractTrace {
        if (schemaVersion != 1) {
            throw new IllegalArgumentException(
                    "La versión de la traza abstracta debe ser 1."
            );
        }
        scenarioId = requireText(
                scenarioId,
                "El escenario abstracto es obligatorio."
        );
        if (shardCount <= 0 || quorum <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de shards y el quorum deben ser positivos."
            );
        }
        simulationTraceHash = requireSha256(
                simulationTraceHash,
                "El hash de la traza concreta es inválido."
        );
        initialState = Objects.requireNonNull(
                initialState,
                "El estado abstracto inicial es obligatorio."
        );

        List<AbstractTraceStep> copiedSteps = new ArrayList<>(
                Objects.requireNonNull(
                        steps,
                        "La lista de pasos abstractos es obligatoria."
                )
        );
        if (copiedSteps.isEmpty()) {
            throw new IllegalArgumentException(
                    "La traza abstracta debe contener al menos un paso."
            );
        }

        long previousConcreteStep = -1L;
        for (int index = 0; index < copiedSteps.size(); index++) {
            AbstractTraceStep step = Objects.requireNonNull(
                    copiedSteps.get(index),
                    "La lista de pasos no admite valores nulos."
            );
            if (step.abstractStep() != index) {
                throw new IllegalArgumentException(
                        "Los pasos abstractos deben ser contiguos desde cero."
                );
            }
            if (step.concreteStep() < previousConcreteStep) {
                throw new IllegalArgumentException(
                        "La procedencia concreta no puede retroceder."
                );
            }
            previousConcreteStep = step.concreteStep();
        }
        steps = List.copyOf(copiedSteps);
    }

    public AbstractProtocolState finalState() {
        return steps.get(steps.size() - 1).state();
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
