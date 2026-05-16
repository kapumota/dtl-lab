package dltlab.consensus;

import dltlab.transaction.Transaction;

import java.util.Set;

/** Interfaz comun para nodos honestos y maliciosos. */
public interface ConsensusNode {
    void setNodeId(int nodeId);
    void setFollowees(boolean[] followees);
    void setPendingTransactions(Set<Transaction> pendingTransactions);

    /** Permite preparar estado por ronda sin depender de detalles del simulador. */
    default void beginRound(int round) {}

    Set<Transaction> sendToFollowers();

    /** En consenso avanzado algunos nodos maliciosos pueden enviar vistas distintas por receptor. */
    default Set<Transaction> sendToFollower(int followerId) {
        return sendToFollowers();
    }

    void receiveFromFollowees(Set<Candidate> candidates);
    boolean isMalicious();

    default String behaviorName() {
        return isMalicious() ? "malicioso" : "honesto";
    }
}
