package dltlab.simulation;

/** Entrega todos los mensajes sin retraso ni duplicacion. */
public final class NoFaultModel implements NetworkFaultModel {
    @Override
    public DeliveryDecision decide(NetworkMessage message, SimulationClock clock,
                                   DeterministicRandom random) {
        return DeliveryDecision.deliver();
    }
}
