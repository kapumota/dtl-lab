package dltlab.security;

/** Resultado agregado de una propiedad de seguridad evaluada muchas veces. */
public record SecurityPropertyResult(String propertyName, int iterations, int passed, int failed, String detail) {
    public boolean success() {
        return failed == 0;
    }

    public double passRatio() {
        if (iterations == 0) return 0.0;
        return (double) passed / (double) iterations;
    }
}
