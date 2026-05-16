package dltlab.consensus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Grafo dirigido: receiver sigue a sender si follows[receiver][sender] es true. */
public class TrustGraph {
    private final boolean[][] follows;

    public TrustGraph(boolean[][] follows) {
        this.follows = new boolean[follows.length][follows.length];
        for (int i = 0; i < follows.length; i++) {
            if (follows[i].length != follows.length) {
                throw new IllegalArgumentException("El grafo de confianza debe ser cuadrado.");
            }
            this.follows[i] = follows[i].clone();
        }
    }

    public static TrustGraph random(int nodeCount, double connectivityProbability, Random random) {
        boolean[][] follows = new boolean[nodeCount][nodeCount];
        for (int receiver = 0; receiver < nodeCount; receiver++) {
            int followeeCount = 0;
            for (int sender = 0; sender < nodeCount; sender++) {
                if (receiver == sender) continue;
                if (random.nextDouble() < connectivityProbability) {
                    follows[receiver][sender] = true;
                    followeeCount++;
                }
            }
            // Evita nodos completamente aislados en la demo educativa.
            if (followeeCount == 0) {
                int sender = random.nextInt(nodeCount - 1);
                if (sender >= receiver) sender++;
                follows[receiver][sender] = true;
            }
        }
        return new TrustGraph(follows);
    }

    public int size() {
        return follows.length;
    }

    public boolean follows(int receiver, int sender) {
        return follows[receiver][sender];
    }

    public boolean[] followeesOf(int receiver) {
        return follows[receiver].clone();
    }

    public List<Integer> followeesList(int receiver) {
        List<Integer> result = new ArrayList<>();
        for (int sender = 0; sender < follows.length; sender++) {
            if (follows[receiver][sender]) result.add(sender);
        }
        return Collections.unmodifiableList(result);
    }

    public int edgeCount() {
        int count = 0;
        for (int receiver = 0; receiver < follows.length; receiver++) {
            for (int sender = 0; sender < follows.length; sender++) {
                if (follows[receiver][sender]) count++;
            }
        }
        return count;
    }

    public double averageFollowees() {
        return edgeCount() / (double) follows.length;
    }
}
