package dltlab.mempool;

/** Configuracion economica de la mempool realista. */
public record MempoolConfig(long maxMempoolVBytes,
                            long minRelayFeeRateSatsPerVByte,
                            boolean rbfEnabled,
                            boolean evictionEnabled) {
    public MempoolConfig {
        if (maxMempoolVBytes <= 0) {
            throw new IllegalArgumentException("La capacidad de mempool debe ser positiva.");
        }
        if (minRelayFeeRateSatsPerVByte < 0) {
            throw new IllegalArgumentException("El fee rate minimo no puede ser negativo.");
        }
    }

    public static MempoolConfig unbounded() {
        return new MempoolConfig(Long.MAX_VALUE, 0L, false, false);
    }

    public static MempoolConfig economicDefault(long maxMempoolVBytes) {
        return new MempoolConfig(maxMempoolVBytes, 1L, true, true);
    }
}
