package dltlab.simulation;

/** Retrasa todos los recibos una cantidad fija de rondas. */
public final class DelayedReceiptModel implements NetworkFaultModel {
    private final int delayRounds;

    public DelayedReceiptModel(int delayRounds) {
        if (delayRounds <= 0) {
            throw new IllegalArgumentException("El retraso debe ser positivo.");
        }
        this.delayRounds = delayRounds;
    }

    @Override
    public DeliveryDecision decide(NetworkMessage message, SimulationClock clock,
                                   DeterministicRandom random) {
        return DeliveryDecision.delay(delayRounds,
                "El recibo fue retrasado por el modelo de red.");
    }
}
