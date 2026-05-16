package dltlab.crypto;

import java.util.Arrays;

/** Wrapper inmutable para usar bytes como llave en mapas. */
public final class ByteArrayKey {
    private final byte[] data;

    public ByteArrayKey(byte[] data) {
        this.data = Hashing.copy(data);
    }

    public byte[] bytes() {
        return Hashing.copy(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ByteArrayKey other)) return false;
        return Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
