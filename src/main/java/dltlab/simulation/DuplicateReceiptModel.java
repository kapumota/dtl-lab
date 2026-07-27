package dltlab.simulation;

/** Duplica cada mensaje de recibo para probar idempotencia de entrega. */
public final class DuplicateReceiptModel implements NetworkFaultModel {
    @Override
    public DeliveryDecision decide(NetworkMessage message, SimulationClock clock,
                                   DeterministicRandom random) {
        return DeliveryDecision.duplicate(2, "El recibo fue duplicado por el modelo de red.");
    }
}
