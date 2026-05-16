package dltlab.consensus;

import dltlab.transaction.Transaction;

import java.util.Collections;
import java.util.Set;

/** Nodo malicioso silencioso: recibe datos pero nunca ayuda a difundirlos. */
public class SilentNode implements ConsensusNode {
    @Override
    public void setNodeId(int nodeId) {}

    @Override
    public void setFollowees(boolean[] followees) {}

    @Override
    public void setPendingTransactions(Set<Transaction> pendingTransactions) {}

    @Override
    public Set<Transaction> sendToFollowers() {
        return Collections.emptySet();
    }

    @Override
    public Set<Transaction> sendToFollower(int followerId) {
        return Collections.emptySet();
    }

    @Override
    public void receiveFromFollowees(Set<Candidate> candidates) {}

    @Override
    public boolean isMalicious() {
        return true;
    }

    @Override
    public String behaviorName() {
        return NodeBehavior.SILENT.spanishName();
    }
}
