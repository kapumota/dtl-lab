package dltlab.consensus;

/** Describe un nodo de la red para reportes y visualizaciones. */
public record NodeProfile(int nodeId, NodeBehavior behavior) {
    public boolean isHonest() {
        return behavior == NodeBehavior.HONEST;
    }

    public boolean isMalicious() {
        return behavior != NodeBehavior.HONEST;
    }
}
