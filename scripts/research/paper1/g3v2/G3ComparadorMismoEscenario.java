package dltlab.research.paper1.g3v2;

import dltlab.conformance.v2.V2IntermediateAudit;
import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.ShardManager;
import dltlab.sharding.protocol.ProtocolContext;
import dltlab.sharding.protocol.ProtocolException;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;
import dltlab.verification.LedgerValueOracle;
import dltlab.wallet.Wallet;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * G3-v2: un escenario Java real por proceso; todos los observadores evalúan
 * el MISMO resultado ejecutado, sin repetir la transferencia por detector.
 *
 * G2 depende del journal G1. El resultado de G2 no es evidencia independiente
 * de ese journal. La comprobación de salud G1 no es detector semántico.
 * Ninguna de estas pruebas demuestra recuperación tras crash.
 */
public final class G3ComparadorMismoEscenario {
    private G3ComparadorMismoEscenario() { }

    @FunctionalInterface
    private interface Verificacion {
        void run() throws Exception;
    }

    private static String escenarioActual;

    private static final class NoEvaluable extends RuntimeException {
        NoEvaluable(String mensaje) { super(mensaje); }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Uso: G3ComparadorMismoEscenario beneficiario|rollback|contencion");
        }
        escenarioActual = args[0];
        switch (escenarioActual) {
            case "beneficiario" -> beneficiario();
            case "rollback" -> rollback();
            case "contencion" -> contencion();
            default -> throw new IllegalArgumentException("Escenario G3-v2 no registrado: " + escenarioActual);
        }
    }

    private static void verificar(String nombre, Verificacion f) {
        try {
            f.run();
            resultado(nombre, "PASS", "ok");
        } catch (NoEvaluable ex) {
            resultado(nombre, "NO_EVALUABLE", ex.getMessage());
        } catch (AssertionError ex) {
            resultado(nombre, "FAIL", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        } catch (Exception ex) {
            resultado(nombre, "ERROR_OBSERVADOR", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private static void resultado(String nombre, String estado, String detalle) {
        String limpio = detalle == null ? "sin_detalle" : detalle.replace('\n', ' ').replace('\r', ' ').replace('|', '/');
        System.out.println("G3V2|" + escenarioActual + "|" + nombre + "|" + estado + "|" + limpio);
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    private static void g2(ShardManager manager, CrossShardSession session,
                           String origen, String beneficiarioCodificado,
                           V2IntermediateAudit.Verdict esperado) {
        if (session == null || session.receipt() == null) {
            resultado("G2_JOURNAL", "NO_EVALUABLE", "No existe recibo de sesion");
            return;
        }
        var t = session.transfer();
        try {
            var result = V2IntermediateAudit.evaluate(new V2IntermediateAudit.Input(
                    t.id(), session.receipt().receiptId(), origen, beneficiarioCodificado,
                    t.sourceShardId(), t.targetShardId(), t.amount(),
                    manager.auditFailureCount(), manager.commitAttemptEvents()));
            // El resultado válido esperado se aplica únicamente al control sin mutar.
            resultado("G2_JOURNAL",
                    result.verdict() == V2IntermediateAudit.Verdict.NO_EVALUABLE ? "NO_EVALUABLE" :
                    result.verdict() == esperado ? "PASS" : "FAIL",
                    "veredicto=" + result.verdict() + ";" + result.detail());
        } catch (RuntimeException error) {
            resultado("G2_JOURNAL", "ERROR_OBSERVADOR", error.toString());
        }
    }

    // Conservación, identidad UTXO y titularidad son predicados diferentes.
    // La ausencia de un UTXO impide evaluar su titular, pero sí viola estructura.
    private static void mismaEstructuraEImportes(LedgerValueOracle.Snapshot antes,
                                                  LedgerValueOracle.Snapshot despues) {
        exigir(antes.utxos().keySet().equals(despues.utxos().keySet()),
                "Conjunto de shards cambiado");
        for (int shard : antes.utxos().keySet()) {
            Map<String, LedgerValueOracle.OutputState> inicial = antes.utxos().get(shard);
            Map<String, LedgerValueOracle.OutputState> finalizado = despues.utxos().get(shard);
            exigir(inicial.keySet().equals(finalizado.keySet()),
                    "UTXO agregados o faltantes en shard " + shard);
            for (String clave : inicial.keySet()) {
                exigir(inicial.get(clave).amount() == finalizado.get(clave).amount(),
                        "Importe UTXO alterado: " + clave);
            }
        }
    }

    private static void mismosTitulares(LedgerValueOracle.Snapshot antes,
                                       LedgerValueOracle.Snapshot despues) {
        if (!antes.utxos().keySet().equals(despues.utxos().keySet())) {
            throw new NoEvaluable("Shards distintos; titularidad no comparable");
        }
        for (int shard : antes.utxos().keySet()) {
            var inicial = antes.utxos().get(shard);
            var finalizado = despues.utxos().get(shard);
            if (!inicial.keySet().equals(finalizado.keySet())) {
                throw new NoEvaluable("UTXO ausentes; la titularidad exige mismas claves");
            }
            for (String clave : inicial.keySet()) {
                exigir(inicial.get(clave).recipientEncoded().equals(
                        finalizado.get(clave).recipientEncoded()),
                        "Propietario UTXO alterado: " + clave);
            }
        }
    }

    private static String codificar(Wallet cartera) {
        return Base64.getEncoder().encodeToString(cartera.getPublicKey().getEncoded());
    }

    private static void beneficiario() {
        Wallet titular = new Wallet("g3v2-titular");
        Wallet beneficiario = new Wallet("g3v2-beneficiario-distinto");
        UTXO fuente = new UTXO(new byte[]{49, 15, 21}, 0);
        var configuracion = new ShardManager.Setup(2, 4, 3);
        configuracion.addInitialUtxo(0, fuente, new Transaction.Output(10_000L, titular.getPublicKey()));
        var manager = configuracion.start();
        var transferencia = new CrossShardTransfer(0, 1, fuente, 4_000L,
                beneficiario.getPublicKey()).sign(titular.getPrivateKey());
        CrossShardSession sesion = manager.beginAtomicTransfer(transferencia, 4);
        boolean reciboEntregado = manager.deliverAtomicReceipt(transferencia.id());
        var antes = LedgerValueOracle.capture(manager.getShards());
        boolean confirmado = false;
        String excepcion = "ninguna";
        try {
            confirmado = manager.commitAtomicTransfer(transferencia.id());
        } catch (RuntimeException error) {
            excepcion = error.toString();
        }
        var despues = LedgerValueOracle.capture(manager.getShards());
        final boolean confirmadoFinal = confirmado;
        final String excepcionFinal = excepcion;
        verificar("TERMINAL", () -> exigir(reciboEntregado && confirmadoFinal
                        && manager.getSession(transferencia.id()).status() == CrossShardStatus.COMMITTED,
                "No se completo commit: " + excepcionFinal));
        verificar("VALOR", () -> LedgerValueOracle.requireConservation(antes, despues));
        verificar("ESTRUCTURA_UTXO", () -> {
            var origen = despues.utxos().get(0);
            var destino = despues.utxos().get(1);
            exigir(origen != null && destino != null, "Falta shard");
            exigir(!origen.containsKey(fuente.key()), "UTXO de origen no consumido");
            exigir(origen.size() == 1 && destino.size() == 1, "Cantidad de UTXO incorrecta");
            exigir(origen.values().iterator().next().amount() == 6_000L, "Importe de cambio incorrecto");
            exigir(destino.values().iterator().next().amount() == 4_000L, "Importe del credito incorrecto");
        });
        verificar("TITULARIDAD", () -> {
            var origen = despues.utxos().get(0);
            var destino = despues.utxos().get(1);
            if (origen == null || destino == null || origen.size() != 1 || destino.size() != 1) {
                throw new NoEvaluable("No existe exactamente una salida de cambio y de credito");
            }
            exigir(origen.values().iterator().next().recipientEncoded().equals(codificar(titular)),
                    "Titular del cambio incorrecto");
            exigir(destino.values().iterator().next().recipientEncoded().equals(codificar(beneficiario)),
                    "Destinatario del credito incorrecto");
        });
        verificar("RECIBOS", () -> {
            var recibos = despues.consumedReceipts().get(1);
            exigir(recibos != null && recibos.size() == 1
                    && recibos.contains(sesion.receipt().receiptId()),
                    "Recibo correcto no consumido unicamente en destino");
        });
        verificar("BLOQUEOS", () -> exigir(!despues.locks().get(0).contains(fuente.key()),
                "Bloqueo residual de origen"));
        g2(manager, sesion, fuente.key(), codificar(beneficiario),
                V2IntermediateAudit.Verdict.TRACE_COMMIT_VALIDADA);
        resultado("SALUD_G1_NO_DETECTOR", manager.auditFailureCount() == 0 ? "PASS" : "FAIL",
                "fallos_instrumentacion=" + manager.auditFailureCount());
    }

    private static void rollback() {
        Wallet titular = new Wallet("g3v2-rollback-titular");
        UTXO fuente = new UTXO(new byte[]{49, 16, 22}, 0);
        ProtocolContext.FailureInjector injector = punto -> {
            if (punto == ProtocolContext.FailurePoint.AFTER_TARGET_CREDIT) {
                throw new IllegalStateException("Fallo controlado despues del credito");
            }
        };
        var cfg = new ShardManager.Setup(2, 4, 3, injector);
        cfg.addInitialUtxo(0, fuente, new Transaction.Output(10_000L, titular.getPublicKey()));
        var manager = cfg.start();
        var transferencia = new CrossShardTransfer(0, 1, fuente, 4_000L,
                titular.getPublicKey()).sign(titular.getPrivateKey());
        CrossShardSession sesion = manager.beginAtomicTransfer(transferencia, 4);
        boolean reciboEntregado = manager.deliverAtomicReceipt(transferencia.id());
        var antes = LedgerValueOracle.capture(manager.getShards());
        boolean rollbackDeclarado = false;
        String excepcion = "ninguna";
        try {
            manager.commitAtomicTransfer(transferencia.id());
            excepcion = "commit aceptado pese al fallo programado";
        } catch (ProtocolException error) {
            rollbackDeclarado = error.rolledBack();
            excepcion = error.toString();
        } catch (RuntimeException error) {
            excepcion = error.toString();
        }
        var despues = LedgerValueOracle.capture(manager.getShards());
        final boolean restauradoFinal = rollbackDeclarado;
        final String excepcionFinal = excepcion;
        verificar("TERMINAL", () -> exigir(reciboEntregado && restauradoFinal
                        && manager.getSession(transferencia.id()).status() == CrossShardStatus.RECEIPT_DELIVERED,
                "Rollback no confirmado: " + excepcionFinal));
        verificar("VALOR", () -> LedgerValueOracle.requireConservation(antes, despues));
        verificar("ESTRUCTURA_UTXO", () -> mismaEstructuraEImportes(antes, despues));
        verificar("TITULARIDAD", () -> mismosTitulares(antes, despues));
        verificar("RECIBOS", () -> exigir(antes.consumedReceipts().equals(despues.consumedReceipts()),
                "Recibos no restaurados"));
        verificar("BLOQUEOS", () -> exigir(antes.locks().equals(despues.locks()),
                "Bloqueos no restaurados"));
        g2(manager, sesion, fuente.key(), codificar(titular),
                V2IntermediateAudit.Verdict.TRACE_ROLLBACK_VALIDADA);
        resultado("SALUD_G1_NO_DETECTOR", manager.auditFailureCount() == 0 ? "PASS" : "FAIL",
                "fallos_instrumentacion=" + manager.auditFailureCount());
    }

    private static void contencion() throws Exception {
        Wallet titular = new Wallet("g3v2-contencion-titular");
        UTXO fuente = new UTXO(new byte[]{49, 17, 23}, 0);
        var cfg = new ShardManager.Setup(2, 4, 3);
        cfg.addInitialUtxo(0, fuente, new Transaction.Output(10_000L, titular.getPublicKey()));
        var manager = cfg.start();
        var previo = LedgerValueOracle.capture(manager.getShards());
        var primera = new CrossShardTransfer(0, 1, fuente, 4_000L,
                titular.getPublicKey()).sign(titular.getPrivateKey());
        var segunda = new CrossShardTransfer(0, 1, fuente, 3_000L,
                titular.getPublicKey()).sign(titular.getPrivateKey());
        CountDownLatch lista = new CountDownLatch(2);
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        boolean uno = false, dos = false;
        try {
            Future<Boolean> a = hilos.submit(() -> iniciar(manager, primera, lista, salida));
            Future<Boolean> b = hilos.submit(() -> iniciar(manager, segunda, lista, salida));
            exigir(lista.await(15, TimeUnit.SECONDS), "Los hilos no alcanzaron la barrera");
            salida.countDown();
            uno = a.get(30, TimeUnit.SECONDS);
            dos = b.get(30, TimeUnit.SECONDS);
        } finally {
            salida.countDown();
            hilos.shutdownNow();
        }
        final boolean unoFinal = uno, dosFinal = dos;
        final CrossShardTransfer ganadora = uno ? primera : segunda;
        boolean entregado = false, confirmado = false;
        String error = "ninguno";
        if (uno != dos) {
            try {
                entregado = manager.deliverAtomicReceipt(ganadora.id());
                confirmado = manager.commitAtomicTransfer(ganadora.id());
            } catch (RuntimeException fallo) {
                error = fallo.toString();
            }
        }
        var posterior = LedgerValueOracle.capture(manager.getShards());
        final boolean entregadoFinal = entregado, confirmadoFinal = confirmado;
        final String errorFinal = error;
        verificar("TERMINAL", () -> exigir(unoFinal != dosFinal && entregadoFinal && confirmadoFinal,
                "Ganador unico no confirmado: " + errorFinal));
        verificar("VALOR", () -> LedgerValueOracle.requireConservation(previo, posterior));
        verificar("ESTRUCTURA_UTXO", () -> {
            if (unoFinal == dosFinal || !confirmadoFinal) {
                throw new NoEvaluable("La forma final exige un unico commit efectivo");
            }
            var origen = posterior.utxos().get(0);
            var destino = posterior.utxos().get(1);
            exigir(origen != null && destino != null && origen.size() == 1 && destino.size() == 1,
                    "Estructura de UTXO posterior incorrecta");
            exigir(manager.getSessions().size() == 1, "Cantidad de sesiones incorrecta");
            exigir(origen.values().iterator().next().amount() == 10_000L - ganadora.amount(),
                    "Cambio de la sesion ganadora incorrecto");
            exigir(destino.values().iterator().next().amount() == ganadora.amount(),
                    "Credito de la sesion ganadora incorrecto");
        });
        verificar("TITULARIDAD", () -> {
            var origen = posterior.utxos().get(0);
            var destino = posterior.utxos().get(1);
            if (origen == null || destino == null || origen.size() != 1 || destino.size() != 1) {
                throw new NoEvaluable("No hay una salida por shard para contrastar titulares");
            }
            exigir(origen.values().iterator().next().recipientEncoded().equals(codificar(titular)),
                    "Titular del cambio incorrecto");
            exigir(destino.values().iterator().next().recipientEncoded().equals(codificar(titular)),
                    "Titular del credito incorrecto");
        });
        verificar("RECIBOS", () -> {
            if (unoFinal == dosFinal || !confirmadoFinal) {
                throw new NoEvaluable("Sin unico commit, no se atribuye ausencia de recibo");
            }
            var recibos = posterior.consumedReceipts().get(1);
            exigir(recibos != null && recibos.size() == 1
                    && recibos.contains(manager.getSession(ganadora.id()).receipt().receiptId()),
                    "Recibo de la sesion ganadora no consumido exactamente una vez");
        });
        verificar("BLOQUEOS", () -> {
            if (unoFinal == dosFinal || !confirmadoFinal) {
                throw new NoEvaluable("Sin unico commit, no se atribuyen bloqueos residuales");
            }
            exigir(!posterior.locks().get(0).contains(fuente.key()),
                    "El origen conserva el bloqueo despues del commit");
        });
        if (uno != dos) {
            g2(manager, manager.getSession(ganadora.id()), fuente.key(), codificar(titular),
                    V2IntermediateAudit.Verdict.TRACE_COMMIT_VALIDADA);
        } else {
            resultado("G2_JOURNAL", "NO_EVALUABLE", "No hubo ganador unico");
        }
        resultado("SALUD_G1_NO_DETECTOR", manager.auditFailureCount() == 0 ? "PASS" : "FAIL",
                "fallos_instrumentacion=" + manager.auditFailureCount());
    }

    private static boolean iniciar(ShardManager manager, CrossShardTransfer t,
                                   CountDownLatch lista, CountDownLatch salida) throws Exception {
        lista.countDown();
        exigir(salida.await(15, TimeUnit.SECONDS), "Timeout de barrera");
        try {
            manager.beginAtomicTransfer(t, 4);
            return true;
        } catch (ProtocolException esperado) {
            return false;
        }
    }
}
