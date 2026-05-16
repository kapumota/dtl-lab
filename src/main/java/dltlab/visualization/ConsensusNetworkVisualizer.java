package dltlab.visualization;

import dltlab.consensus.AdvancedConsensusResult;
import dltlab.consensus.NodeProfile;
import dltlab.consensus.TrustGraph;

import java.util.HashMap;
import java.util.Map;

/** Renderiza el grafo de confianza de consenso en texto y formato DOT. */
public class ConsensusNetworkVisualizer {
    public String renderAscii(AdvancedConsensusResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Red de consenso\n");
        sb.append("================\n");
        sb.append("Nodos: ").append(result.totalNodes())
                .append(" | honestos: ").append(result.honestNodes())
                .append(" | maliciosos: ").append(result.maliciousNodes()).append('\n');
        sb.append("Censores: ").append(result.censoringNodes())
                .append(" | equivocadores: ").append(result.equivocatingNodes())
                .append(" | silenciosos: ").append(result.silentNodes()).append('\n');
        sb.append("Aristas de confianza: ").append(result.trustGraph().edgeCount())
                .append(" | followees promedio: ")
                .append(String.format(java.util.Locale.ROOT, "%.2f", result.trustGraph().averageFollowees()))
                .append('\n');
        sb.append("Transaccion censurada corta: ").append(shortId(result.censoredTransactionId())).append('\n');
        sb.append("Ratio final de acuerdo honesto: ")
                .append(String.format(java.util.Locale.ROOT, "%.2f%%", result.honestAgreementRatio() * 100.0)).append('\n');
        sb.append("Exito de censura observado: ")
                .append(String.format(java.util.Locale.ROOT, "%.2f%%", result.censorshipSuccessRatio() * 100.0)).append('\n');
        sb.append('\n');

        Map<Integer, NodeProfile> profiles = new HashMap<>();
        for (NodeProfile profile : result.nodeProfiles()) profiles.put(profile.nodeId(), profile);
        TrustGraph graph = result.trustGraph();
        for (int receiver = 0; receiver < graph.size(); receiver++) {
            NodeProfile profile = profiles.get(receiver);
            sb.append("Nodo ").append(receiver)
                    .append(" [").append(profile.behavior().spanishName()).append("] sigue a ")
                    .append(graph.followeesList(receiver)).append('\n');
        }
        return sb.toString();
    }

    public String renderDot(AdvancedConsensusResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph consenso {\n");
        sb.append("  rankdir=LR;\n");
        sb.append("  label=\"Red de consenso DLT-Lab\";\n");
        for (NodeProfile profile : result.nodeProfiles()) {
            sb.append("  n").append(profile.nodeId())
                    .append(" [label=\"N").append(profile.nodeId()).append("\\n")
                    .append(profile.behavior().spanishName()).append("\"];")
                    .append('\n');
        }
        TrustGraph graph = result.trustGraph();
        for (int receiver = 0; receiver < graph.size(); receiver++) {
            for (int sender = 0; sender < graph.size(); sender++) {
                if (graph.follows(receiver, sender)) {
                    // sender -> receiver significa que receiver escucha a sender.
                    sb.append("  n").append(sender).append(" -> n").append(receiver).append(";\n");
                }
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String shortId(String id) {
        return id == null || id.length() <= 12 ? String.valueOf(id) : id.substring(0, 12);
    }
}
