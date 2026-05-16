package dltlab.consensus;

import dltlab.transaction.Transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Nodo malicioso censor: difunde transacciones, excepto una lista censurada. */
public class CensoringNode implements ConsensusNode {
    private final Set<String> censoredTxIds;
    private final Map<String, Transaction> known = new HashMap<>();

    public CensoringNode(Set<String> censoredTxIds) {
        this.censoredTxIds = new HashSet<>(censoredTxIds);
    }

    @Override
    public void setNodeId(int nodeId) {}

    @Override
    public void setFollowees(boolean[] followees) {}

    @Override
    public void setPendingTransactions(Set<Transaction> pendingTransactions) {
        for (Transaction tx : pendingTransactions) {
            if (!censoredTxIds.contains(tx.id())) known.put(tx.id(), tx);
        }
    }

    @Override
    public Set<Transaction> sendToFollowers() {
        return new HashSet<>(known.values());
    }

    @Override
    public void receiveFromFollowees(Set<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            Transaction tx = candidate.transaction();
            if (!censoredTxIds.contains(tx.id())) known.put(tx.id(), tx);
        }
    }

    @Override
    public boolean isMalicious() {
        return true;
    }

    @Override
    public String behaviorName() {
        return NodeBehavior.CENSORING.spanishName();
    }
}
