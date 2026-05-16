package dltlab.consensus;

import dltlab.transaction.Transaction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Nodo malicioso simple: ignora mensajes y propaga solo su conjunto fijo. */
public class MaliciousNode implements ConsensusNode {
    private Set<Transaction> fixedProposal = new HashSet<>();

    @Override
    public void setNodeId(int nodeId) {
        // El nodo malicioso no necesita recordar su identificador.
    }

    @Override
    public void setFollowees(boolean[] followees) {
        // Ignora la topologia para simular comportamiento no cooperativo.
    }

    @Override
    public void setPendingTransactions(Set<Transaction> pendingTransactions) {
        this.fixedProposal = new HashSet<>(pendingTransactions);
    }

    @Override
    public Set<Transaction> sendToFollowers() {
        return Collections.unmodifiableSet(fixedProposal);
    }

    @Override
    public void receiveFromFollowees(Set<Candidate> candidates) {
        // Ignora deliberadamente las propuestas recibidas.
    }

    @Override
    public boolean isMalicious() {
        return true;
    }
}
