package dltlab.mev;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Exporta metricas especificas de MEV con columnas comparables por escenario. */
public class MEVMetricsCsvExporter {
    public Path export(List<MEVScenarioResult> results, Path outputFile) {
        try {
            Files.createDirectories(outputFile.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("escenario,tipo,orden_honesto,orden_mev,fees_honestos,fees_mev,valor_mev_extraido,ingreso_minero_honesto,ingreso_minero_mev,diferencia_ingreso\n");
            for (MEVScenarioResult result : results) {
                sb.append(escape(result.scenarioName())).append(',')
                        .append(escape(result.primaryType().spanishName())).append(',')
                        .append(escape(result.honestOrderLabels().toString())).append(',')
                        .append(escape(result.mevOrderLabels().toString())).append(',')
                        .append(result.honestFees()).append(',')
                        .append(result.mevFees()).append(',')
                        .append(result.extractedValue()).append(',')
                        .append(result.honestMinerRevenue()).append(',')
                        .append(result.mevMinerRevenue()).append(',')
                        .append(result.revenueDelta()).append('\n');
            }
            Files.writeString(outputFile, sb.toString(), StandardCharsets.UTF_8);
            return outputFile;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron exportar las metricas MEV.", e);
        }
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
