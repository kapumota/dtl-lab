package dltlab.conformance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/** Genera un módulo de replay que invoca los operadores reales del modelo. */
public final class TlcTraceReplayGenerator {
    public TlcReplayArtifact generate(AbstractTrace trace) {
        Objects.requireNonNull(trace, "La traza abstracta es obligatoria.");
        String moduleName = moduleName(trace);
        return new TlcReplayArtifact(
                moduleName,
                renderModule(moduleName, trace),
                renderConfig(trace),
                trace.steps().size()
        );
    }

    private static String renderModule(
            String moduleName,
            AbstractTrace trace
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append("---- MODULE ")
                .append(moduleName)
                .append(" ----\n")
                .append("EXTENDS CrossShardCommit\n\n")
                .append("VARIABLE replayIndex\n\n")
                .append("replayVars ==\n")
                .append("    << status,\n")
                .append("       terminalStatus,\n")
                .append("       sourceShard,\n")
                .append("       targetShard,\n")
                .append("       locked,\n")
                .append("       receiptOwner,\n")
                .append("       receiptUseCount,\n")
                .append("       destinationCredit,\n")
                .append("       fundsReleased,\n")
                .append("       messages,\n")
                .append("       votes,\n")
                .append("       replayIndex >>\n\n")
                .append("ReplayInit ==\n")
                .append("    /\\ Init\n");
        appendState(builder, trace.initialState(), false, "    ");
        builder.append("    /\\ replayIndex = 0\n\n");

        for (int index = 0; index < trace.steps().size(); index++) {
            AbstractTraceStep step = trace.steps().get(index);
            builder.append("ReplayStep")
                    .append(index)
                    .append(" ==\n")
                    .append("    /\\ replayIndex = ")
                    .append(index)
                    .append("\n")
                    .append("    /\\ ")
                    .append(renderAction(step.action()))
                    .append("\n");
            appendState(builder, step.state(), true, "    ");
            builder.append("    /\\ replayIndex' = ")
                    .append(index + 1)
                    .append("\n\n");
        }

        int completed = trace.steps().size();
        builder.append("ReplayDone ==\n")
                .append("    /\\ replayIndex = ")
                .append(completed)
                .append("\n")
                .append("    /\\ UNCHANGED replayVars\n\n")
                .append("ReplayNext ==\n");
        for (int index = 0; index < completed; index++) {
            builder.append(index == 0 ? "    \\/ " : "    \\/ ")
                    .append("ReplayStep")
                    .append(index)
                    .append("\n");
        }
        builder.append("    \\/ ReplayDone\n\n")
                .append("ReplaySpec ==\n")
                .append("    /\\ ReplayInit\n")
                .append("    /\\ [][ReplayNext]_replayVars\n")
                .append("    /\\ WF_replayVars(ReplayNext)\n\n")
                .append("ReplayEventuallyComplete ==\n")
                .append("    <>(replayIndex = ")
                .append(completed)
                .append(")\n\n")
                .append("====\n");
        return builder.toString();
    }

