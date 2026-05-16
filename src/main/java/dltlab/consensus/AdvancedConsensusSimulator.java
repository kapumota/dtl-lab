package dltlab.consensus;

import dltlab.transaction.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/** Simulador avanzado: trust graph, censura, equivocacion y metricas por ronda. */
public class AdvancedConsensusSimulator {
    public AdvancedConsensusResult run(Set<Transaction> universe, ConsensusConfig config, Random random) {
        if (universe.isEmpty()) {
            throw new IllegalArgumentException("La simulacion necesita al menos una transaccion candidata.");
        }

        List<Transaction> sortedUniverse = universe.stream()
                .sorted((a, b) -> a.id().compareTo(b.id()))
                .toList();
        String censoredTxId = sortedUniverse.get(0).id();
        Set<String> censored = Set.of(censoredTxId);

        TrustGraph trustGraph = TrustGraph.random(config.nodeCount(), config.connectivityProbability(), random);
        List<NodeProfile> profiles = buildProfiles(config, random);
        List<ConsensusNode> nodes = buildNodes(profiles, config, censored);

        for (int receiver = 0; receiver < nodes.size(); receiver++) {
            nodes.get(receiver).setNodeId(receiver);
            nodes.get(receiver).setFollowees(trustGraph.followeesOf(receiver));
        }

        for (int nodeId = 0; nodeId < nodes.size(); nodeId++) {
            Set<Transaction> initial = new HashSet<>();
            for (Transaction tx : sortedUniverse) {
                if (random.nextDouble() < config.initialTransactionProbability()) {
                    initial.add(tx);
                }
            }
            if (profiles.get(nodeId).isHonest() && initial.isEmpty()) {
                initial.add(sortedUniverse.get(nodeId % sortedUniverse.size()));
            }
            nodes.get(nodeId).setPendingTransactions(initial);
        }

        List<ConsensusRoundMetric> roundMetrics = new ArrayList<>();
        for (int round = 0; round < config.rounds(); round++) {
            for (ConsensusNode node : nodes) {
                node.beginRound(round);
            }

            List<Set<Transaction>> proposalsForMetrics = new ArrayList<>();
            for (int sender = 0; sender < nodes.size(); sender++) {
                proposalsForMetrics.add(nodes.get(sender).sendToFollowers());
            }

            int totalMessages = 0;
            Set<String> propagated = new HashSet<>();
            for (int receiver = 0; receiver < nodes.size(); receiver++) {
                Set<Candidate> inbox = new HashSet<>();
                for (int sender = 0; sender < nodes.size(); sender++) {
                    if (!trustGraph.follows(receiver, sender)) continue;
                    Set<Transaction> proposal = nodes.get(sender).sendToFollower(receiver);
                    totalMessages += proposal.size();
                    for (Transaction tx : proposal) {
                        propagated.add(tx.id());
                        inbox.add(new Candidate(tx, sender, round));
                    }
                }
                nodes.get(receiver).receiveFromFollowees(inbox);
            }

            roundMetrics.add(buildRoundMetric(round, profiles, proposalsForMetrics, totalMessages, propagated, censoredTxId));
        }

        Map<String, Integer> finalGroupSizes = new LinkedHashMap<>();
        Map<String, Integer> finalGroupTxCounts = new HashMap<>();
        int honestNodes = 0;
        int honestOutputsContainingCensored = 0;
        for (int nodeId = 0; nodeId < nodes.size(); nodeId++) {
            if (!profiles.get(nodeId).isHonest()) continue;
            honestNodes++;
            Set<Transaction> output = nodes.get(nodeId).sendToFollowers();
            if (containsTx(output, censoredTxId)) honestOutputsContainingCensored++;
            String key = consensusKey(output);
            finalGroupSizes.merge(key, 1, Integer::sum);
            finalGroupTxCounts.put(key, output.size());
        }

        String bestKey = "";
        int largestGroup = 0;
        for (Map.Entry<String, Integer> entry : finalGroupSizes.entrySet()) {
            if (entry.getValue() > largestGroup) {
                largestGroup = entry.getValue();
                bestKey = entry.getKey();
            }
        }

        int censoringNodes = count(profiles, NodeBehavior.CENSORING);
        int equivocatingNodes = count(profiles, NodeBehavior.EQUIVOCATING);
        int silentNodes = count(profiles, NodeBehavior.SILENT);
        int maliciousNodes = censoringNodes + equivocatingNodes + silentNodes;

        return new AdvancedConsensusResult(
                config.nodeCount(),
                honestNodes,
                maliciousNodes,
                censoringNodes,
                equivocatingNodes,
                silentNodes,
                largestGroup,
                finalGroupTxCounts.getOrDefault(bestKey, 0),
                censoredTxId,
                honestOutputsContainingCensored,
                trustGraph,
                profiles,
                roundMetrics,
                finalGroupSizes
        );
    }

