package dltlab.blockchain;

import dltlab.crypto.ByteUtils;
import dltlab.crypto.Hashing;
import dltlab.transaction.Transaction;

import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bloque basico: parent hash, coinbase y lista de transacciones. */
public class Block {
    public static final long COINBASE_REWARD = 25_000_000L;

    private final byte[] previousBlockHash;
    private final Transaction coinbase;
    private final List<Transaction> transactions;
    private final long timestamp;
    private final int declaredHeight;
    private final byte[] hash;

    public Block(byte[] previousBlockHash, PublicKey miner, List<Transaction> transactions, int declaredHeight) {
        this.previousBlockHash = Hashing.copy(previousBlockHash);
        this.coinbase = new Transaction();
        this.coinbase.addOutput(COINBASE_REWARD, miner);
        this.coinbase.finalizeTransaction();
        this.transactions = new ArrayList<>(transactions);
        this.timestamp = Instant.now().toEpochMilli();
        this.declaredHeight = declaredHeight;
        this.hash = calculateHash();
    }

    private byte[] calculateHash() {
        return Hashing.sha256(ByteUtils.write(out -> {
            out.writeInt(previousBlockHash == null ? 0 : previousBlockHash.length);
            if (previousBlockHash != null) out.write(previousBlockHash);
            out.writeInt(declaredHeight);
            out.writeLong(timestamp);
            out.write(coinbase.getHash());
            out.writeInt(transactions.size());
            for (Transaction tx : transactions) {
                out.write(tx.getHash());
            }
        }));
    }

    public byte[] getPreviousBlockHash() {
        return Hashing.copy(previousBlockHash);
    }

    public Transaction getCoinbase() {
        return coinbase;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
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
