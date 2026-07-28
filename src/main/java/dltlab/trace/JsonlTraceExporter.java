package dltlab.trace;

import dltlab.sharding.CrossShardStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Serializa una ejecucion como JSONL estable y calcula hashes SHA-256. */
public final class JsonlTraceExporter implements TraceExporter {
    @Override
    public byte[] export(TraceExecution execution) {
        Objects.requireNonNull(
                execution,
                "La ejecucion de traza es obligatoria."
        );
        String body = renderBody(execution);
        String contentHash = hash(
                body.getBytes(StandardCharsets.UTF_8)
        );
        String result = renderResult(
                execution,
                "sha256:" + contentHash
        );
        return (body + result + "\n")
                .getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void export(
            TraceExecution execution,
            Path output
    ) throws IOException {
        Objects.requireNonNull(
                output,
                "La ruta de salida es obligatoria."
        );
        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(output, export(execution));
    }

    @Override
    public String contentHash(TraceExecution execution) {
        Objects.requireNonNull(
                execution,
                "La ejecucion de traza es obligatoria."
        );
        return "sha256:" + hash(
                renderBody(execution)
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public String fileHash(TraceExecution execution) {
        return "sha256:" + hash(export(execution));
    }

    private static String renderBody(TraceExecution execution) {
        StringBuilder builder = new StringBuilder();
        builder.append(renderConfiguration(execution))
                .append('\n');
        for (TraceEvent event : execution.events()) {
            builder.append(renderEvent(event))
                    .append('\n');
        }
        return builder.toString();
    }

    private static String renderConfiguration(
            TraceExecution execution
    ) {
        return "{"
                + field("recordType", "configuracion")
                + ","
                + numberField(
                        "schemaVersion",
                        execution.schemaVersion()
                )
                + ","
                + field("scenarioId", execution.scenarioId())
                + ","
                + numberField("seed", execution.seed())
                + ","
                + numberField(
                        "shardCount",
                        execution.shardCount()
                )
                + ","
                + numberField("quorum", execution.quorum())
                + ","
                + field(
                        "simulationTraceHash",
                        execution.simulationTraceHash()
                )
                + "}";
    }

    private static String renderEvent(TraceEvent event) {
        return "{"
                + field("recordType", "evento")
                + ","
                + numberField(
                        "schemaVersion",
                        event.schemaVersion()
                )
                + ","
                + numberField("step", event.step())
                + ","
                + numberField(
                        "simulationSequence",
                        event.simulationSequence()
                )
                + ","
                + nullableNumberField(
                        "sessionSequence",
                        event.sessionSequence()
                )
                + ","
                + numberField(
                        "logicalRound",
                        event.logicalRound()
                )
                + ","
                + field("kind", event.kind().name())
                + ","
                + field("subjectId", event.subjectId())
                + ","
                + nullableField(
                        "transferId",
                        event.transferId()
                )
                + ","
                + field(
                        "simulationEventType",
                        event.simulationEventType().name()
                )
                + ","
                + field("outcome", event.outcome())
                + ","
                + nullableEnumField(
                        "javaAction",
                        event.javaAction()
                )
                + ","
                + nullableEnumField(
                        "previousStatus",
                        event.previousStatus()
                )
                + ","
                + nullableEnumField(
                        "nextStatus",
                        event.nextStatus()
                )
                + ","
                + nullableNumberField(
                        "sourceShard",
                        event.sourceShard()
                )
                + ","
                + nullableNumberField(
                        "targetShard",
                        event.targetShard()
                )
                + ","
                + nullableField(
                        "sourceUtxoKey",
                        event.sourceUtxoKey()
                )
                + ","
                + nullableField(
                        "receiptId",
                        event.receiptId()
                )
                + ","
                + nullableNumberField(
                        "amount",
                        event.amount()
                )
                + ","
                + nullableNumberField(
                        "sourceApprovals",
                        event.sourceApprovals()
                )
                + ","
                + nullableNumberField(
                        "sourceValidators",
                        event.sourceValidators()
                )
                + ","
                + nullableNumberField(
                        "targetApprovals",
                        event.targetApprovals()
                )
                + ","
                + nullableNumberField(
                        "targetValidators",
                        event.targetValidators()
                )
                + ","
                + field("detail", event.detail())
                + "}";
    }

    private static String renderResult(
            TraceExecution execution,
            String contentHash
    ) {
        StringBuilder states = new StringBuilder();
        states.append('{');
        boolean first = true;
        for (Map.Entry<String, CrossShardStatus> entry
                : new TreeMap<>(
                        execution.finalStates()
                ).entrySet()) {
            if (!first) {
                states.append(',');
            }
            states.append(quote(entry.getKey()))
                    .append(':')
                    .append(quote(entry.getValue().name()));
            first = false;
        }
        states.append('}');

        return "{"
                + field("recordType", "resultado")
                + ","
                + numberField(
                        "schemaVersion",
                        execution.schemaVersion()
                )
                + ","
                + field("scenarioId", execution.scenarioId())
                + ","
                + numberField("seed", execution.seed())
                + ","
                + numberField(
                        "eventCount",
                        execution.events().size()
                )
                + ",\"finalStates\":"
                + states
                + ","
                + field("contentHash", contentHash)
                + "}";
    }

    private static String field(String name, String value) {
        return quote(name) + ":" + quote(value);
    }

    private static String nullableField(
            String name,
            String value
    ) {
        return quote(name)
                + ":"
                + (value == null ? "null" : quote(value));
    }

    private static String numberField(String name, long value) {
        return quote(name) + ":" + value;
    }

    private static String nullableNumberField(
            String name,
            Number value
    ) {
        return quote(name)
                + ":"
                + (value == null ? "null" : value);
    }

    private static String nullableEnumField(
            String name,
            Enum<?> value
    ) {
        return quote(name)
                + ":"
                + (value == null
                ? "null"
                : quote(value.name()));
    }

    private static String quote(String value) {
        return "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (character < 0x20) {
                        builder.append("\\u")
                                .append(String.format(
                                        Locale.ROOT,
                                        "%04x",
                                        (int) character
                                ));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }
        return builder.toString();
    }

    private static String hash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance(
                    "SHA-256"
            );
            return java.util.HexFormat.of()
                    .formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(
                    "SHA-256 no esta disponible en la plataforma.",
                    error
            );
        }
    }
}
