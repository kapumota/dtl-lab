package dltlab.simulation;

/** Reloj logico monotono usado por la simulacion determinista. */
public final class SimulationClock {
    private int currentRound;

    public SimulationClock() {
        this(0);
    }

    public SimulationClock(int initialRound) {
        if (initialRound < 0) {
            throw new IllegalArgumentException("La ronda inicial no puede ser negativa.");
        }
        this.currentRound = initialRound;
    }

    public int now() {
        return currentRound;
    }

    public void advanceTo(int round) {
        if (round < currentRound) {
            throw new IllegalArgumentException("El reloj de simulacion no puede retroceder.");
        }
        currentRound = round;
    }

    public void advanceBy(int rounds) {
        if (rounds < 0) {
            throw new IllegalArgumentException("El avance del reloj no puede ser negativo.");
        }
        advanceTo(Math.addExact(currentRound, rounds));
    }
}
