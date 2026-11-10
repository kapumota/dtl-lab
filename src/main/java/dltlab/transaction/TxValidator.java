package dltlab.transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Valida transacciones UTXO y actualiza el ledger local. */
public class TxValidator {
    private final UTXOPool utxoPool;

    public TxValidator(UTXOPool utxoPool) {
        // Copia defensiva para evitar que el llamador modifique el estado interno.
        this.utxoPool = new UTXOPool(utxoPool);
    }

    public UTXOPool getUtxoPool() {
        return new UTXOPool(utxoPool);
    }

    public boolean isValidTx(Transaction tx) {
        if (tx == null || tx.getInputs().isEmpty() || tx.getOutputs().isEmpty()) {
            return false;
        }
        Set<UTXO> claimed = new HashSet<>();
        long inputSum = 0L;
        try {
            long outputSum = tx.outputSum();
            for (int i = 0; i < tx.getInputs().size(); i++) {
                Transaction.Input input = tx.getInputs().get(i);
                UTXO utxo = new UTXO(input.getPreviousTxHash(), input.getOutputIndex());
                Transaction.Output previous = utxoPool.getOutput(utxo);
                if (previous == null || previous.getValue() < 0 || previous.getRecipient() == null
                        || !claimed.add(utxo) || !tx.verifyInputSignature(i, previous.getRecipient())) {
                    return false;
                }
                inputSum = Math.addExact(inputSum, previous.getValue());
            }
            return inputSum >= outputSum;
        } catch (ArithmeticException | IllegalArgumentException error) {
            return false;
        }
    }

    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        List<Transaction> remaining = new ArrayList<>(List.of(possibleTxs));
        List<Transaction> accepted = new ArrayList<>();
        boolean progress;

        // Se repite para permitir transacciones que dependen de otras aceptadas en el mismo lote.
        do {
            progress = false;
            List<Transaction> nextRound = new ArrayList<>();
            for (Transaction tx : remaining) {
                if (isValidTx(tx)) {
                    applyValidatedTransaction(tx);
                    accepted.add(tx);
                    progress = true;
                } else {
                    nextRound.add(tx);
                }
            }
            remaining = nextRound;
        } while (progress);

        return accepted.toArray(new Transaction[0]);
    }

    public void applyTransaction(Transaction tx) {
        if (!isValidTx(tx)) {
            throw new IllegalArgumentException("La transaccion ordinaria no es valida.");
        }
        applyValidatedTransaction(tx);
    }

    private void applyValidatedTransaction(Transaction tx) {
        byte[] hash = tx.getHash();
        for (Transaction.Input input : tx.getInputs()) {
            utxoPool.removeUTXO(new UTXO(input.getPreviousTxHash(), input.getOutputIndex()));
        }
        for (int i = 0; i < tx.getOutputs().size(); i++) {
            utxoPool.addUTXO(new UTXO(hash, i), tx.getOutputs().get(i));
        }
    }
}