    private List<NodeProfile> buildProfiles(ConsensusConfig config, Random random) {
        int malicious = Math.max(1, (int) Math.round(config.nodeCount() * config.maliciousProbability()));
        malicious = Math.min(config.nodeCount() - 1, malicious);
        int censoring = Math.max(config.censoringShareAmongMalicious() > 0.0 ? 1 : 0,
                (int) Math.round(malicious * config.censoringShareAmongMalicious()));
        int equivocating = Math.max(config.equivocatingShareAmongMalicious() > 0.0 ? 1 : 0,
                (int) Math.round(malicious * config.equivocatingShareAmongMalicious()));
        if (censoring + equivocating > malicious) {
            equivocating = Math.max(0, malicious - censoring);
        }
        int silent = malicious - censoring - equivocating;

        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < config.nodeCount(); i++) ids.add(i);
        Collections.shuffle(ids, random);

        Map<Integer, NodeBehavior> behaviors = new HashMap<>();
        int cursor = 0;
        for (int i = 0; i < censoring; i++) behaviors.put(ids.get(cursor++), NodeBehavior.CENSORING);
        for (int i = 0; i < equivocating; i++) behaviors.put(ids.get(cursor++), NodeBehavior.EQUIVOCATING);
        for (int i = 0; i < silent; i++) behaviors.put(ids.get(cursor++), NodeBehavior.SILENT);

        List<NodeProfile> profiles = new ArrayList<>();
        for (int i = 0; i < config.nodeCount(); i++) {
            profiles.add(new NodeProfile(i, behaviors.getOrDefault(i, NodeBehavior.HONEST)));
        }
        return profiles;
    }

    private List<ConsensusNode> buildNodes(List<NodeProfile> profiles, ConsensusConfig config, Set<String> censored) {
        List<ConsensusNode> nodes = new ArrayList<>();
        for (NodeProfile profile : profiles) {
            ConsensusNode node = switch (profile.behavior()) {
                case HONEST -> new HonestNode(config.rounds(), config.honestThresholdRatio());
                case CENSORING -> new CensoringNode(censored);
                case EQUIVOCATING -> new EquivocatingNode();
                case SILENT -> new SilentNode();
            };
            nodes.add(node);
        }
        return nodes;
    }

    private ConsensusRoundMetric buildRoundMetric(int round, List<NodeProfile> profiles, List<Set<Transaction>> proposals,
                                                  int totalMessages, Set<String> propagated, String censoredTxId) {
        Map<String, Integer> groupSizes = new HashMap<>();
        int honestNodes = 0;
        int honestContainingCensored = 0;
        for (int nodeId = 0; nodeId < profiles.size(); nodeId++) {
            if (!profiles.get(nodeId).isHonest()) continue;
            honestNodes++;
            Set<Transaction> proposal = proposals.get(nodeId);
            if (containsTx(proposal, censoredTxId)) honestContainingCensored++;
            groupSizes.merge(consensusKey(proposal), 1, Integer::sum);
        }
        int largest = groupSizes.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        double ratio = honestNodes == 0 ? 0.0 : largest / (double) honestNodes;
        return new ConsensusRoundMetric(round + 1, totalMessages, propagated.size(), largest, honestNodes, ratio,
                honestContainingCensored, groupSizes.size());
    }

    private int count(List<NodeProfile> profiles, NodeBehavior behavior) {
        int count = 0;
        for (NodeProfile profile : profiles) {
            if (profile.behavior() == behavior) count++;
        }
        return count;
    }

    private boolean containsTx(Set<Transaction> transactions, String txId) {
        for (Transaction tx : transactions) {
            if (tx.id().equals(txId)) return true;
        }
        return false;
    }

    private String consensusKey(Set<Transaction> transactions) {
        return transactions.stream().map(Transaction::id).sorted().collect(Collectors.joining(","));
    }
}
