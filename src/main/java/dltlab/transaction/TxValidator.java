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
        Set<UTXO> claimed = new HashSet<>();
        long inputSum = 0L;
        long outputSum = 0L;

        for (int i = 0; i < tx.getInputs().size(); i++) {
            Transaction.Input input = tx.getInputs().get(i);
            UTXO utxo = new UTXO(input.getPreviousTxHash(), input.getOutputIndex());

            if (!utxoPool.contains(utxo)) {
                return false;
            }
            if (!claimed.add(utxo)) {
                return false;
            }

            Transaction.Output previousOutput = utxoPool.getOutput(utxo);
            if (!tx.verifyInputSignature(i, previousOutput.getRecipient())) {
                return false;
            }
            inputSum += previousOutput.getValue();
        }

        for (Transaction.Output output : tx.getOutputs()) {
            if (output.getValue() < 0) {
                return false;
            }
            outputSum += output.getValue();
        }

        return inputSum >= outputSum;
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
                    applyTransaction(tx);
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
        for (Transaction.Input input : tx.getInputs()) {
            utxoPool.removeUTXO(new UTXO(input.getPreviousTxHash(), input.getOutputIndex()));
        }
        for (int i = 0; i < tx.getOutputs().size(); i++) {
            utxoPool.addUTXO(new UTXO(tx.getHash(), i), tx.getOutputs().get(i));
        }
    }
}
