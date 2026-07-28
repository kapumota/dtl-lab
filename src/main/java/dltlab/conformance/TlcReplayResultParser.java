package dltlab.conformance;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Interpreta la salida de TLC sin decidir las guardas de Next en Java. */
public final class TlcReplayResultParser {
    private static final String SUCCESS_MARKER =
            "Model checking completed. No error has been found.";
    private static final Pattern REPLAY_INDEX = Pattern.compile(
            "(?m)^/\\\\ replayIndex = ([0-9]+)\\s*$"
    );

    public ConformanceResult parse(
            AbstractTrace trace,
            int exitCode,
            String stdout,
            String stderr,
            Path modulePath,
            Path configPath,
            Path stdoutPath,
            Path stderrPath
    ) {
        Objects.requireNonNull(trace, "La traza abstracta es obligatoria.");
        stdout = Objects.requireNonNullElse(stdout, "");
        stderr = Objects.requireNonNullElse(stderr, "");

        boolean accepted = exitCode == 0 && stdout.contains(SUCCESS_MARKER);
        if (accepted) {
            return new ConformanceResult(
                    trace.scenarioId(),
                    true,
                    exitCode,
                    trace.steps().size(),
                    null,
                    null,
                    null,
                    null,
                    "TLC aceptó todos los pasos abstractos de la traza.",
                    modulePath,
                    configPath,
                    stdoutPath,
                    stderrPath
            );
        }

        Integer replayIndex = lastReplayIndex(stdout + "\n" + stderr);
        AbstractTraceStep rejected = replayIndex != null
                && replayIndex >= 0
                && replayIndex < trace.steps().size()
                ? trace.steps().get(replayIndex)
                : null;
        int checked = replayIndex == null
                ? 0
                : Math.min(replayIndex, trace.steps().size());

        return new ConformanceResult(
                trace.scenarioId(),
                false,
                exitCode,
                checked,
                replayIndex,
                rejected == null ? null : rejected.concreteStep(),
                rejected == null ? null : rejected.action().tlaName(),
                rejected == null ? null : rejected.action().transferId(),
                diagnostic(exitCode, stdout, stderr, replayIndex),
                modulePath,
                configPath,
                stdoutPath,
                stderrPath
        );
    }

    private static Integer lastReplayIndex(String output) {
        Matcher matcher = REPLAY_INDEX.matcher(output);
        Integer result = null;
        while (matcher.find()) {
            result = Integer.parseInt(matcher.group(1));
        }
        return result;
    }

    private static String diagnostic(
            int exitCode,
            String stdout,
            String stderr,
            Integer replayIndex
    ) {
        if (exitCode == -1) {
            return "TLC excedió el tiempo máximo de ejecución."
                    + suffix(replayIndex);
        }
        String selected = firstRelevantLine(stderr);
        if (selected == null) {
            selected = firstRelevantLine(stdout);
        }
        if (selected == null) {
            selected = "TLC rechazó el replay con código " + exitCode + ".";
        }
        return selected + suffix(replayIndex);
    }

    private static String suffix(Integer replayIndex) {
        return replayIndex == null
                ? ""
                : " Índice abstracto observado: " + replayIndex + ".";
    }

    private static String firstRelevantLine(String text) {
        for (String line : text.split("\\R")) {
            String normalized = line.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            String lower = normalized.toLowerCase();
            if (lower.contains("error")
                    || lower.contains("deadlock")
                    || lower.contains("temporal")
                    || lower.contains("invariant")
                    || lower.contains("violat")) {
                return normalized;
            }
        }
        return null;
    }
}
