package dltlab.consensus;

import dltlab.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/** Simula difusion de transacciones en una red con nodos honestos y maliciosos. */
public class ConsensusSimulator {
    public ConsensusResult run(Set<Transaction> universe, int nodeCount, double maliciousProbability,
                               double connectivityProbability, int rounds, Random random) {
        List<ConsensusNode> nodes = new ArrayList<>();
        int maliciousCount = 0;
        for (int i = 0; i < nodeCount; i++) {
            boolean malicious = random.nextDouble() < maliciousProbability;
            ConsensusNode node = malicious ? new MaliciousNode() : new HonestNode(rounds, 0.35);
            if (malicious) maliciousCount++;
            node.setNodeId(i);
            nodes.add(node);
        }

        boolean[][] follows = new boolean[nodeCount][nodeCount];
        for (int receiver = 0; receiver < nodeCount; receiver++) {
            for (int sender = 0; sender < nodeCount; sender++) {
                follows[receiver][sender] = receiver != sender && random.nextDouble() < connectivityProbability;
            }
            nodes.get(receiver).setFollowees(follows[receiver]);
        }

        List<Transaction> txs = new ArrayList<>(universe);
        for (ConsensusNode node : nodes) {
            Set<Transaction> initial = new HashSet<>();
            for (Transaction tx : txs) {
                if (random.nextDouble() < 0.45) {
                    initial.add(tx);
                }
            }
            node.setPendingTransactions(initial);
        }

        for (int round = 0; round < rounds; round++) {
            List<Set<Transaction>> proposals = new ArrayList<>();
            for (ConsensusNode node : nodes) {
                proposals.add(node.sendToFollowers());
            }

            for (int receiver = 0; receiver < nodeCount; receiver++) {
                Set<Candidate> inbox = new HashSet<>();
                for (int sender = 0; sender < nodeCount; sender++) {
                    if (follows[receiver][sender]) {
                        for (Transaction tx : proposals.get(sender)) {
                            inbox.add(new Candidate(tx, sender));
                        }
                    }
                }
                nodes.get(receiver).receiveFromFollowees(inbox);
            }
        }

        Map<String, Integer> groupSizes = new HashMap<>();
        Map<String, Integer> txCounts = new HashMap<>();
        int honestNodes = 0;
        for (ConsensusNode node : nodes) {
            if (node.isMalicious()) continue;
            honestNodes++;
            Set<Transaction> output = node.sendToFollowers();
            String key = output.stream().map(Transaction::id).sorted().collect(Collectors.joining(","));
            groupSizes.merge(key, 1, Integer::sum);
            txCounts.put(key, output.size());
        }

        String bestKey = "";
        int bestSize = 0;
        for (Map.Entry<String, Integer> entry : groupSizes.entrySet()) {
            if (entry.getValue() > bestSize) {
                bestSize = entry.getValue();
                bestKey = entry.getKey();
            }
        }
        return new ConsensusResult(nodeCount, maliciousCount, honestNodes, bestSize, txCounts.getOrDefault(bestKey, 0));
    }
}
