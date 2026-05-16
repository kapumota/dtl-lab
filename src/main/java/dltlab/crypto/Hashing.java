package dltlab.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/** Utilidades de hash usadas por transacciones, bloques y recibos. */
public final class Hashing {
    private Hashing() {}

    public static byte[] sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no esta disponible en esta JVM.", e);
        }
    }

    public static String hex(byte[] data) {
        if (data == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder(data.length * 2);
        for (byte b : data) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static String shortHex(byte[] data) {
        String hex = hex(data);
        return hex.length() <= 12 ? hex : hex.substring(0, 12);
    }

    public static byte[] copy(byte[] data) {
        return data == null ? null : Arrays.copyOf(data, data.length);
    }

    public static String base64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
}
