package dltlab.simulation;

/** Descarta todos los recibos enviados por la red simulada. */
public final class DroppedReceiptModel implements NetworkFaultModel {
    @Override
    public DeliveryDecision decide(NetworkMessage message, SimulationClock clock,
                                   DeterministicRandom random) {
        return DeliveryDecision.drop("El recibo fue descartado por el modelo de red.");
    }
}
