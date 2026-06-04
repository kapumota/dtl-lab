package dltlab.network;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/** Estima latencia de propagacion sobre una topologia P2P no dirigida. */
public class MessagePropagationSimulator {
    public double estimateAverageLatency(PeerTable table, int sourcePeerId, Set<Integer> controlledPeers, long baseLatencyMs) {
        if (baseLatencyMs <= 0) {
            throw new IllegalArgumentException("La latencia base debe ser positiva.");
        }
        table.peer(sourcePeerId);
        Map<Integer, Integer> distance = shortestDistances(table, sourcePeerId);
        double total = 0.0;
        int reachable = 0;
        for (Peer peer : table.peers()) {
            Integer hops = distance.get(peer.id());
            if (hops == null || peer.id() == sourcePeerId) continue;
            double multiplier = controlledPeers.contains(peer.id()) ? 2.5 : 1.0;
            total += hops * baseLatencyMs * multiplier;
            reachable++;
        }
        return reachable == 0 ? 0.0 : total / reachable;
    }

    private Map<Integer, Integer> shortestDistances(PeerTable table, int sourcePeerId) {
        Map<Integer, Integer> distance = new HashMap<>();
        Queue<Integer> queue = new ArrayDeque<>();
        distance.put(sourcePeerId, 0);
        queue.add(sourcePeerId);
        while (!queue.isEmpty()) {
            int current = queue.remove();
            int nextDistance = distance.get(current) + 1;
            for (int neighbor : table.neighborsOf(current)) {
                if (!distance.containsKey(neighbor)) {
                    distance.put(neighbor, nextDistance);
                    queue.add(neighbor);
                }
            }
        }
        return distance;
    }
}
