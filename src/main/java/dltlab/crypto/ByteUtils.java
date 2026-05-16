package dltlab.crypto;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Ayuda a serializar datos de forma deterministica para hash y firma. */
public final class ByteUtils {
    private ByteUtils() {}

    public static byte[] write(Writer writer) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(buffer);
            writer.write(out);
            out.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo serializar la estructura.", e);
        }
    }

    public interface Writer {
        void write(DataOutputStream out) throws IOException;
    }
}
