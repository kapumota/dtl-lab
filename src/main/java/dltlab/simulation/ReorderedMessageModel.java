package dltlab.simulation;

/** Asigna retrasos y prioridades reproducibles para alterar el orden de entrega. */
public final class ReorderedMessageModel implements NetworkFaultModel {
    private final int maximumDelay;

    public ReorderedMessageModel(int maximumDelay) {
        if (maximumDelay < 0) {
            throw new IllegalArgumentException("El retraso maximo no puede ser negativo.");
        }
        this.maximumDelay = maximumDelay;
    }

    @Override
    public DeliveryDecision decide(NetworkMessage message, SimulationClock clock,
                                   DeterministicRandom random) {
        int delay = maximumDelay == 0 ? 0 : random.nextInt(maximumDelay + 1);
        int priorityOffset = random.nextInt(9) - 4;
        return new DeliveryDecision(false, delay, 1, priorityOffset,
                "El mensaje fue reordenado de forma determinista.");
    }
}
