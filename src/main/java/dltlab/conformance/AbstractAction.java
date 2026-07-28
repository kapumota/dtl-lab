package dltlab.conformance;

import java.util.Objects;

/** Acción abstracta asociada con un paso concreto de la traza Java. */
public record AbstractAction(
        long concreteStep,
        int expansionIndex,
        Kind kind,
        String transferId,
        Integer receiptCopy,
        String validatorId,
        boolean synthetic,
        String rationale
) {
    /** Vocabulario de acciones usado por la especificación TLA+. */
    public enum Kind {
        LOCK_TRANSFER("LockTransfer"),
        RELEASE_DELAYED_RECEIPT("ReleaseDelayedReceipt"),
        CONSUME_RECEIPT("ConsumeReceipt"),
        CAST_VOTE("CastVote"),
        COMMIT_TRANSFER("CommitTransfer"),
        TIMEOUT_TRANSFER("TimeoutTransfer"),
        STUTTER("Stutter");

        private final String tlaName;

        Kind(String tlaName) {
            this.tlaName = tlaName;
        }

        public String tlaName() {
            return tlaName;
        }
    }

    public AbstractAction {
        if (concreteStep < 0 || expansionIndex < 0) {
            throw new IllegalArgumentException(
                    "El paso concreto y el índice de expansión deben ser no negativos."
            );
        }
        kind = Objects.requireNonNull(
                kind,
                "La acción abstracta es obligatoria."
        );
        rationale = requireText(
                rationale,
                "La justificación de la abstracción es obligatoria."
        );

        if (transferId != null) {
            transferId = requireText(
                    transferId,
                    "El identificador de transferencia no puede estar vacío."
            );
        }
        if (receiptCopy != null && receiptCopy <= 0) {
            throw new IllegalArgumentException(
                    "La copia de recibo debe ser positiva."
            );
        }
        if (validatorId != null) {
            validatorId = requireText(
                    validatorId,
                    "El identificador de validador no puede estar vacío."
            );
        }

        switch (kind) {
            case STUTTER -> requireNoParameters(receiptCopy, validatorId);
            case LOCK_TRANSFER, COMMIT_TRANSFER, TIMEOUT_TRANSFER -> {
                requireTransfer(transferId);
                requireNoParameters(receiptCopy, validatorId);
            }
            case RELEASE_DELAYED_RECEIPT, CONSUME_RECEIPT -> {
                requireTransfer(transferId);
                if (receiptCopy == null) {
                    throw new IllegalArgumentException(
                            "La acción sobre un recibo debe indicar su copia."
                    );
                }
                if (validatorId != null) {
                    throw new IllegalArgumentException(
                            "La acción sobre un recibo no debe indicar un validador."
                    );
                }
            }
            case CAST_VOTE -> {
                requireTransfer(transferId);
                if (validatorId == null) {
                    throw new IllegalArgumentException(
                            "La acción de voto debe indicar un validador."
                    );
                }
                if (receiptCopy != null) {
                    throw new IllegalArgumentException(
                            "La acción de voto no debe indicar una copia de recibo."
                    );
                }
            }
        }
    }

    public String tlaName() {
        return kind.tlaName();
    }

    public boolean isStutter() {
        return kind == Kind.STUTTER;
    }

    private static void requireTransfer(String transferId) {
        if (transferId == null) {
            throw new IllegalArgumentException(
                    "La acción abstracta debe identificar su transferencia."
            );
        }
    }

    private static void requireNoParameters(
            Integer receiptCopy,
            String validatorId
    ) {
        if (receiptCopy != null || validatorId != null) {
            throw new IllegalArgumentException(
                    "La acción abstracta no admite parámetros adicionales."
            );
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
