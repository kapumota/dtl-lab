package dltlab.conformance;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** Ejecuta TLC como oráculo sobre un artefacto de replay generado. */
public final class TraceConformanceChecker {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(120);

    private final TlcTraceReplayGenerator generator;
    private final TlcReplayResultParser parser;
    private final Duration timeout;

    public TraceConformanceChecker() {
        this(
                new TlcTraceReplayGenerator(),
                new TlcReplayResultParser(),
                DEFAULT_TIMEOUT
        );
    }

    public TraceConformanceChecker(
            TlcTraceReplayGenerator generator,
            TlcReplayResultParser parser,
            Duration timeout
    ) {
        this.generator = Objects.requireNonNull(
                generator,
                "El generador de replay es obligatorio."
        );
        this.parser = Objects.requireNonNull(
                parser,
                "El parser de resultados TLC es obligatorio."
        );
        this.timeout = Objects.requireNonNull(
                timeout,
                "El timeout de TLC es obligatorio."
        );
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException(
                    "El timeout de TLC debe ser positivo."
            );
        }
    }

    public ConformanceResult check(
            AbstractTrace trace,
            Path outputDirectory,
            Path baseSpecification,
            Path tlaToolsJar
    ) throws IOException, InterruptedException {
        Objects.requireNonNull(trace, "La traza abstracta es obligatoria.");
        outputDirectory = Objects.requireNonNull(
                outputDirectory,
                "El directorio de salida es obligatorio."
        ).toAbsolutePath().normalize();
        baseSpecification = requireFile(
                baseSpecification,
                "No existe la especificación base CrossShardCommit.tla."
        );
        tlaToolsJar = requireFile(
                tlaToolsJar,
                "No existe el archivo tla2tools.jar."
        );

        TlcReplayArtifact artifact = generator.generate(trace);
        Path runDirectory = outputDirectory.resolve(artifact.moduleName());
        resetDirectory(runDirectory);
        Path metaDirectory = runDirectory.resolve("tlc-meta");
        Files.createDirectories(metaDirectory);

        Path modulePath = runDirectory.resolve(
                artifact.moduleName() + ".tla"
        );
        Path configPath = runDirectory.resolve(
                artifact.moduleName() + ".cfg"
        );
        Path copiedBase = runDirectory.resolve("CrossShardCommit.tla");
        Path stdoutPath = runDirectory.resolve("tlc.stdout.txt");
        Path stderrPath = runDirectory.resolve("tlc.stderr.txt");

        Files.writeString(
                modulePath,
                artifact.moduleText(),
                StandardCharsets.UTF_8
        );
        Files.writeString(
                configPath,
                artifact.configText(),
                StandardCharsets.UTF_8
        );
        Files.copy(
                baseSpecification,
                copiedBase,
                StandardCopyOption.REPLACE_EXISTING
        );

        ProcessBuilder processBuilder = new ProcessBuilder(command(
                tlaToolsJar,
                metaDirectory,
                configPath,
                modulePath
        ));
        processBuilder.directory(runDirectory.toFile());
        processBuilder.redirectOutput(stdoutPath.toFile());
        processBuilder.redirectError(stderrPath.toFile());

        Process process = processBuilder.start();
        boolean completed = process.waitFor(
                timeout.toMillis(),
                TimeUnit.MILLISECONDS
        );
        int exitCode;
        if (!completed) {
            process.destroyForcibly();
            process.waitFor();
            exitCode = -1;
        } else {
            exitCode = process.exitValue();
        }

        String stdout = readIfPresent(stdoutPath);
        String stderr = readIfPresent(stderrPath);
        return parser.parse(
                trace,
                exitCode,
                stdout,
                stderr,
                modulePath,
                configPath,
                stdoutPath,
                stderrPath
        );
    }

    private static List<String> command(
            Path tlaToolsJar,
            Path metaDirectory,
            Path configPath,
            Path modulePath
    ) {
        List<String> command = new ArrayList<>();
        command.add(javaExecutable().toString());
        command.add("-XX:+UseParallelGC");
        command.add("-cp");
        command.add(tlaToolsJar.toString());
        command.add("tlc2.TLC");
        command.add("-workers");
        command.add("1");
        command.add("-metadir");
        command.add(metaDirectory.toString());
        command.add("-config");
        command.add(configPath.getFileName().toString());
        command.add(modulePath.getFileName().toString());
        return List.copyOf(command);
    }

    private static void resetDirectory(Path directory) throws IOException {
        if (Files.notExists(directory)) {
            return;
        }
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static Path javaExecutable() {
        String executable = System.getProperty("os.name", "")
                .toLowerCase()
                .contains("win")
                ? "java.exe"
                : "java";
        return Path.of(
                System.getProperty("java.home"),
                "bin",
                executable
        );
    }

    private static Path requireFile(Path path, String message) {
        Objects.requireNonNull(path, message);
        Path normalized = path.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalized)) {
            throw new IllegalArgumentException(message + " Ruta: " + normalized + ".");
        }
        return normalized;
    }

    private static String readIfPresent(Path path) throws IOException {
        return Files.isRegularFile(path)
                ? Files.readString(path, StandardCharsets.UTF_8)
                : "";
    }
}
