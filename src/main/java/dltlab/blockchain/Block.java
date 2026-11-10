package dltlab.blockchain;

import dltlab.crypto.ByteUtils;
import dltlab.crypto.Hashing;
import dltlab.transaction.Transaction;

import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/** Bloque basico: parent hash, coinbase y lista de transacciones. */
public final class Block {
    public static final long COINBASE_REWARD = 25_000_000L;

    private final byte[] previousBlockHash;
    private final Transaction coinbase;
    private final List<Transaction> transactions;
    private final long timestamp;
    private final int declaredHeight;
    private final byte[] hash;

    public Block(byte[] previousBlockHash, PublicKey miner, List<Transaction> transactions, int declaredHeight) {
        this.previousBlockHash = Hashing.copy(previousBlockHash);
        this.transactions = transactions.stream().map(Transaction::immutableCopy).toList();
        this.timestamp = Instant.now().toEpochMilli();
        this.declaredHeight = declaredHeight;
        this.coinbase = Transaction.coinbase(COINBASE_REWARD, miner, coinbaseContext());
        this.hash = calculateHash();
    }

    private byte[] coinbaseContext() {
        return ByteUtils.write(out -> {
            out.writeBoolean(previousBlockHash != null);
            if (previousBlockHash != null) {
                out.writeInt(previousBlockHash.length);
                out.write(previousBlockHash);
            }
            out.writeInt(declaredHeight);
            out.writeLong(timestamp);
            out.writeInt(transactions.size());
            for (Transaction tx : transactions) {
                out.write(tx.getHash());
            }
        });
    }

    private byte[] calculateHash() {
        return Hashing.sha256(ByteUtils.write(out -> {
            out.writeUTF("dltlab:block:v1");
            out.write(coinbaseContext());
            out.write(coinbase.getHash());
        }));
    }

    boolean hasValidHash() {
        return Arrays.equals(hash, calculateHash());
    }

    public byte[] getPreviousBlockHash() {
        return Hashing.copy(previousBlockHash);
    }

    public Transaction getCoinbase() {
        return coinbase;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getDeclaredHeight() {
        return declaredHeight;
    }

    public byte[] getHash() {
        return Hashing.copy(hash);
    }

    public String shortId() {
        return Hashing.shortHex(hash);
    }
}
