package dltlab.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/** Crea pares de llaves para wallets de la simulacion. */
public final class KeyPairFactory {
    private KeyPairFactory() {}

    public static KeyPair createRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA no esta disponible en esta JVM.", e);
        }
    }
}
