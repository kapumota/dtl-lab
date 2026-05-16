package dltlab.security;

import java.util.Collections;
import java.util.List;

/** Reporte de seguridad con score educativo de 0 a 100. */
public class SecurityScoreReport {
    private final long seed;
    private final List<SecurityPropertyResult> results;

    public SecurityScoreReport(long seed, List<SecurityPropertyResult> results) {
        this.seed = seed;
        this.results = List.copyOf(results);
    }

    public long seed() {
        return seed;
    }

    public List<SecurityPropertyResult> results() {
        return Collections.unmodifiableList(results);
    }

    public int totalIterations() {
        int total = 0;
        for (SecurityPropertyResult result : results) {
            total += result.iterations();
        }
        return total;
    }

    public int totalPassed() {
        int total = 0;
        for (SecurityPropertyResult result : results) {
            total += result.passed();
        }
        return total;
    }

    public int totalFailed() {
        int total = 0;
        for (SecurityPropertyResult result : results) {
            total += result.failed();
        }
        return total;
    }

    public boolean allPassed() {
        return totalFailed() == 0;
    }

    public double score() {
        int total = totalIterations();
        if (total == 0) return 0.0;
        return 100.0 * totalPassed() / total;
    }

    public String render() {
        StringBuilder builder = new StringBuilder();
        builder.append("Reporte de seguridad y verificacion\n");
        builder.append("Seed: ").append(seed).append('\n');
        builder.append(String.format(java.util.Locale.ROOT, "Security score: %.2f/100\n", score()));
        for (SecurityPropertyResult result : results) {
            builder.append(result.success() ? "[PASS] " : "[FAIL] ");
            builder.append(result.propertyName())
                    .append(" | iteraciones=").append(result.iterations())
                    .append(" | pasadas=").append(result.passed())
                    .append(" | fallidas=").append(result.failed())
                    .append(" | ").append(result.detail())
                    .append('\n');
        }
        return builder.toString();
    }
}
