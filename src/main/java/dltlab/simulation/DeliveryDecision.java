package dltlab.simulation;

/** Decision reproducible sobre entrega, perdida, retraso o duplicacion. */
public record DeliveryDecision(
        boolean dropped,
        int delayRounds,
        int copies,
        int priorityOffset,
        String reason
) {
    public DeliveryDecision {
        if (delayRounds < 0) {
            throw new IllegalArgumentException("El retraso no puede ser negativo.");
        }
        if (copies <= 0) {
            throw new IllegalArgumentException("La cantidad de copias debe ser positiva.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("La razon de entrega es obligatoria.");
        }
    }

    public static DeliveryDecision deliver() {
        return new DeliveryDecision(false, 0, 1, 0, "Entrega normal del mensaje.");
    }

    public static DeliveryDecision drop(String reason) {
        return new DeliveryDecision(true, 0, 1, 0, reason);
    }

    public static DeliveryDecision delay(int rounds, String reason) {
        return new DeliveryDecision(false, rounds, 1, 0, reason);
    }

    public static DeliveryDecision duplicate(int copies, String reason) {
        return new DeliveryDecision(false, 0, copies, 0, reason);
    }
}
