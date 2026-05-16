package dltlab.mempool;

import dltlab.transaction.Transaction;
import dltlab.transaction.UTXOPool;

import java.util.Collection;
import java.util.List;

/** Estrategia de seleccion de transacciones para construir un bloque. */
public interface MempoolPolicy {
    String name();
    List<Transaction> select(Collection<Transaction> candidates, UTXOPool pool, int maxCount);
}
