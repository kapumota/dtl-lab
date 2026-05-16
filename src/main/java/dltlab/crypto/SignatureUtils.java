package dltlab.crypto;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

/** Firma y verifica mensajes. En esta version se usa RSA para mantener la demo sencilla. */
public final class SignatureUtils {
    private SignatureUtils() {}

    public static byte[] sign(PrivateKey privateKey, byte[] message) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message);
            return signature.sign();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo firmar el mensaje.", e);
        }
    }

    public static boolean verify(PublicKey publicKey, byte[] message, byte[] signatureBytes) {
        if (signatureBytes == null) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(message);
            return signature.verify(signatureBytes);
        } catch (InvalidKeyException | SignatureException e) {
            return false;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo verificar la firma.", e);
        }
    }
}
