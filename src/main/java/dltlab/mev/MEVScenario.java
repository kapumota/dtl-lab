package dltlab.mev;

import dltlab.transaction.Transaction;
import dltlab.transaction.UTXOPool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Escenario reproducible para comparar orden honesto contra orden MEV. */
public class MEVScenario {
    private final String name;
    private final Map<String, Transaction> transactionsByLabel;
    private final List<String> honestOrderLabels;
    private final List<MEVOpportunity> opportunities;
    private final UTXOPool initialPool;

    public MEVScenario(String name,
                       Map<String, Transaction> transactionsByLabel,
                       List<String> honestOrderLabels,
                       List<MEVOpportunity> opportunities,
                       UTXOPool initialPool) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("El nombre del escenario es obligatorio.");
        this.name = name;
        this.transactionsByLabel = new LinkedHashMap<>(transactionsByLabel);
        this.honestOrderLabels = List.copyOf(honestOrderLabels);
        this.opportunities = List.copyOf(opportunities);
        this.initialPool = new UTXOPool(initialPool);
    }

    public String name() {
        return name;
    }

    public Map<String, Transaction> transactionsByLabel() {
        return Collections.unmodifiableMap(transactionsByLabel);
    }

    public List<String> honestOrderLabels() {
        return honestOrderLabels;
    }

    public List<MEVOpportunity> opportunities() {
        return opportunities;
    }

    public UTXOPool initialPool() {
        return new UTXOPool(initialPool);
    }
}
