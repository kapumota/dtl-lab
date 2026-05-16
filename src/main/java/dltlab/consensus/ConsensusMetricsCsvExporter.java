package dltlab.consensus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Exporta metricas por ronda de consenso a CSV. */
public class ConsensusMetricsCsvExporter {
    public Path export(AdvancedConsensusResult result, Path outputFile) {
        try {
            Files.createDirectories(outputFile.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("ronda,mensajes,tx_unicas,grupo_honesto_mayor,nodos_honestos,ratio_acuerdo_honesto,outputs_honestos_con_tx_censurada,grupos_consenso\n");
            for (ConsensusRoundMetric metric : result.roundMetrics()) {
                sb.append(metric.round()).append(',')
                        .append(metric.totalMessages()).append(',')
                        .append(metric.uniqueTransactionsPropagated()).append(',')
                        .append(metric.largestHonestGroup()).append(',')
                        .append(metric.honestNodes()).append(',')
                        .append(String.format(java.util.Locale.ROOT, "%.4f", metric.honestAgreementRatio())).append(',')
                        .append(metric.honestOutputsContainingCensoredTx()).append(',')
                        .append(metric.consensusGroups()).append('\n');
            }
            Files.writeString(outputFile, sb.toString(), StandardCharsets.UTF_8);
            return outputFile;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo exportar el CSV de consenso.", e);
        }
    }
}
