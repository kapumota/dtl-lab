package dltlab.consensus;

import dltlab.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Nodo malicioso equivocado: envia vistas diferentes a distintos seguidores. */
public class EquivocatingNode implements ConsensusNode {
    private final List<Transaction> known = new ArrayList<>();
    private int currentRound = 0;

    @Override
    public void setNodeId(int nodeId) {}

    @Override
    public void setFollowees(boolean[] followees) {}

    @Override
    public void setPendingTransactions(Set<Transaction> pendingTransactions) {
        known.clear();
        known.addAll(pendingTransactions);
    }

    @Override
    public void beginRound(int round) {
        this.currentRound = round;
    }

    @Override
    public Set<Transaction> sendToFollowers() {
        return new HashSet<>(known);
    }

    @Override
    public Set<Transaction> sendToFollower(int followerId) {
        Set<Transaction> result = new HashSet<>();
        for (int i = 0; i < known.size(); i++) {
            // Cambia la vista por seguidor y por ronda para dificultar la convergencia.
            if (((i + followerId + currentRound) & 1) == 0) {
                result.add(known.get(i));
            }
        }
        return result;
    }

    @Override
    public void receiveFromFollowees(Set<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            if (known.stream().noneMatch(tx -> tx.id().equals(candidate.transaction().id()))) {
                known.add(candidate.transaction());
            }
        }
    }

    @Override
    public boolean isMalicious() {
        return true;
    }

    @Override
    public String behaviorName() {
        return NodeBehavior.EQUIVOCATING.spanishName();
    }
}
