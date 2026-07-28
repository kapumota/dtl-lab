package dltlab.conformance;

import java.util.Objects;

/** Artefacto textual reproducible que TLC puede ejecutar. */
public record TlcReplayArtifact(
        String moduleName,
        String moduleText,
        String configText,
        int stepCount
) {
    public TlcReplayArtifact {
        moduleName = requireText(
                moduleName,
                "El nombre del módulo de replay es obligatorio."
        );
        moduleText = requireText(
                moduleText,
                "El módulo TLA+ de replay es obligatorio."
        );
        configText = requireText(
                configText,
                "La configuración TLC de replay es obligatoria."
        );
        if (stepCount <= 0) {
            throw new IllegalArgumentException(
                    "El replay debe contener al menos un paso abstracto."
            );
        }
        if (!moduleText.contains("---- MODULE " + moduleName + " ----")) {
            throw new IllegalArgumentException(
                    "El texto TLA+ no declara el módulo esperado."
            );
        }
        if (!configText.contains("SPECIFICATION ReplaySpec")) {
            throw new IllegalArgumentException(
                    "La configuración debe ejecutar ReplaySpec."
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
