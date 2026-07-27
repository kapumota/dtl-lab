package dltlab.sharding.protocol;

/** Error durante una operacion atomica del protocolo. */
public class ProtocolException extends RuntimeException {
    private final String transferId;
    private final boolean rolledBack;

    public ProtocolException(String transferId, String message) {
        this(transferId, message, false, null);
    }

    public ProtocolException(String transferId, String message, boolean rolledBack, Throwable cause) {
        super(message, cause);
        this.transferId = transferId;
        this.rolledBack = rolledBack;
    }

    public String transferId() {
        return transferId;
    }

    public boolean rolledBack() {
        return rolledBack;
    }
}
