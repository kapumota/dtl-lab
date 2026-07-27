package dltlab.simulation;

/** Generador SplitMix64 con secuencia estable y controlada por una seed explicita. */
public final class DeterministicRandom {
    private static final long GOLDEN_GAMMA = 0x9E3779B97F4A7C15L;
    private final long seed;
    private long state;
    private long draws;

    public DeterministicRandom(long seed) {
        this.seed = seed;
        this.state = seed;
    }

    public long seed() {
        return seed;
    }

    public long draws() {
        return draws;
    }

    public long nextLong() {
        state += GOLDEN_GAMMA;
        draws++;
        long value = state;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("El limite aleatorio debe ser positivo.");
        }
        return (int) Long.remainderUnsigned(nextLong(), bound);
    }

    public boolean nextBoolean() {
        return (nextLong() & 1L) == 0L;
    }
}
