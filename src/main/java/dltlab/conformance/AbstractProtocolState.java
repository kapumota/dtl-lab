package dltlab.conformance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Estado abstracto tipado que proyecta las variables del modelo TLA+. */
public record AbstractProtocolState(
        int schemaVersion,
        Map<String, TransferState> transfers,
        Set<ReceiptMessage> messages
) {
    /** Estados de transferencia definidos por `StatusValues` en TLA+. */
    public enum Status {
        PENDING("Pending"),
        LOCKED("Locked"),
        PREPARED("Prepared"),
        COMMITTED("Committed"),
        ABORTED("Aborted");

        private final String tlaLiteral;

        Status(String tlaLiteral) {
            this.tlaLiteral = tlaLiteral;
        }

        public String tlaLiteral() {
            return tlaLiteral;
        }
    }

    /** Primera decisión terminal conservada por `terminalStatus`. */
    public enum TerminalStatus {
        NONE("None"),
        COMMITTED("Committed"),
        ABORTED("Aborted");

        private final String tlaLiteral;

        TerminalStatus(String tlaLiteral) {
            this.tlaLiteral = tlaLiteral;
        }

        public String tlaLiteral() {
            return tlaLiteral;
        }
    }

    /** Proyección de las variables indexadas por una transferencia. */
    public record TransferState(
            String transferId,
            int sourceShard,
            int targetShard,
            Status status,
            TerminalStatus terminalStatus,
            boolean locked,
            Integer receiptOwnerShard,
            int receiptUseCount,
            boolean destinationCredit,
            boolean fundsReleased,
            Set<String> votes
    ) {
        public TransferState {
            transferId = requireText(
                    transferId,
                    "El identificador abstracto de transferencia es obligatorio."
            );
            if (sourceShard < 0
                    || targetShard < 0
                    || sourceShard == targetShard) {
                throw new IllegalArgumentException(
                        "Los shards abstractos deben ser distintos y no negativos."
                );
            }
            status = Objects.requireNonNull(
                    status,
                    "El estado abstracto es obligatorio."
            );
            terminalStatus = Objects.requireNonNull(
                    terminalStatus,
                    "El estado terminal abstracto es obligatorio."
            );
            if (receiptOwnerShard != null && receiptOwnerShard < 0) {
                throw new IllegalArgumentException(
                        "El shard propietario del recibo no puede ser negativo."
                );
            }
            if (receiptUseCount < 0) {
                throw new IllegalArgumentException(
                        "El contador de uso del recibo no puede ser negativo."
                );
            }

            TreeSet<String> sortedVotes = new TreeSet<>();
            for (String vote : Objects.requireNonNull(
                    votes,
                    "El conjunto de votos es obligatorio."
            )) {
                sortedVotes.add(requireText(
                        vote,
                        "El identificador de voto no puede estar vacío."
                ));
            }
            votes = Collections.unmodifiableSet(
                    new LinkedHashSet<>(sortedVotes)
            );
        }

        public TransferState withVote(String validatorId) {
            TreeSet<String> updatedVotes = new TreeSet<>(votes);
            updatedVotes.add(requireText(
                    validatorId,
                    "El identificador de validador es obligatorio."
            ));
            return new TransferState(
                    transferId,
                    sourceShard,
                    targetShard,
                    status,
                    terminalStatus,
                    locked,
                    receiptOwnerShard,
                    receiptUseCount,
                    destinationCredit,
                    fundsReleased,
                    updatedVotes
            );
        }
    }

    /** Mensaje abstracto de recibo definido por `ReceiptMessage`. */
    public record ReceiptMessage(
            String transferId,
            int copy,
            boolean delayed
    ) {
        public ReceiptMessage {
            transferId = requireText(
                    transferId,
                    "El mensaje debe identificar su transferencia."
            );
            if (copy <= 0) {
                throw new IllegalArgumentException(
                        "La copia de mensaje debe ser positiva."
                );
            }
        }
    }

    public AbstractProtocolState {
        if (schemaVersion != 1) {
            throw new IllegalArgumentException(
                    "La versión del estado abstracto debe ser 1."
            );
        }

        TreeMap<String, TransferState> sortedTransfers = new TreeMap<>();
        for (Map.Entry<String, TransferState> entry
                : Objects.requireNonNull(
                        transfers,
                        "Las transferencias abstractas son obligatorias."
                ).entrySet()) {
            String transferId = requireText(
                    entry.getKey(),
                    "La clave de transferencia abstracta es obligatoria."
            );
            TransferState transfer = Objects.requireNonNull(
                    entry.getValue(),
                    "El estado de transferencia no puede ser nulo."
            );
            if (!transferId.equals(transfer.transferId())) {
                throw new IllegalArgumentException(
                        "La clave de transferencia debe coincidir con su estado."
                );
            }
            sortedTransfers.put(transferId, transfer);
        }
        if (sortedTransfers.isEmpty()) {
            throw new IllegalArgumentException(
                    "El estado abstracto debe contener al menos una transferencia."
            );
        }
        transfers = Collections.unmodifiableMap(
                new LinkedHashMap<>(sortedTransfers)
        );

        List<ReceiptMessage> sortedMessages = new ArrayList<>(
                Objects.requireNonNull(
                        messages,
                        "El conjunto de mensajes abstractos es obligatorio."
                )
        );
        sortedMessages.sort(
                Comparator.comparing(ReceiptMessage::transferId)
                        .thenComparingInt(ReceiptMessage::copy)
                        .thenComparing(ReceiptMessage::delayed)
        );
        for (ReceiptMessage message : sortedMessages) {
            Objects.requireNonNull(
                    message,
                    "El conjunto de mensajes no admite valores nulos."
            );
            if (!transfers.containsKey(message.transferId())) {
                throw new IllegalArgumentException(
                        "Cada mensaje debe pertenecer a una transferencia abstracta."
                );
            }
        }
        messages = Collections.unmodifiableSet(
                new LinkedHashSet<>(sortedMessages)
        );
    }

    public TransferState transfer(String transferId) {
        TransferState transfer = transfers.get(requireText(
                transferId,
                "El identificador de transferencia es obligatorio."
        ));
        if (transfer == null) {
            throw new IllegalArgumentException(
                    "La transferencia abstracta no existe: " + transferId + "."
            );
        }
        return transfer;
    }

    public AbstractProtocolState withTransfer(TransferState transfer) {
        Objects.requireNonNull(
                transfer,
                "El estado actualizado de transferencia es obligatorio."
        );
        if (!transfers.containsKey(transfer.transferId())) {
            throw new IllegalArgumentException(
                    "No se puede agregar una transferencia fuera del estado inicial."
            );
        }
        Map<String, TransferState> updated = new TreeMap<>(transfers);
        updated.put(transfer.transferId(), transfer);
        return new AbstractProtocolState(schemaVersion, updated, messages);
    }

    public AbstractProtocolState withMessages(Set<ReceiptMessage> updatedMessages) {
        return new AbstractProtocolState(
                schemaVersion,
                transfers,
                updatedMessages
        );
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
