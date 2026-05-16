package dltlab.transaction;

import dltlab.crypto.Hashing;

import java.util.Arrays;

/** Referencia a una salida no gastada: hash de transaccion + indice de salida. */
public final class UTXO {
    private final byte[] txHash;
    private final int outputIndex;

    public UTXO(byte[] txHash, int outputIndex) {
        this.txHash = Hashing.copy(txHash);
        this.outputIndex = outputIndex;
    }

    public byte[] getTxHash() {
        return Hashing.copy(txHash);
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public String key() {
        return Hashing.hex(txHash) + ":" + outputIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UTXO other)) return false;
        return outputIndex == other.outputIndex && Arrays.equals(txHash, other.txHash);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(txHash) + outputIndex;
    }

    @Override
    public String toString() {
        return Hashing.shortHex(txHash) + ":" + outputIndex;
    }
}
