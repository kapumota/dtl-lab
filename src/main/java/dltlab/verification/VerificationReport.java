package dltlab.verification;

import java.util.List;

/** Reporte agregado de verificacion. */
public record VerificationReport(List<VerificationResult> results) {
    public boolean allPassed() {
        return results.stream().allMatch(VerificationResult::passed);
    }

    public String render() {
        StringBuilder builder = new StringBuilder();
        builder.append("Reporte de verificacion\n");
        for (VerificationResult result : results) {
            builder.append(result.passed() ? "[PASS] " : "[FAIL] ");
            builder.append(result.name()).append(" - ").append(result.detail()).append('\n');
        }
        return builder.toString();
    }
}
