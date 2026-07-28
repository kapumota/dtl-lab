package dltlab.trace;

import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.protocol.ProtocolAction;
import dltlab.simulation.ScenarioCatalog;
import dltlab.simulation.SimulationEventType;
import dltlab.simulation.SimulationRun;
import dltlab.simulation.SimulationScenario;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Pruebas de exportacion determinista para la Fase 7A. */
public final class TraceExportTest {
    private TraceExportTest() {
    }

    public static void main(String[] args) throws Exception {
        testSameSeedProducesSameJsonl();
        testRepresentativeScenariosAreExported();
        testSimulationObservationsArePreserved();
        testJsonlRecordOrder();
        testExporterWritesExactBytes();
        testFormalFieldsAreAbsent();
        testJsonEscaping();
        System.out.println(
                "Las pruebas de exportacion determinista de trazas "
                        + "pasaron correctamente."
        );
    }

    private static void testSameSeedProducesSameJsonl() {
        JsonlTraceExporter exporter = new JsonlTraceExporter();
        TraceExecution first = record(
                SimulationScenario.S10_MULTIPLE_CONCURRENT_SESSIONS,
                2026L
        );
        TraceExecution second = record(
                SimulationScenario.S10_MULTIPLE_CONCURRENT_SESSIONS,
                2026L
        );

        assertTrue(
                Arrays.equals(
                        exporter.export(first),
                        exporter.export(second)
                ),
                "La misma seed debe producir los mismos bytes JSONL."
        );
        assertEquals(
                exporter.contentHash(first),
                exporter.contentHash(second),
                "La misma seed debe producir el mismo hash de contenido."
        );
        assertEquals(
                exporter.fileHash(first),
                exporter.fileHash(second),
                "La misma seed debe producir el mismo hash de archivo."
        );
    }

    private static void testRepresentativeScenariosAreExported() {
        List<SimulationScenario> scenarios = Arrays.asList(
                SimulationScenario.values()
        );

        for (SimulationScenario scenario : scenarios) {
            TraceExecution execution = record(scenario, 77L);
            assertTrue(
                    !execution.events().isEmpty(),
                    "El escenario debe producir eventos: "
                            + scenario
                            + "."
            );
            assertTrue(
                    execution.events().stream().anyMatch(
                            event -> event.javaAction()
                                    == ProtocolAction.CREATE_SESSION
                    ),
                    "La traza debe conservar la creacion de sesion."
            );
            for (int index = 0;
                 index < execution.events().size();
                 index++) {
                assertEquals(
                        (long) index,
                        execution.events().get(index).step(),
                        "Los pasos deben ser contiguos."
                );
            }
        }

        TraceExecution normal = record(
                SimulationScenario.S01_NORMAL_COMMIT,
                77L
        );
        assertTrue(
                normal.finalStates().containsValue(
                        CrossShardStatus.COMMITTED
                ),
                "El escenario normal debe terminar en COMMITTED."
        );

        TraceExecution timeout = record(
                SimulationScenario.S02_TIMEOUT_BEFORE_DELIVERY,
                77L
        );
        assertTrue(
                timeout.finalStates().containsValue(
                        CrossShardStatus.TIMED_OUT
                ),
                "El escenario de timeout debe terminar en TIMED_OUT."
        );

        TraceExecution quorum = record(
                SimulationScenario.S09_INSUFFICIENT_QUORUM,
                77L
        );
        assertTrue(
                quorum.finalStates().containsValue(
                        CrossShardStatus.FAILED_VALIDATION
                ),
                "El escenario sin quorum debe terminar "
                        + "en FAILED_VALIDATION."
        );

        TraceExecution concurrent = record(
                SimulationScenario.S10_MULTIPLE_CONCURRENT_SESSIONS,
                77L
        );
        assertEquals(
                6,
                concurrent.finalStates().size(),
                "El escenario multisesion debe conservar "
                        + "seis transferencias."
        );
    }

    private static void testSimulationObservationsArePreserved() {
        TraceExecution duplicate = record(
                SimulationScenario.S03_DUPLICATED_RECEIPT,
                91L
        );
        assertTrue(
                duplicate.events().stream().anyMatch(
                        event -> event.simulationEventType()
                                == SimulationEventType.DUPLICATE_MESSAGE
                ),
                "La traza debe conservar la duplicacion de mensajes."
        );
        assertTrue(
                duplicate.events().stream().anyMatch(
                        event -> event.simulationEventType()
                                == SimulationEventType.DELIVER_RECEIPT
                                && "RECHAZADO".equals(event.outcome())
                ),
                "La traza debe conservar la entrega duplicada rechazada."
        );

        TraceExecution timeout = record(
                SimulationScenario.S02_TIMEOUT_BEFORE_DELIVERY,
                91L
        );
        assertTrue(
                timeout.events().stream().anyMatch(
                        event -> event.simulationEventType()
                                == SimulationEventType.DROP_MESSAGE
                ),
                "La traza debe conservar la perdida de mensajes."
        );

        TraceExecution outage = record(
                SimulationScenario.S08_TEMPORARY_TARGET_OUTAGE,
                91L
        );
        assertTrue(
                outage.events().stream().anyMatch(
                        event -> event.simulationEventType()
                                == SimulationEventType.SHARD_OFFLINE
                ),
                "La traza debe conservar la indisponibilidad del shard."
        );
        assertTrue(
                outage.events().stream().anyMatch(
                        event -> event.simulationEventType()
                                == SimulationEventType.SHARD_ONLINE
                ),
                "La traza debe conservar la recuperacion del shard."
        );
    }

