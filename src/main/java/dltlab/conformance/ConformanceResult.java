package dltlab.conformance;

import java.nio.file.Path;
import java.util.Objects;

/** Resultado estructurado de ejecutar una traza abstracta con TLC. */
public record ConformanceResult(
        String scenarioId,
        boolean accepted,
        int exitCode,
        int checkedAbstractSteps,
        Integer rejectedAbstractStep,
        Long rejectedConcreteStep,
        String rejectedAction,
        String transferId,
        String message,
        Path modulePath,
        Path configPath,
        Path stdoutPath,
        Path stderrPath
) {
    public ConformanceResult {
        scenarioId = requireText(
                scenarioId,
                "El escenario del resultado es obligatorio."
        );
        message = requireText(
                message,
                "El mensaje del resultado es obligatorio."
        );
        modulePath = Objects.requireNonNull(
                modulePath,
                "La ruta del módulo de replay es obligatoria."
        );
        configPath = Objects.requireNonNull(
                configPath,
                "La ruta de configuración es obligatoria."
        );
        stdoutPath = Objects.requireNonNull(
                stdoutPath,
                "La ruta de salida estándar es obligatoria."
        );
        stderrPath = Objects.requireNonNull(
                stderrPath,
                "La ruta de error estándar es obligatoria."
        );
        if (checkedAbstractSteps < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de pasos revisados no puede ser negativa."
            );
        }
        if (accepted) {
            if (exitCode != 0
                    || rejectedAbstractStep != null
                    || rejectedConcreteStep != null
                    || rejectedAction != null
                    || transferId != null) {
                throw new IllegalArgumentException(
                        "Un resultado aceptado no debe declarar un paso rechazado."
                );
            }
        } else if (rejectedAbstractStep != null
                && rejectedAbstractStep < 0) {
            throw new IllegalArgumentException(
                    "El paso rechazado no puede ser negativo."
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