    private static void appendState(
            StringBuilder builder,
            AbstractProtocolState state,
            boolean primed,
            String indent
    ) {
        String suffix = primed ? "'" : "";
        builder.append(indent)
                .append("/\\ status")
                .append(suffix)
                .append(" = ")
                .append(renderTransferFunction(
                        state,
                        transfer -> quote(transfer.status().tlaLiteral())
                ))
                .append("\n")
                .append(indent)
                .append("/\\ terminalStatus")
                .append(suffix)
                .append(" = ")
                .append(renderTransferFunction(
                        state,
                        transfer -> quote(
                                transfer.terminalStatus().tlaLiteral()
                        )
                ))
                .append("\n")
                .append(indent)
                .append("/\\ sourceShard")
                .append(suffix)
                .append(" = ")
                .append(renderTransferFunction(
                        state,
                        transfer -> quote(shardAtom(transfer.sourceShard()))
                ))
                .append("\n")
                .append(indent)
                .append("/\\ targetShard")
                .append(suffix)
                .append(" = ")
                .append(renderTransferFunction(
                        state,
                        transfer -> quote(shardAtom(transfer.targetShard()))
                ))
                .append("\n")
                .append(indent)
                .append("/\\ locked")
                .append(suffix)
                .append(" = ")
                .append(renderTransferFunction(
                        state,
                        transfer -> tlaBoolean(transfer.locked())
                ))
                .append("\n")
                .append(indent)
                .append("/\\ receiptOwner")
                .append(suffix)
                .append(" = ")
                .append(renderTransferFunction(
                        state,
                        transfer -> quote(
                                transfer.receiptOwnerShard() == null
                                        ? "None"
                                        : shardAtom(
                                                transfer.receiptOwnerShard()
                                        )
                        )
                ))
                .append("\n")
                .append(indent)
                .append("/\\ receiptUseCount")
                .append(suffix)
                .append(" = ")
                .append(renderTransferFunction(
                        state,
                        transfer -> Integer.toString(
                                transfer.receiptUseCount()
                        )
                ))
                .append("\n")
                .append(indent)
                .append("/\\ destinationCredit")
                .append(suffix)
                .append(" = ")
                .append(renderTransferFunction(
                        state,
                        transfer -> tlaBoolean(
                                transfer.destinationCredit()
                        )
                ))
                .append("\n")
                .append(indent)
                .append("/\\ fundsReleased")
                .append(suffix)
                .append(" = ")
                .append(renderTransferFunction(
                        state,
                        transfer -> tlaBoolean(
                                transfer.fundsReleased()
                        )
                ))
                .append("\n")
                .append(indent)
                .append("/\\ messages")
                .append(suffix)
                .append(" = ")
                .append(renderMessages(state.messages()))
                .append("\n")
                .append(indent)
                .append("/\\ votes")
                .append(suffix)
                .append(" = ")
                .append(renderTransferFunction(
                        state,
                        transfer -> renderStringSet(transfer.votes())
                ))
                .append("\n");
    }

    private static String renderTransferFunction(
            AbstractProtocolState state,
            Function<AbstractProtocolState.TransferState, String> renderer
    ) {
        List<AbstractProtocolState.TransferState> transfers = state
                .transfers()
                .values()
                .stream()
                .sorted(Comparator.comparing(
                        AbstractProtocolState.TransferState::transferId
                ))
                .toList();
        StringBuilder builder = new StringBuilder(
                "[t \\in Transfers |-> CASE "
        );
        for (int index = 0; index < transfers.size(); index++) {
            AbstractProtocolState.TransferState transfer = transfers.get(index);
            if (index > 0) {
                builder.append(" [] ");
            }
            builder.append("t = ")
                    .append(quote(transfer.transferId()))
                    .append(" -> ")
                    .append(renderer.apply(transfer));
        }
        builder.append(" [] OTHER -> ")
                .append(renderer.apply(transfers.get(0)))
                .append("]");
        return builder.toString();
    }

    private static String renderMessages(
            Set<AbstractProtocolState.ReceiptMessage> messages
    ) {
        if (messages.isEmpty()) {
            return "{}";
        }
        List<String> rendered = messages.stream()
                .sorted(
                        Comparator.comparing(
                                AbstractProtocolState.ReceiptMessage::transferId
                        ).thenComparingInt(
                                AbstractProtocolState.ReceiptMessage::copy
                        ).thenComparing(
                                AbstractProtocolState.ReceiptMessage::delayed
                        )
                )
                .map(message -> "ReceiptMessage("
                        + quote(message.transferId())
                        + ", "
                        + message.copy()
                        + ", "
                        + tlaBoolean(message.delayed())
                        + ")")
                .toList();
        return "{" + String.join(", ", rendered) + "}";
    }

