package dltlab.transaction;

import dltlab.crypto.ByteUtils;
import dltlab.crypto.Hashing;
import dltlab.crypto.SignatureUtils;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Transaccion estilo Bitcoin: entradas que gastan UTXOs y salidas nuevas. */
public class Transaction {
    private final List<Input> inputs = new ArrayList<>();
    private final List<Output> outputs = new ArrayList<>();
    private byte[] hash;
    private byte[] coinbaseContext;
    private boolean frozen;

    public static Transaction coinbase(long value, PublicKey recipient, byte[] context) {
        if (context == null || context.length == 0) {
            throw new IllegalArgumentException("El contexto de coinbase es obligatorio.");
        }
        Transaction tx = new Transaction();
        tx.coinbaseContext = Hashing.copy(context);
        tx.addOutput(value, recipient);
        return tx.immutableCopy();
    }

    /** Captura el contenido actual, incluso cuando cambio una firma desde un Input. */
    public final Transaction immutableCopy() {
        if (frozen) {
            return this;
        }
        Transaction copy = new Transaction();
        copy.coinbaseContext = Hashing.copy(coinbaseContext);
        for (Input input : inputs) {
            Input snapshot = new Input(input.previousTxHash, input.outputIndex);
            snapshot.setSignature(input.signature);
            snapshot.frozen = true;
            copy.inputs.add(snapshot);
        }
        for (Output output : outputs) {
            copy.addOutput(output.value, snapshotKey(output.recipient));
        }
        copy.finalizeTransaction();
        copy.frozen = true;
        return copy;
    }

    private static PublicKey snapshotKey(PublicKey key) {
        try {
            byte[] encoded = Hashing.copy(key.getEncoded());
            PublicKey copy = KeyFactory.getInstance(key.getAlgorithm())
                    .generatePublic(new X509EncodedKeySpec(encoded));
            if (!Arrays.equals(encoded, copy.getEncoded())) {
                throw new IllegalArgumentException("La llave publica debe tener una codificacion canonica.");
            }
            return copy;
        } catch (GeneralSecurityException error) {
            throw new IllegalArgumentException("La llave publica del bloque no es valida.", error);
        }
    }

    private void requireMutable() {
        if (frozen) {
            throw new IllegalStateException("La transaccion pertenece a un bloque inmutable.");
        }
    }

    public static class Input {
        private final byte[] previousTxHash;
        private final int outputIndex;
        private byte[] signature;
        private boolean frozen;

        public Input(byte[] previousTxHash, int outputIndex) {
            this.previousTxHash = Hashing.copy(previousTxHash);
            this.outputIndex = outputIndex;
        }

        public byte[] getPreviousTxHash() {
            return Hashing.copy(previousTxHash);
        }

        public int getOutputIndex() {
            return outputIndex;
        }

        public byte[] getSignature() {
            return Hashing.copy(signature);
        }

        public void setSignature(byte[] signature) {
            if (frozen) {
                throw new IllegalStateException("La entrada pertenece a un bloque inmutable.");
            }
            this.signature = Hashing.copy(signature);
        }
    }

    public static class Output {
        private final long value;
        private final PublicKey recipient;

        public Output(long value, PublicKey recipient) {
            this.value = value;
            this.recipient = recipient;
        }

        public long getValue() {
            return value;
        }

        public PublicKey getRecipient() {
            return recipient;
        }
    }

    public void addInput(byte[] previousTxHash, int outputIndex) {
        requireMutable();
        inputs.add(new Input(previousTxHash, outputIndex));
    }

    public void addOutput(long value, PublicKey recipient) {
        requireMutable();
        outputs.add(new Output(value, recipient));
    }

    public List<Input> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<Output> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public void signInput(int index, PrivateKey privateKey) {
        requireMutable();
        if (index < 0 || index >= inputs.size()) {
            throw new IllegalArgumentException("Indice de entrada invalido.");
        }
        inputs.get(index).setSignature(SignatureUtils.sign(privateKey, getRawDataToSign(index)));
    }

    public byte[] getRawDataToSign(int inputIndex) {
        return ByteUtils.write(out -> {
            out.writeInt(inputIndex);
            Input input = inputs.get(inputIndex);
            writeBytes(out, input.previousTxHash);
            out.writeInt(input.outputIndex);
            out.writeInt(outputs.size());
            for (Output output : outputs) {
                out.writeLong(output.value);
                writeBytes(out, output.recipient.getEncoded());
            }
        });
    }

    public byte[] getRawData() {
        return ByteUtils.write(out -> {
            out.writeInt(inputs.size());
            for (Input input : inputs) {
                writeBytes(out, input.previousTxHash);
                out.writeInt(input.outputIndex);
                writeBytes(out, input.signature == null ? new byte[0] : input.signature);
            }
            out.writeInt(outputs.size());
            for (Output output : outputs) {
                out.writeLong(output.value);
                writeBytes(out, output.recipient.getEncoded());
            }
            if (coinbaseContext != null) {
                out.writeUTF("dltlab:coinbase:v1");
                writeBytes(out, coinbaseContext);
            }
        });
    }

    private static void writeBytes(java.io.DataOutputStream out, byte[] data) throws java.io.IOException {
        out.writeInt(data.length);
        out.write(data);
    }

    public void finalizeTransaction() {
        requireMutable();
        this.hash = Hashing.sha256(getRawData());
    }

    public byte[] getHash() {
        return frozen ? Hashing.copy(hash) : Hashing.sha256(getRawData());
    }

    public String id() {
        return Hashing.hex(getHash());
    }

    public String shortId() {
        return Hashing.shortHex(getHash());
    }

    public boolean verifyInputSignature(int inputIndex, PublicKey publicKey) {
        return SignatureUtils.verify(publicKey, getRawDataToSign(inputIndex), inputs.get(inputIndex).getSignature());
    }

    public long outputSum() {
        long sum = 0L;
        for (Output output : outputs) {
            if (output.value < 0 || output.recipient == null || output.recipient.getEncoded() == null) {
                throw new IllegalArgumentException("La salida monetaria no es valida.");
            }
            sum = Math.addExact(sum, output.value);
        }
        return sum;
    }
}
