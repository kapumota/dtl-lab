package dltlab.conformance;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Ejecuta TLC sobre el corpus negativo y exige cada rechazo esperado. */
public final class NegativeTraceCatalogRunner {
    private NegativeTraceCatalogRunner() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3 || args.length > 4) {
            throw new IllegalArgumentException(
                    "Uso: NegativeTraceCatalogRunner <salida> <tla2tools.jar> "
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
        TraceConformanceChecker checker = new TraceConformanceChecker();
        List<Evaluation> evaluations = new ArrayList<>();

        for (NegativeTraceCase negativeCase
                : new NegativeTraceCatalog().create(seed)) {
            ConformanceResult result = checker.check(
                    negativeCase.trace(),
                    outputDirectory,
                    baseSpecification,
                    tlaToolsJar
            );
            boolean matched = matchesExpectation(negativeCase, result);
            evaluations.add(new Evaluation(negativeCase, result, matched));

            System.out.println(
                    negativeCase.mutationId()
                            + ": "
                            + (result.accepted()
                            ? "ACEPTADA INESPERADAMENTE"
                            : matched
                            ? "RECHAZADA COMO SE ESPERABA"
                            : "RECHAZADA CON DIAGNOSTICO DISTINTO")
                            + " - "
                            + result.message()
            );
        }

        Path manifest = outputDirectory.resolve("manifest.csv");
        Files.writeString(
                manifest,
                renderManifest(seed, outputDirectory, evaluations),
                StandardCharsets.UTF_8
        );

        long matched = evaluations.stream()
                .filter(Evaluation::matched)
                .count();
        if (matched != evaluations.size()) {
            throw new IllegalStateException(
                    "El corpus negativo contiene "
                            + (evaluations.size() - matched)
                            + " resultados que no coinciden con el rechazo esperado."
            );
        }

        System.out.println(
                "TLC rechazo las "
                        + matched
                        + " trazas corruptas del corpus."
        );
        System.out.println("Manifiesto: " + manifest + ".");
    }

    private static boolean matchesExpectation(
            NegativeTraceCase negativeCase,
            ConformanceResult result
    ) {
        return !result.accepted()
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
    }

    private static String renderManifest(
            long seed,
            Path outputDirectory,
            List<Evaluation> evaluations
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                "mutation_id,source_scenario,seed,expected_property,description,"
                        + "expected_abstract_step,expected_concrete_step,"
                        + "expected_action,expected_transfer_id,accepted,"
                        + "diagnostic_matches,exit_code,checked_abstract_steps,"
                        + "rejected_abstract_step,rejected_concrete_step,"
                        + "rejected_action,transfer_id,message,module,config,stdout,stderr\n"
        );

        for (Evaluation evaluation : evaluations) {
            NegativeTraceCase negativeCase = evaluation.negativeCase();
            ConformanceResult result = evaluation.result();
            builder.append(csv(negativeCase.mutationId())).append(',')
                    .append(csv(negativeCase.sourceScenarioId())).append(',')
                    .append(seed).append(',')
                    .append(csv(negativeCase.expectedProperty())).append(',')
                    .append(csv(negativeCase.description())).append(',')
                    .append(negativeCase.expectedRejectedAbstractStep()).append(',')
                    .append(negativeCase.expectedRejectedConcreteStep()).append(',')
                    .append(csv(negativeCase.expectedRejectedAction().tlaName())).append(',')
                    .append(csv(negativeCase.expectedTransferId())).append(',')
                    .append(result.accepted()).append(',')
                    .append(evaluation.matched()).append(',')
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

    private record Evaluation(
            NegativeTraceCase negativeCase,
            ConformanceResult result,
            boolean matched
    ) {
        private Evaluation {
            Objects.requireNonNull(
                    negativeCase,
                    "El caso negativo es obligatorio."
            );
            Objects.requireNonNull(
                    result,
                    "El resultado de TLC es obligatorio."
            );
        }
    }
}
