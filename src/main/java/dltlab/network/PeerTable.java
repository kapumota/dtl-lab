package dltlab.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Tabla P2P no dirigida para modelar vecinos conocidos por cada nodo. */
public class PeerTable {
    private final Map<Integer, Peer> peers = new LinkedHashMap<>();
    private final Map<Integer, Set<Integer>> connections = new LinkedHashMap<>();

    public void addPeer(Peer peer) {
        peers.put(peer.id(), peer);
        connections.putIfAbsent(peer.id(), new LinkedHashSet<>());
    }

    public void connect(int left, int right) {
        ensurePeer(left);
        ensurePeer(right);
        if (left == right) {
            throw new IllegalArgumentException("Un peer no puede conectarse consigo mismo.");
        }
        connections.get(left).add(right);
        connections.get(right).add(left);
    }

    public boolean contains(int peerId) {
        return peers.containsKey(peerId);
    }

    public Peer peer(int peerId) {
        ensurePeer(peerId);
        return peers.get(peerId);
    }

    public Set<Integer> neighborsOf(int peerId) {
        ensurePeer(peerId);
        return Collections.unmodifiableSet(connections.get(peerId));
    }

    public List<Peer> peers() {
        return Collections.unmodifiableList(new ArrayList<>(peers.values()));
    }

    public int peerCount() {
        return peers.size();
    }

    public int connectionCount() {
        int total = 0;
        for (Set<Integer> edges : connections.values()) {
            total += edges.size();
        }
        return total / 2;
    }

    public int adversarialPeerCount() {
        int count = 0;
        for (Peer peer : peers.values()) {
            if (peer.adversarial()) count++;
        }
        return count;
    }

    public static PeerTable educationalTopology() {
        PeerTable table = new PeerTable();
        for (int id = 0; id < 10; id++) {
            table.addPeer(new Peer(id, id >= 6));
        }
        table.connect(0, 6);
        table.connect(0, 7);
        table.connect(0, 8);
        table.connect(1, 2);
        table.connect(1, 3);
        table.connect(2, 3);
        table.connect(2, 4);
        table.connect(3, 5);
        table.connect(4, 5);
        table.connect(4, 6);
        table.connect(5, 7);
        table.connect(6, 8);
        table.connect(7, 9);
        table.connect(8, 9);
        return table;
    }

    private void ensurePeer(int peerId) {
        if (!peers.containsKey(peerId)) {
            throw new IllegalArgumentException("Peer inexistente: " + peerId);
        }
    }
}
