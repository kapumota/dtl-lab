package dltlab.trace;

import dltlab.simulation.ScenarioCatalog;
import dltlab.simulation.SimulationRun;
import dltlab.simulation.SimulationScenario;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Exporta un catalogo reproducible de trazas para todos los escenarios. */
public final class TraceCatalogExporter {
    private static final long DEFAULT_SEED = 2026L;
    private static final Path DEFAULT_OUTPUT =
            Path.of("results", "traces", "catalog-v1");

    private TraceCatalogExporter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 2) {
            throw new IllegalArgumentException(
                    "Uso: TraceCatalogExporter [directorio_salida] [seed].");
        }

        Path outputDirectory = args.length >= 1
                ? Path.of(args[0])
                : DEFAULT_OUTPUT;
        long seed = args.length == 2
                ? parseSeed(args[1])
                : DEFAULT_SEED;

        exportCatalog(outputDirectory, seed);
    }

    public static void exportCatalog(
            Path outputDirectory,
            long seed
    ) throws IOException {
        Files.createDirectories(outputDirectory);

        TraceRecorder recorder = new TraceRecorder();
        JsonlTraceExporter exporter = new JsonlTraceExporter();
        StringBuilder manifest = new StringBuilder();
        manifest.append(
                "scenario_id,seed,event_count,content_hash,file_hash,file\n"
        );

        for (SimulationScenario scenario
                : SimulationScenario.values()) {
            SimulationRun run = ScenarioCatalog.run(
                    scenario,
                    seed
            );
            TraceExecution execution = recorder.record(
                    scenario,
                    run
            );
            String filename = scenario.name()
                    .toLowerCase(Locale.ROOT)
                    + ".jsonl";
            Path output = outputDirectory.resolve(filename);

            exporter.export(execution, output);
            manifest.append(scenario.name())
                    .append(',')
                    .append(seed)
                    .append(',')
                    .append(execution.events().size())
                    .append(',')
                    .append(exporter.contentHash(execution))
                    .append(',')
                    .append(exporter.fileHash(execution))
                    .append(',')
                    .append(filename)
                    .append('\n');
        }

        Files.writeString(
                outputDirectory.resolve("manifest.csv"),
                manifest.toString(),
                StandardCharsets.UTF_8
        );

        System.out.println(
                "Catalogo de trazas exportado en "
                        + outputDirectory.toAbsolutePath()
                        + "."
        );
    }

    private static long parseSeed(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(
                    "La seed debe ser un numero entero.",
                    error
            );
        }
    }
}
