package dltlab.wallet;

import dltlab.crypto.Hashing;
import dltlab.crypto.KeyPairFactory;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/** Wallet educativa: posee llaves y puede crear transacciones firmadas. */
public class Wallet {
    private final String label;
    private final KeyPair keyPair;

    public Wallet(String label) {
        this(label, KeyPairFactory.createRsaKeyPair());
    }

    public Wallet(String label, KeyPair keyPair) {
        this.label = label;
        this.keyPair = keyPair;
    }

    public String getLabel() {
        return label;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public String shortAddress() {
        return Hashing.shortHex(Hashing.sha256(getPublicKey().getEncoded()));
    }

    public Transaction createSpend(UTXO input, Transaction.Output previousOutput, PublicKey recipient,
                                   long amount, long fee) {
        long change = previousOutput.getValue() - amount - fee;
        if (change < 0) {
            throw new IllegalArgumentException("Fondos insuficientes para crear la transaccion.");
        }

        Transaction tx = new Transaction();
        tx.addInput(input.getTxHash(), input.getOutputIndex());
        tx.addOutput(amount, recipient);
        if (change > 0) {
            tx.addOutput(change, getPublicKey());
        }
        tx.signInput(0, getPrivateKey());
        tx.finalizeTransaction();
        return tx;
    }
}
