package dltlab.transaction;

import dltlab.crypto.ByteUtils;
import dltlab.crypto.Hashing;
import dltlab.crypto.SignatureUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Transaccion estilo Bitcoin: entradas que gastan UTXOs y salidas nuevas. */
public class Transaction {
    private final List<Input> inputs = new ArrayList<>();
    private final List<Output> outputs = new ArrayList<>();
    private byte[] hash;

    public static class Input {
        private final byte[] previousTxHash;
        private final int outputIndex;
        private byte[] signature;

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
        inputs.add(new Input(previousTxHash, outputIndex));
        hash = null;
    }

    public void addOutput(long value, PublicKey recipient) {
        outputs.add(new Output(value, recipient));
        hash = null;
    }

    public List<Input> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<Output> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public void signInput(int index, PrivateKey privateKey) {
        if (index < 0 || index >= inputs.size()) {
            throw new IllegalArgumentException("Indice de entrada invalido.");
        }
        inputs.get(index).setSignature(SignatureUtils.sign(privateKey, getRawDataToSign(index)));
        hash = null;
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
        });
    }

    private static void writeBytes(java.io.DataOutputStream out, byte[] data) throws java.io.IOException {
        out.writeInt(data.length);
        out.write(data);
    }

    public void finalizeTransaction() {
        this.hash = Hashing.sha256(getRawData());
    }

    public byte[] getHash() {
        if (hash == null) {
            finalizeTransaction();
        }
        return Hashing.copy(hash);
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
            sum += output.value;
        }
        return sum;
    }
}
