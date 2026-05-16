package dltlab.sharding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Exporta metricas especificas de sharding a CSV. */
public class ShardMetricsCsvExporter {
    public Path export(List<ShardRoundMetric> metrics, Path outputFile) {
        try {
            Files.createDirectories(outputFile.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("ronda,pendientes,confirmadas,abortadas,timeout,fallo_validacion,utxos_bloqueados,valor_movido,validadores,validadores_online\n");
            for (ShardRoundMetric metric : metrics) {
                sb.append(metric.round()).append(',')
                        .append(metric.pendingTransfers()).append(',')
                        .append(metric.committedTransfers()).append(',')
                        .append(metric.abortedTransfers()).append(',')
                        .append(metric.timedOutTransfers()).append(',')
                        .append(metric.failedValidationTransfers()).append(',')
                        .append(metric.lockedUtxos()).append(',')
                        .append(metric.totalValueMoved()).append(',')
                        .append(metric.totalValidators()).append(',')
                        .append(metric.onlineValidators()).append('\n');
            }
            Files.writeString(outputFile, sb.toString(), StandardCharsets.UTF_8);
            return outputFile;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron exportar las metricas CSV de sharding.", e);
        }
    }
}
