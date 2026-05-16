package dltlab.consensus;

import dltlab.transaction.Transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Nodo honesto: acepta transacciones vistas por varios followees y las retransmite. */
public class HonestNode implements ConsensusNode {
    private final int totalRounds;
    private final double thresholdRatio;
    private int nodeId;
    private boolean[] followees = new boolean[0];
    private final Map<String, Transaction> known = new HashMap<>();
    private final Map<String, Set<Integer>> votersByTx = new HashMap<>();
    private int currentRound = 0;
    private Set<Transaction> cachedProposal = new HashSet<>();

    public HonestNode(int totalRounds, double thresholdRatio) {
        this.totalRounds = totalRounds;
        this.thresholdRatio = thresholdRatio;
    }

    @Override
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public void setFollowees(boolean[] followees) {
        this.followees = followees.clone();
    }

    @Override
    public void setPendingTransactions(Set<Transaction> pendingTransactions) {
        for (Transaction tx : pendingTransactions) {
            known.put(tx.id(), tx);
            votersByTx.computeIfAbsent(tx.id(), ignored -> new HashSet<>()).add(nodeId);
        }
    }

    @Override
    public void beginRound(int round) {
        currentRound = round + 1;
        cachedProposal = buildProposal();
    }

    @Override
    public Set<Transaction> sendToFollowers() {
        currentRound++;
        return buildProposal();
    }

    @Override
    public Set<Transaction> sendToFollower(int followerId) {
        return new HashSet<>(cachedProposal);
    }

    @Override
    public void receiveFromFollowees(Set<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            if (candidate.senderId() >= 0 && candidate.senderId() < followees.length && followees[candidate.senderId()]) {
                Transaction tx = candidate.transaction();
                known.put(tx.id(), tx);
                votersByTx.computeIfAbsent(tx.id(), ignored -> new HashSet<>()).add(candidate.senderId());
            }
        }
    }

    @Override
    public boolean isMalicious() {
        return false;
    }

    @Override
    public String behaviorName() {
        return NodeBehavior.HONEST.spanishName();
    }

    private Set<Transaction> buildProposal() {
        if (currentRound >= totalRounds) {
            return consensusTransactions();
        }
        return new HashSet<>(known.values());
    }

    private Set<Transaction> consensusTransactions() {
        int followeeCount = 0;
        for (boolean followee : followees) {
            if (followee) followeeCount++;
        }
        int threshold = Math.max(1, (int) Math.ceil(followeeCount * thresholdRatio));
        Set<Transaction> result = new HashSet<>();
        for (Map.Entry<String, Transaction> entry : known.entrySet()) {
            if (votersByTx.getOrDefault(entry.getKey(), Set.of()).size() >= threshold) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
}
