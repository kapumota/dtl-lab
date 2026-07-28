package dltlab.conformance;

import java.util.Objects;

/** Caso negativo tipado con el rechazo esperado por TLC. */
public record NegativeTraceCase(
        int schemaVersion,
        String mutationId,
        String sourceScenarioId,
        String expectedProperty,
        String description,
        int expectedRejectedAbstractStep,
        long expectedRejectedConcreteStep,
        AbstractAction.Kind expectedRejectedAction,
        String expectedTransferId,
        AbstractTrace trace
) {
    public NegativeTraceCase {
        if (schemaVersion != 1) {
            throw new IllegalArgumentException(
                    "La version del caso negativo debe ser 1."
            );
        }
        mutationId = requireText(
                mutationId,
                "El identificador de mutacion es obligatorio."
        );
        sourceScenarioId = requireText(
                sourceScenarioId,
                "El escenario fuente es obligatorio."
        );
        expectedProperty = requireText(
                expectedProperty,
                "La propiedad esperada es obligatoria."
        );
        description = requireText(
                description,
                "La descripcion de la mutacion es obligatoria."
        );
        if (expectedRejectedAbstractStep < 0
                || expectedRejectedConcreteStep < 0) {
            throw new IllegalArgumentException(
                    "Los pasos esperados de rechazo deben ser no negativos."
            );
        }
        expectedRejectedAction = Objects.requireNonNull(
                expectedRejectedAction,
                "La accion esperada de rechazo es obligatoria."
        );
        if (expectedTransferId != null) {
            expectedTransferId = requireText(
                    expectedTransferId,
                    "La transferencia esperada no puede estar vacia."
            );
        }
        trace = Objects.requireNonNull(
                trace,
                "La traza negativa es obligatoria."
        );
        if (expectedRejectedAbstractStep >= trace.steps().size()) {
            throw new IllegalArgumentException(
                    "El paso esperado de rechazo no existe en la traza."
            );
        }
        AbstractTraceStep expected = trace.steps().get(
                expectedRejectedAbstractStep
        );
        if (expected.concreteStep() != expectedRejectedConcreteStep
                || expected.action().kind() != expectedRejectedAction
                || !Objects.equals(
                        expected.action().transferId(),
                        expectedTransferId
                )) {
            throw new IllegalArgumentException(
                    "La procedencia esperada no coincide con el paso mutado."
            );
        }
        if (!trace.scenarioId().startsWith(mutationId + "__")) {
            throw new IllegalArgumentException(
                    "El escenario negativo debe comenzar con su identificador."
            );
        }
    }

    private static String requireText(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
