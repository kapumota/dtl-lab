package dltlab.sharding;

import dltlab.crypto.Hashing;
import dltlab.crypto.ByteUtils;
import dltlab.crypto.SignatureUtils;
import dltlab.transaction.UTXO;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

/** Solicitud para mover valor entre shards. */
public final class CrossShardTransfer {
    private final String id;
    private final int sourceShardId;
    private final int targetShardId;
    private final UTXO sourceUtxo;
    private final long amount;
    private final PublicKey recipient;

    private final byte[] signature;

    public CrossShardTransfer(int sourceShardId, int targetShardId, UTXO sourceUtxo, long amount, PublicKey recipient) {
        this(sourceShardId, targetShardId, sourceUtxo, amount, recipient, null);
    }

    public CrossShardTransfer(int sourceShardId, int targetShardId, UTXO sourceUtxo, long amount, PublicKey recipient, byte[] signature) {
        this.sourceShardId = sourceShardId;
        this.targetShardId = targetShardId;
        this.sourceUtxo = Objects.requireNonNull(sourceUtxo);
        this.amount = amount;
        this.recipient = recipient == null ? null : canonicalKey(recipient);
        this.signature = Hashing.copy(signature);
        this.id = "v2:" + Hashing.hex(Hashing.sha256(signingBytes()));
    }

    static PublicKey canonicalKey(PublicKey key) {
        byte[] encoded = key.getEncoded();
        if (encoded == null || encoded.length == 0) {
            throw new IllegalArgumentException("La llave publica debe tener una codificacion RSA X.509.");
        }
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (GeneralSecurityException error) {
            throw new IllegalArgumentException("La llave publica debe tener una codificacion RSA X.509.", error);
        }
    }

    public CrossShardTransfer sign(PrivateKey owner) {
        return new CrossShardTransfer(sourceShardId, targetShardId, sourceUtxo, amount,
                recipient, SignatureUtils.sign(owner, signingBytes()));
    }

    public boolean isAuthorizedBy(PublicKey owner) {
        return owner != null && recipient != null
                && SignatureUtils.verify(owner, signingBytes(), signature);
    }

    public byte[] signingBytes() {
        return ByteUtils.write(out -> {
            out.writeUTF("dltlab:cross-shard:v2");
            out.writeInt(sourceShardId);
            out.writeInt(targetShardId);
            byte[] hash = sourceUtxo.getTxHash();
            out.writeInt(hash.length);
            out.write(hash);
            out.writeInt(sourceUtxo.getOutputIndex());
            out.writeLong(amount);
            byte[] key = recipient == null ? new byte[0] : recipient.getEncoded();
            out.writeInt(key.length);
            out.write(key);
        });
    }

    public String id() { return id; }
    public int sourceShardId() { return sourceShardId; }
    public int targetShardId() { return targetShardId; }
    public UTXO sourceUtxo() { return sourceUtxo; }
    public long amount() { return amount; }
    public PublicKey recipient() { return recipient; }
}
