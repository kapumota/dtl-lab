package dltlab.consensus;

import java.util.List;
import java.util.Map;

/** Resultado de consenso ponderado por reputacion. */
public record ReputationConsensusResult(
        Map<Integer, ReputationScore> finalScores,
        List<EquivocationEvidence> evidence,
        List<SlashingEvent> slashingEvents,
        String selectedValue,
        double selectedWeight
) {
    public ReputationConsensusResult {
        finalScores = Map.copyOf(finalScores);
        evidence = List.copyOf(evidence);
        slashingEvents = List.copyOf(slashingEvents);
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("Consenso ponderado por reputacion\n");
        sb.append("---------------------------------\n");
        sb.append("Valor seleccionado: ").append(selectedValue).append('\n');
        sb.append(String.format("Peso ganador: %.4f%n", selectedWeight));
        sb.append("Evidencias de equivocacion: ").append(evidence.size()).append('\n');
        sb.append("Eventos de slashing: ").append(slashingEvents.size()).append('\n');
        for (ReputationScore score : finalScores.values()) {
            sb.append(String.format("Nodo %d score=%.2f slashing=%d%n",
                    score.nodeId(), score.score(), score.slashingEvents()));
        }
        return sb.toString();
    }
}