    private static String renderAction(AbstractAction action) {
        return switch (action.kind()) {
            case LOCK_TRANSFER -> "LockTransfer("
                    + quote(action.transferId()) + ")";
            case RELEASE_DELAYED_RECEIPT -> "ReleaseDelayedReceipt("
                    + quote(action.transferId()) + ", "
                    + action.receiptCopy() + ")";
            case CONSUME_RECEIPT -> "ConsumeReceipt("
                    + quote(action.transferId()) + ", "
                    + action.receiptCopy() + ")";
            case CAST_VOTE -> "CastVote("
                    + quote(action.transferId()) + ", "
                    + quote(action.validatorId()) + ")";
            case COMMIT_TRANSFER -> "CommitTransfer("
                    + quote(action.transferId()) + ")";
            case TIMEOUT_TRANSFER -> "TimeoutTransfer("
                    + quote(action.transferId()) + ")";
            case STUTTER -> "Stutter";
        };
    }

    private static String renderConfig(AbstractTrace trace) {
        TreeSet<String> shards = new TreeSet<>();
        for (int shard = 0; shard < trace.shardCount(); shard++) {
            shards.add(shardAtom(shard));
        }
        TreeSet<String> transfers = new TreeSet<>(
                trace.initialState().transfers().keySet()
        );
        TreeSet<String> validators = new TreeSet<>();
        for (int index = 1; index <= trace.quorum(); index++) {
            validators.add("v" + index);
        }
        trace.initialState().transfers().values().forEach(
                transfer -> validators.addAll(transfer.votes())
        );
        for (AbstractTraceStep step : trace.steps()) {
            if (step.action().validatorId() != null) {
                validators.add(step.action().validatorId());
            }
            step.state().transfers().values().forEach(
                    transfer -> validators.addAll(transfer.votes())
            );
        }

        return "CONSTANTS\n"
                + "    Shards = " + renderStringSet(shards) + "\n"
                + "    Transfers = " + renderStringSet(transfers) + "\n"
                + "    Validators = " + renderStringSet(validators) + "\n"
                + "    Quorum = " + trace.quorum() + "\n"
                + "    ReceiptCopies = 1\n"
                + "    DelayedCopies = {}\n"
                + "    EnableTimeout = TRUE\n\n"
                + "SPECIFICATION ReplaySpec\n\n"
                + "INVARIANT TypeOK\n\n"
                + "PROPERTY ReplayEventuallyComplete\n";
    }

    private static String moduleName(AbstractTrace trace) {
        String sanitized = trace.scenarioId()
                .replaceAll("[^A-Za-z0-9_]", "_");
        if (sanitized.isEmpty()
                || !Character.isLetter(sanitized.charAt(0))) {
            sanitized = "Scenario_" + sanitized;
        }
        String material = trace.scenarioId()
                + "|"
                + trace.seed()
                + "|"
                + trace.simulationTraceHash();
        return "Replay_"
                + sanitized
                + "_"
                + sha256(material).substring(0, 12);
    }

    private static String renderStringSet(Collection<String> values) {
        List<String> sorted = new ArrayList<>(values);
        sorted.sort(String::compareTo);
        return "{" + sorted.stream()
                .map(TlcTraceReplayGenerator::quote)
                .reduce((left, right) -> left + ", " + right)
                .orElse("") + "}";
    }

    private static String shardAtom(int shard) {
        return "s" + shard;
    }

    private static String tlaBoolean(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    private static String quote(String value) {
        Objects.requireNonNull(value, "El literal TLA+ es obligatorio.");
        StringBuilder builder = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (character < 0x20) {
                        builder.append(String.format(
                                Locale.ROOT,
                                "\\u%04x",
                                (int) character
                        ));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }
        return builder.append('"').toString();
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(
                    "SHA-256 no está disponible en la plataforma.",
                    error
            );
        }
    }
}
