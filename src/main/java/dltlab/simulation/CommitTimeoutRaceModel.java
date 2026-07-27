package dltlab.simulation;

/** Alinea la entrega con la primera ronda posterior al timeout. */
public final class CommitTimeoutRaceModel implements NetworkFaultModel {
    @Override
    public DeliveryDecision decide(NetworkMessage message, SimulationClock clock,
                                   DeterministicRandom random) {
        int deliveryRound = message.timeoutRound();
        int delay = Math.max(0, deliveryRound - clock.now());
        return new DeliveryDecision(false, delay, 1, -100,
                "El recibo fue alineado con la carrera entre commit y timeout.");
    }
}
