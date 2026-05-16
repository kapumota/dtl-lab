package dltlab.metrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Exporta metricas de simulacion a un CSV pequeno y legible. */
public class CsvExporter {
    public Path export(SimulationMetrics metrics, Path outputFile) {
        try {
            Files.createDirectories(outputFile.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("metrica,valor\n");
            for (Map.Entry<String, String> entry : metrics.values().entrySet()) {
                sb.append(escape(entry.getKey())).append(',').append(escape(entry.getValue())).append('\n');
            }
            Files.writeString(outputFile, sb.toString(), StandardCharsets.UTF_8);
            return outputFile;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron exportar las metricas CSV.", e);
        }
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
