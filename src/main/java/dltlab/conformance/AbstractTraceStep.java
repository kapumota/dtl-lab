package dltlab.conformance;

import java.util.Objects;

/** Paso abstracto producido desde uno de los eventos concretos de Fase 7A. */
public record AbstractTraceStep(
        long abstractStep,
        long concreteStep,
        int expansionIndex,
        AbstractAction action,
        AbstractProtocolState state
) {
    public AbstractTraceStep {
        if (abstractStep < 0 || concreteStep < 0 || expansionIndex < 0) {
            throw new IllegalArgumentException(
                    "Los índices del paso abstracto deben ser no negativos."
            );
        }
        action = Objects.requireNonNull(
                action,
                "La acción del paso abstracto es obligatoria."
        );
        state = Objects.requireNonNull(
                state,
                "El estado posterior del paso abstracto es obligatorio."
        );
        if (action.concreteStep() != concreteStep
                || action.expansionIndex() != expansionIndex) {
            throw new IllegalArgumentException(
                    "La acción y el paso abstracto deben compartir su procedencia."
            );
        }
    }
}
