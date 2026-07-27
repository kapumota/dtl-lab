package dltlab.simulation;

/** Modelo de fallos determinista aplicado a cada mensaje de red. */
@FunctionalInterface
public interface NetworkFaultModel {
    DeliveryDecision decide(
            NetworkMessage message,
            SimulationClock clock,
            DeterministicRandom random
    );
}
