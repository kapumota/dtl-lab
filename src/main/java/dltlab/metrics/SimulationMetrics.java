package dltlab.metrics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Guarda metricas simples de una ejecucion para exportarlas a CSV. */
public class SimulationMetrics {
    private final Map<String, String> values = new LinkedHashMap<>();

    public void put(String key, long value) {
        values.put(key, Long.toString(value));
    }

    public void put(String key, double value) {
        values.put(key, String.format(java.util.Locale.ROOT, "%.4f", value));
    }

    public void put(String key, String value) {
        values.put(key, value);
    }

    public Map<String, String> values() {
        return Collections.unmodifiableMap(values);
    }
}
