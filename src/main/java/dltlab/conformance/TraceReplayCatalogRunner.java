package dltlab.conformance;

import dltlab.simulation.ScenarioCatalog;
import dltlab.simulation.SimulationRun;
import dltlab.simulation.SimulationScenario;
import dltlab.trace.TraceExecution;
import dltlab.trace.TraceRecorder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Ejecuta replay TLC para el catálogo válido de escenarios deterministas. */
public final class TraceReplayCatalogRunner {
    private TraceReplayCatalogRunner() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3 || args.length > 4) {
            throw new IllegalArgumentException(
                    "Uso: TraceReplayCatalogRunner <salida> <tla2tools.jar> "
                            + "<CrossShardCommit.tla> [seed]"
            );
        }

        Path outputDirectory = Path.of(args[0]).toAbsolutePath().normalize();
        Path tlaToolsJar = Path.of(args[1]).toAbsolutePath().normalize();
        Path baseSpecification = Path.of(args[2])
                .toAbsolutePath()
                .normalize();
        long seed = args.length == 4 ? Long.parseLong(args[3]) : 2026L;

        Files.createDirectories(outputDirectory);
        TraceRecorder recorder = new TraceRecorder();
        JavaToTlaStateMapper mapper = new JavaToTlaStateMapper();
        TraceConformanceChecker checker = new TraceConformanceChecker();
        List<ConformanceResult> results = new ArrayList<>();

        for (SimulationScenario scenario : SimulationScenario.values()) {
            SimulationRun run = ScenarioCatalog.run(scenario, seed);
            TraceExecution concrete = recorder.record(scenario, run);
            AbstractTrace abstractTrace = mapper.map(concrete);
            ConformanceResult result = checker.check(
                    abstractTrace,
                    outputDirectory,
                    baseSpecification,
                    tlaToolsJar
            );
            results.add(result);
            System.out.println(
                    scenario
                            + ": "
                            + (result.accepted() ? "ACEPTADA" : "RECHAZADA")
                            + " - "
                            + result.message()
            );
        }

        Path manifest = outputDirectory.resolve("manifest.csv");
        Files.writeString(
                manifest,
                renderManifest(seed, outputDirectory, results),
                StandardCharsets.UTF_8
        );

        long accepted = results.stream()
                .filter(ConformanceResult::accepted)
                .count();
        if (accepted != results.size()) {
            throw new IllegalStateException(
                    "TLC rechazó "
                            + (results.size() - accepted)
                            + " escenarios del catálogo válido."
            );
        }

        System.out.println(
                "TLC aceptó los "
                        + accepted
                        + " escenarios del catálogo de replay."
        );
        System.out.println("Manifiesto: " + manifest + ".");
    }

    private static String renderManifest(
            long seed,
            Path outputDirectory,
            List<ConformanceResult> results
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                "scenario_id,seed,accepted,exit_code,checked_abstract_steps,"
                        + "rejected_abstract_step,rejected_concrete_step,"
                        + "rejected_action,transfer_id,message,module,config,stdout,stderr\n"
        );
        for (ConformanceResult result : results) {
            builder.append(csv(result.scenarioId())).append(',')
                    .append(seed).append(',')
                    .append(result.accepted()).append(',')
                    .append(result.exitCode()).append(',')
                    .append(result.checkedAbstractSteps()).append(',')
                    .append(nullable(result.rejectedAbstractStep())).append(',')
                    .append(nullable(result.rejectedConcreteStep())).append(',')
                    .append(csv(result.rejectedAction())).append(',')
                    .append(csv(result.transferId())).append(',')
                    .append(csv(result.message())).append(',')
                    .append(csv(relative(outputDirectory, result.modulePath()))).append(',')
                    .append(csv(relative(outputDirectory, result.configPath()))).append(',')
                    .append(csv(relative(outputDirectory, result.stdoutPath()))).append(',')
                    .append(csv(relative(outputDirectory, result.stderrPath()))).append('\n');
        }
        return builder.toString();
    }

    private static String relative(Path root, Path path) {
        return root.toAbsolutePath()
                .normalize()
                .relativize(path.toAbsolutePath().normalize())
                .toString()
                .replace('\\', '/');
    }

    private static String nullable(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
