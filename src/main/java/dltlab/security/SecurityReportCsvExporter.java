package dltlab.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Exporta el reporte de seguridad a CSV para CI y analisis posterior. */
public class SecurityReportCsvExporter {
    public Path export(SecurityScoreReport report, Path outputFile) {
        try {
            Files.createDirectories(outputFile.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("propiedad,iteraciones,pasadas,fallidas,ratio_pasadas,estado,detalle\n");
            for (SecurityPropertyResult result : report.results()) {
                sb.append(escape(result.propertyName())).append(',')
                        .append(result.iterations()).append(',')
                        .append(result.passed()).append(',')
                        .append(result.failed()).append(',')
                        .append(String.format(java.util.Locale.ROOT, "%.4f", result.passRatio())).append(',')
                        .append(result.success() ? "PASS" : "FAIL").append(',')
                        .append(escape(result.detail())).append('\n');
            }
            sb.append("TOTAL,").append(report.totalIterations()).append(',')
                    .append(report.totalPassed()).append(',')
                    .append(report.totalFailed()).append(',')
                    .append(String.format(java.util.Locale.ROOT, "%.4f", report.score() / 100.0)).append(',')
                    .append(report.allPassed() ? "PASS" : "FAIL").append(',')
                    .append(escape("Security score " + String.format(java.util.Locale.ROOT, "%.2f", report.score()))).append('\n');
            Files.writeString(outputFile, sb.toString(), StandardCharsets.UTF_8);
            return outputFile;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo exportar el reporte de seguridad.", e);
        }
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
