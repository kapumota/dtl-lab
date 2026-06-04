package dltlab.consensus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consenso simple que descuenta reputacion cuando detecta equivocacion firmada. */
public class ReputationWeightedConsensus {
    private final double equivocationPenalty;

    public ReputationWeightedConsensus(double equivocationPenalty) {
        if (equivocationPenalty <= 0.0 || equivocationPenalty > 1.0) {
            throw new IllegalArgumentException("La penalidad debe estar en el rango valido.");
        }
        this.equivocationPenalty = equivocationPenalty;
    }

    public static ReputationWeightedConsensus educationalDefault() {
        return new ReputationWeightedConsensus(0.55);
    }

    public ReputationConsensusResult evaluate(List<SignedConsensusMessage> messages) {
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("La evaluacion necesita mensajes.");
        }

        Map<Integer, ReputationScore> scores = new LinkedHashMap<>();
        for (SignedConsensusMessage message : messages) {
            scores.putIfAbsent(message.nodeId(), ReputationScore.initial(message.nodeId()));
            if (!message.verifies()) {
                scores.put(message.nodeId(), scores.get(message.nodeId()).penalize(equivocationPenalty));
            }
        }

        List<EquivocationEvidence> evidence = detectEvidence(messages);
        List<SlashingEvent> slashingEvents = new ArrayList<>();
        for (EquivocationEvidence item : evidence) {
            ReputationScore current = scores.get(item.nodeId());
            scores.put(item.nodeId(), current.penalize(equivocationPenalty));
            slashingEvents.add(new SlashingEvent(item.nodeId(), item.round(), equivocationPenalty, item.render()));
        }

        Map<String, Double> weights = new LinkedHashMap<>();
        for (SignedConsensusMessage message : messages) {
            if (!message.verifies()) continue;
            double score = scores.get(message.nodeId()).score();
            weights.merge(message.value(), score, Double::sum);
        }

        Map.Entry<String, Double> winner = weights.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .orElseThrow(() -> new IllegalStateException("No hay mensajes validos para ponderar."));

        return new ReputationConsensusResult(scores, evidence, slashingEvents, winner.getKey(), winner.getValue());
    }

    private List<EquivocationEvidence> detectEvidence(List<SignedConsensusMessage> messages) {
        List<EquivocationEvidence> evidence = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            for (int j = i + 1; j < messages.size(); j++) {
                SignedConsensusMessage first = messages.get(i);
                SignedConsensusMessage second = messages.get(j);
                if (EquivocationEvidence.isEquivocation(first, second)) {
                    boolean alreadyReported = evidence.stream().anyMatch(item -> item.nodeId() == first.nodeId()
                            && item.round() == first.round()
                            && item.first().topic().equals(first.topic()));
                    if (!alreadyReported) {
                        evidence.add(new EquivocationEvidence(first, second));
                    }
                }
            }
        }
        return evidence;
    }
}
