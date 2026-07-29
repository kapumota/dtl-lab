package dltlab.conformance;

import dltlab.simulation.ScenarioCatalog;
import dltlab.simulation.SimulationRun;
import dltlab.simulation.SimulationScenario;
import dltlab.trace.TraceExecution;
import dltlab.trace.TraceRecorder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Ejecuta un único caso de conformidad sin modificar el modelo ni el catálogo. */
public final class ConformanceCaseRunner {
    private ConformanceCaseRunner() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            throw new IllegalArgumentException(
                    "Uso: ConformanceCaseRunner <salida> <tla2tools.jar> "
                            + "<CrossShardCommit.tla> <valid|negative> <caso> <seed>"
            );
        }

        Path outputDirectory = Path.of(args[0]).toAbsolutePath().normalize();
        Path tlaToolsJar = Path.of(args[1]).toAbsolutePath().normalize();
        Path baseSpecification = Path.of(args[2]).toAbsolutePath().normalize();
        String caseType = args[3];
        String caseId = args[4];
        long seed = Long.parseLong(args[5]);

        Files.createDirectories(outputDirectory);
        TraceConformanceChecker checker = new TraceConformanceChecker();

        CaseEvaluation evaluation;
        if ("valid".equals(caseType)) {
            evaluation = evaluateValid(
                    caseId,
                    seed,
                    outputDirectory,
                    tlaToolsJar,
                    baseSpecification,
                    checker
            );
        } else if ("negative".equals(caseType)) {
            evaluation = evaluateNegative(
                    caseId,
                    seed,
                    outputDirectory,
                    tlaToolsJar,
                    baseSpecification,
                    checker
            );
        } else {
            throw new IllegalArgumentException(
                    "El tipo de caso debe ser valid o negative."
            );
        }

        Path resultPath = outputDirectory.resolve("case-result.json");
        Files.writeString(
                resultPath,
                renderJson(evaluation),
                StandardCharsets.UTF_8
        );

        System.out.println(
                evaluation.caseId()
                        + ": "
                        + (evaluation.expectationMet()
                        ? "RESULTADO ESPERADO"
                        : "RESULTADO INESPERADO")
                        + " - "
                        + evaluation.result().message()
        );

        if (!evaluation.expectationMet()) {
            throw new IllegalStateException(
                    "El caso de conformidad no coincidió con su expectativa."
            );
        }
    }

    private static CaseEvaluation evaluateValid(
            String caseId,
            long seed,
            Path outputDirectory,
            Path tlaToolsJar,
            Path baseSpecification,
            TraceConformanceChecker checker
    ) throws Exception {
        SimulationScenario scenario;
        try {
            scenario = SimulationScenario.valueOf(caseId);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException(
                    "No existe el escenario válido solicitado: " + caseId + ".",
                    error
            );
        }

        SimulationRun run = ScenarioCatalog.run(scenario, seed);
        TraceExecution concrete = new TraceRecorder().record(scenario, run);
        AbstractTrace abstractTrace = new JavaToTlaStateMapper().map(concrete);
        ConformanceResult result = checker.check(
                abstractTrace,
                outputDirectory,
                baseSpecification,
                tlaToolsJar
        );

        return new CaseEvaluation(
                caseId,
                "valid",
                seed,
                result,
                result.accepted(),
                null,
                null,
                null,
                null,
                null
        );
    }

    private static CaseEvaluation evaluateNegative(
            String caseId,
            long seed,
            Path outputDirectory,
            Path tlaToolsJar,
            Path baseSpecification,
            TraceConformanceChecker checker
    ) throws Exception {
        NegativeTraceCase negativeCase = findNegativeCase(caseId, seed);
        ConformanceResult result = checker.check(
                negativeCase.trace(),
                outputDirectory,
                baseSpecification,
                tlaToolsJar
        );

        boolean diagnosticMatches = !result.accepted()
                && Objects.equals(
                negativeCase.expectedRejectedAbstractStep(),
                result.rejectedAbstractStep()
        )
                && Objects.equals(
                negativeCase.expectedRejectedConcreteStep(),
                result.rejectedConcreteStep()
        )
                && Objects.equals(
                negativeCase.expectedRejectedAction().tlaName(),
                result.rejectedAction()
        )
                && Objects.equals(
                negativeCase.expectedTransferId(),
                result.transferId()
        );

        return new CaseEvaluation(
                caseId,
                "negative",
                seed,
                result,
                diagnosticMatches,
                diagnosticMatches,
                negativeCase.expectedRejectedAbstractStep(),
                negativeCase.expectedRejectedConcreteStep(),
                negativeCase.expectedRejectedAction().tlaName(),
                negativeCase.expectedTransferId()
        );
    }

    private static NegativeTraceCase findNegativeCase(
            String caseId,
            long seed
    ) {
        List<NegativeTraceCase> cases = new NegativeTraceCatalog().create(seed);
        return cases.stream()
                .filter(item -> item.mutationId().equals(caseId))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe la mutación negativa solicitada: "
                                        + caseId
                                        + "."
                        )
                );
    }

    private static String renderJson(CaseEvaluation evaluation) {
        ConformanceResult result = evaluation.result();
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        append(builder, "schema_version", "1", false);
        append(builder, "case_id", json(evaluation.caseId()), false);
        append(builder, "case_type", json(evaluation.caseType()), false);
        append(builder, "seed", Long.toString(evaluation.seed()), false);
        append(builder, "accepted", Boolean.toString(result.accepted()), false);
        append(
                builder,
                "expectation_met",
                Boolean.toString(evaluation.expectationMet()),
                false
        );
        append(
                builder,
                "diagnostic_matches",
                nullableBoolean(evaluation.diagnosticMatches()),
                false
        );
        append(builder, "exit_code", Integer.toString(result.exitCode()), false);
        append(
                builder,
                "checked_abstract_steps",
                Integer.toString(result.checkedAbstractSteps()),
                false
        );
        append(
                builder,
                "rejected_abstract_step",
                nullableNumber(result.rejectedAbstractStep()),
                false
        );
        append(
                builder,
                "rejected_concrete_step",
                nullableNumber(result.rejectedConcreteStep()),
                false
        );
        append(
                builder,
                "rejected_action",
                nullableString(result.rejectedAction()),
                false
        );
        append(
                builder,
                "transfer_id",
                nullableString(result.transferId()),
                false
        );
        append(
                builder,
                "expected_rejected_abstract_step",
                nullableNumber(evaluation.expectedAbstractStep()),
                false
        );
        append(
                builder,
                "expected_rejected_concrete_step",
                nullableNumber(evaluation.expectedConcreteStep()),
                false
        );
        append(
                builder,
                "expected_action",
                nullableString(evaluation.expectedAction()),
                false
        );
        append(
                builder,
                "expected_transfer_id",
                nullableString(evaluation.expectedTransferId()),
                false
        );
        append(builder, "message", json(result.message()), false);
        append(
                builder,
                "module",
                json(relative(evaluation.outputRoot(), result.modulePath())),
                false
        );
        append(
                builder,
                "config",
                json(relative(evaluation.outputRoot(), result.configPath())),
                false
        );
        append(
                builder,
                "stdout",
                json(relative(evaluation.outputRoot(), result.stdoutPath())),
                false
        );
        append(
                builder,
                "stderr",
                json(relative(evaluation.outputRoot(), result.stderrPath())),
                true
        );
        builder.append("}\n");
        return builder.toString();
    }

    private static void append(
            StringBuilder builder,
            String name,
            String value,
            boolean last
    ) {
        builder.append("  ")
                .append(json(name))
                .append(": ")
                .append(value);
        if (!last) {
            builder.append(',');
        }
        builder.append('\n');
    }

    private static String relative(Path root, Path path) {
        return root.toAbsolutePath()
                .normalize()
                .relativize(path.toAbsolutePath().normalize())
                .toString()
                .replace('\\', '/');
    }

    private static String nullableBoolean(Boolean value) {
        return value == null ? "null" : Boolean.toString(value);
    }

    private static String nullableNumber(Number value) {
        return value == null ? "null" : value.toString();
    }

    private static String nullableString(String value) {
        return value == null ? "null" : json(value);
    }

    private static String json(String value) {
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }

    private record CaseEvaluation(
            String caseId,
            String caseType,
            long seed,
            ConformanceResult result,
            boolean expectationMet,
            Boolean diagnosticMatches,
            Integer expectedAbstractStep,
            Long expectedConcreteStep,
            String expectedAction,
            String expectedTransferId
    ) {
        private CaseEvaluation {
            Objects.requireNonNull(caseId, "El identificador es obligatorio.");
            Objects.requireNonNull(caseType, "El tipo de caso es obligatorio.");
            Objects.requireNonNull(result, "El resultado es obligatorio.");
        }

        private Path outputRoot() {
            return result.modulePath().getParent();
        }
    }
}
