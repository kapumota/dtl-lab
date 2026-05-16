package dltlab.consensus;

import dltlab.transaction.Transaction;

/** Propuesta recibida desde un nodo de la red. */
public record Candidate(Transaction transaction, int senderId, int round) {
    public Candidate(Transaction transaction, int senderId) {
        this(transaction, senderId, -1);
    }
}