    private static void testJsonlRecordOrder() {
        TraceExecution execution = record(
                SimulationScenario.S01_NORMAL_COMMIT,
                2026L
        );
        String jsonl = new String(
                new JsonlTraceExporter().export(execution),
                StandardCharsets.UTF_8
        );
        String[] lines = jsonl.split("\n");

        assertEquals(
                execution.events().size() + 2,
                lines.length,
                "JSONL debe contener configuracion, eventos y resultado."
        );
        assertTrue(
                lines[0].contains(
                        "\"recordType\":\"configuracion\""
                ),
                "La primera linea debe ser la configuracion."
        );
        assertTrue(
                lines[lines.length - 1].contains(
                        "\"recordType\":\"resultado\""
                ),
                "La ultima linea debe ser el resultado."
        );
        for (int index = 1; index < lines.length - 1; index++) {
            assertTrue(
                    lines[index].contains(
                            "\"recordType\":\"evento\""
                    ),
                    "Las lineas intermedias deben ser eventos."
            );
        }
    }

    private static void testExporterWritesExactBytes()
            throws Exception {
        TraceExecution execution = record(
                SimulationScenario.S03_DUPLICATED_RECEIPT,
                31L
        );
        JsonlTraceExporter exporter = new JsonlTraceExporter();
        Path directory = Files.createTempDirectory(
                "dtl-trace-test-"
        );
        Path output = directory.resolve("traza.jsonl");

        try {
            exporter.export(execution, output);
            assertTrue(
                    Arrays.equals(
                            exporter.export(execution),
                            Files.readAllBytes(output)
                    ),
                    "La escritura debe conservar exactamente "
                            + "los bytes exportados."
            );
        } finally {
            Files.deleteIfExists(output);
            Files.deleteIfExists(directory);
        }
    }

    private static void testFormalFieldsAreAbsent() {
        String jsonl = new String(
                new JsonlTraceExporter().export(
                        record(
                                SimulationScenario.S01_NORMAL_COMMIT,
                                2026L
                        )
                ),
                StandardCharsets.UTF_8
        );

        assertFalse(
                jsonl.contains("tlaAction"),
                "Fase 7A no debe incluir acciones TLA+."
        );
        assertFalse(
                jsonl.contains("abstractAction"),
                "Fase 7A no debe incluir acciones abstractas."
        );
        assertFalse(
                jsonl.contains("conformant"),
                "Fase 7A no debe decidir conformidad."
        );
        assertFalse(
                jsonl.contains("tlcResult"),
                "Fase 7A no debe incluir resultados TLC."
        );
    }

    private static void testJsonEscaping() {
        TraceEvent event = new TraceEvent(
                1,
                0L,
                0L,
                0L,
                0,
                TraceEvent.Kind.PROTOCOL_TRANSITION,
                "transferencia-prueba",
                "transferencia-prueba",
                SimulationEventType.BEGIN_TRANSFER,
                "EJECUTADO",
                ProtocolAction.CREATE_SESSION,
                null,
                CrossShardStatus.CREATED,
                0,
                1,
                "utxo-prueba",
                "recibo-prueba",
                10L,
                3,
                4,
                null,
                null,
                "Texto con \"comillas\", salto\n"
                        + "y barra\\final."
        );
        TraceExecution execution = new TraceExecution(
                1,
                "ESCENARIO_PRUEBA",
                1L,
                2,
                3,
                "sha256:" + "0".repeat(64),
                List.of(event),
                Map.of(
                        "transferencia-prueba",
                        CrossShardStatus.CREATED
                )
        );

        String jsonl = new String(
                new JsonlTraceExporter().export(execution),
                StandardCharsets.UTF_8
        );

        assertTrue(
                jsonl.contains("\\\"comillas\\\""),
                "Las comillas deben escaparse."
        );
        assertTrue(
                jsonl.contains("salto\\ny"),
                "Los saltos de linea deben escaparse."
        );
        assertTrue(
                jsonl.contains("barra\\\\final"),
                "Las barras invertidas deben escaparse."
        );
    }

    private static TraceExecution record(
            SimulationScenario scenario,
            long seed
    ) {
        SimulationRun run = ScenarioCatalog.run(scenario, seed);
        return new TraceRecorder().record(scenario, run);
    }

    private static void assertTrue(
            boolean condition,
            String message
    ) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(
            boolean condition,
            String message
    ) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(
            Object expected,
            Object actual,
            String message
    ) {
        if (expected == null
                ? actual != null
                : !expected.equals(actual)) {
            throw new AssertionError(
                    message
                            + " Esperado: "
                            + expected
                            + ". Actual: "
                            + actual
                            + "."
            );
        }
    }
}
