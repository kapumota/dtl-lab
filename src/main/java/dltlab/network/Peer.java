package dltlab.network;

/** Peer de una red P2P con marca adversarial opcional. */
public record Peer(int id, boolean adversarial) {
    public Peer {
        if (id < 0) {
            throw new IllegalArgumentException("El id del peer no puede ser negativo.");
        }
    }

    public String label() {
        return adversarial ? "peer-" + id + "-adversarial" : "peer-" + id + "-honesto";
    }
}
