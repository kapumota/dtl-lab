package dltlab.sharding.protocol;

import dltlab.crypto.Hashing;
import dltlab.sharding.CrossShardSession;
import dltlab.sharding.CrossShardStatus;
import dltlab.sharding.CrossShardTransfer;
import dltlab.sharding.Receipt;
import dltlab.sharding.Shard;
import dltlab.transaction.Transaction;
import dltlab.transaction.UTXO;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.List;

import static dltlab.sharding.protocol.ReceiptObservation.Kind.*;

/** Implementa inicio, commit, abort, timeout y rollback del protocolo cross-shard. */
public final class AtomicCommitProtocol implements CrossShardProtocol {
    private final ProtocolContext context;

    public AtomicCommitProtocol(ProtocolContext context) {
        this.context = Objects.requireNonNull(context, "El contexto del protocolo es obligatorio.");
    }

    public synchronized void requireOutsideHook() {
        context.requireOutsideHook();
    }

    public synchronized List<ReceiptObservation> receiptObservations() {
        requireOutsideHook();
        return context.observations();
    }

    @Override
    public synchronized ProtocolResult begin(CrossShardTransfer transfer, int timeoutRounds) {
        requireOutsideHook();
        Objects.requireNonNull(transfer, "La transferencia es obligatoria.");
        if (timeoutRounds <= 0) {
            throw new IllegalArgumentException("El timeout debe ser positivo.");
        }
        if (context.hasSession(transfer.id())) {
            throw new IllegalStateException("Ya existe una sesion para esta transferencia.");
        }
        Shard source = context.shard(transfer.sourceShardId());
        context.shard(transfer.targetShardId());
        validateSourceTransfer(transfer, source);
        int logicalTime = context.logicalTime();
        int approvals = source.approvingValidators();
        Receipt receipt = createReceipt(transfer);
        CrossShardSession session = new CrossShardSession(transfer, receipt, logicalTime,
                Math.addExact(logicalTime, timeoutRounds), approvals, source.getValidators().size());

        if (approvals < context.quorum()) {
            session.markFailedValidation("El shard origen no alcanzo quorum para bloquear el UTXO.",
                    logicalTime);
            context.putSession(session);
            return new OperationResult(transfer.id(), false, session.status(), session.reason());
        }

        boolean locked = false;
        try {
            locked = source.lockUtxo(transfer.sourceUtxo().key());
            if (!locked) {
                throw new ProtocolException(transfer.id(),
                        "El UTXO origen ya esta bloqueado por otra transferencia.");
            }
            session.markSourceLocked(logicalTime);
            session.markReceiptCreated(logicalTime);
            context.observe(receipt.receiptId(), ISSUED);
            context.putSession(session);
            return new OperationResult(transfer.id(), true, session.status(), session.reason());
        } catch (RuntimeException error) {
            if (locked) {
                source.unlockUtxo(transfer.sourceUtxo().key());
            }
            if (error instanceof ProtocolException protocolError) {
                throw protocolError;
            }
            throw new ProtocolException(transfer.id(),
                    "No se pudo iniciar la transferencia cross-shard.", true, error);
        }
    }

    @Override
    public synchronized ProtocolResult deliverReceipt(String transferId) {
        requireOutsideHook();
        CrossShardSession session = context.session(transferId);
        if (session == null) {
            return missingResult(transferId);
        }
        if (session.isTerminal()) {
            return new OperationResult(transferId, false, session.status(),
                    "La sesion ya tiene una decision terminal.");
        }
        if (context.logicalTime() > session.timeoutRound()) {
            AbortResult timeoutResult = timeout(transferId);
            return new OperationResult(transferId, false, timeoutResult.status(), timeoutResult.message());
        }
        if (session.status() != CrossShardStatus.RECEIPT_CREATED) {
            return new OperationResult(transferId, false, session.status(),
                    "La sesion no esta lista para entregar el recibo.");
        }
        session.markReceiptDelivered(context.logicalTime());
        context.observe(session.receipt().receiptId(), DELIVERED);
        return new OperationResult(transferId, true, session.status(), session.reason());
    }

    @Override
    public synchronized CommitResult commit(String transferId) {
        requireOutsideHook();
        CrossShardSession session = context.session(transferId);
        if (session == null) {
            return new CommitResult(normalizeTransferId(transferId), false,
                    CrossShardStatus.FAILED_VALIDATION, "La sesion no existe.", false);
        }
        if (session.isTerminal()) {
            context.observe(session.receipt().receiptId(), REJECTED);
            return new CommitResult(transferId, false, session.status(),
                    "La sesion ya tiene una decision terminal.", false);
        }
        if (context.logicalTime() > session.timeoutRound()) {
            AbortResult timeoutResult = timeout(transferId);
            return new CommitResult(transferId, false, timeoutResult.status(),
                    timeoutResult.message(), false);
        }
        if (session.status() == CrossShardStatus.RECEIPT_CREATED) {
            ProtocolResult delivery = deliverReceipt(transferId);
            if (!delivery.success()) {
                return new CommitResult(transferId, false, delivery.status(), delivery.message(), false);
            }
        }
        if (session.status() != CrossShardStatus.RECEIPT_DELIVERED) {
            return new CommitResult(transferId, false, session.status(),
                    "La sesion no esta lista para preparar el destino.", false);
        }

        Shard target = context.shard(session.transfer().targetShardId());
        int approvals = target.approvingValidators();
        if (approvals < context.quorum()) {
            failValidationAndRelease(session,
                    "El shard destino no alcanzo quorum para consumir el recibo.");
            return new CommitResult(transferId, false, session.status(), session.reason(), false);
        }
        if (target.getConsumedReceipts().contains(session.receipt().receiptId())) {
            context.observe(session.receipt().receiptId(), REJECTED);
            failValidationAndRelease(session,
                    "El recibo ya habia sido consumido en el shard destino.");
            return new CommitResult(transferId, false, session.status(), session.reason(), false);
        }

        CommitPlan plan = prepareCommit(session);
        String receiptId = session.receipt().receiptId();
        context.observe(receiptId, ATTEMPT);
        try {
            applyCommit(plan);
            context.observe(receiptId, CONFIRMED);
            return new CommitResult(transferId, true, session.status(), session.reason(), false);
        } catch (RuntimeException error) {
            try {
                rollback(plan);
                context.observe(receiptId, ROLLED_BACK);
            } catch (RuntimeException rollbackError) {
                error.addSuppressed(rollbackError);
                throw new ProtocolException(transferId,
                        "El commit fallo y el rollback no pudo completarse.", false, error);
            }
            throw new ProtocolException(transferId,
                    "El commit fallo y el estado previo fue restaurado.", true, error);
        }
    }

    private CommitPlan prepareCommit(CrossShardSession session) {
        Objects.requireNonNull(session, "La sesion es obligatoria.");
        if (session.status() != CrossShardStatus.RECEIPT_DELIVERED) {
            throw new ProtocolException(session.transfer().id(),
                    "La sesion debe estar en RECEIPT_DELIVERED para preparar el commit.");
        }
        Shard source = context.shard(session.transfer().sourceShardId());
        Shard target = context.shard(session.transfer().targetShardId());
        UTXO sourceUtxo = session.transfer().sourceUtxo();
        Transaction.Output sourceOutput = source.getUtxoPool().getOutput(sourceUtxo);
        if (sourceOutput == null || !source.getUtxoPool().contains(sourceUtxo)) {
            throw new ProtocolException(session.transfer().id(),
                    "El UTXO origen no esta disponible para el commit.");
        }
        if (!source.isLocked(sourceUtxo.key())) {
            throw new ProtocolException(session.transfer().id(),
                    "El UTXO origen debe permanecer bloqueado antes del commit.");
        }

        UTXO targetUtxo = createTargetUtxo(session.receipt());
        Transaction.Output targetOutput = new Transaction.Output(session.receipt().amount(),
                session.receipt().recipient());
        validateSourceTransfer(session.transfer(), source);
        long change = Math.subtractExact(sourceOutput.getValue(), session.transfer().amount());
        UTXO changeUtxo = change > 0 ? createChangeUtxo(session.receipt()) : null;
        Transaction.Output changeOutput = change > 0
                ? new Transaction.Output(change, sourceOutput.getRecipient()) : null;

        if (target.getUtxoPool().contains(targetUtxo)) {
            throw new ProtocolException(session.transfer().id(),
                    "El UTXO destino ya existe antes del commit.");
        }
        if (changeUtxo != null && source.getUtxoPool().contains(changeUtxo)) {
            throw new ProtocolException(session.transfer().id(),
                    "El UTXO de cambio ya existe antes del commit.");
        }

        LedgerSnapshot snapshot = new LedgerSnapshot(
                source.getUtxoPool().contains(sourceUtxo),
                sourceOutput,
                source.isLocked(sourceUtxo.key()),
                target.getConsumedReceipts().contains(session.receipt().receiptId()),
                changeUtxo != null && source.getUtxoPool().contains(changeUtxo),
                changeUtxo == null ? null : source.getUtxoPool().getOutput(changeUtxo),
                target.getUtxoPool().contains(targetUtxo),
                target.getUtxoPool().getOutput(targetUtxo),
                session.checkpoint()
        );

        return new CommitPlan(session, source, target, sourceUtxo, sourceOutput,
                changeUtxo, changeOutput, targetUtxo, targetOutput,
                target.approvingValidators(), target.getValidators().size(), snapshot);
    }

    private void applyCommit(CommitPlan plan) {
        Objects.requireNonNull(plan, "El plan de commit es obligatorio.");
        CrossShardSession session = plan.session();
        session.markDestinationPrepared(plan.targetApprovals(), plan.targetValidators(),
                context.logicalTime());
        if (!plan.target().markReceiptConsumed(session.receipt().receiptId())) {
            throw new ProtocolException(session.transfer().id(),
                    "El recibo fue consumido durante la aplicacion del commit.");
        }
        context.checkFailure(ProtocolContext.FailurePoint.AFTER_RECEIPT_CONSUMED);

        context.debit(session.receipt().receiptId(), plan.source(), plan.sourceUtxo());
        plan.source().unlockUtxo(plan.sourceUtxo().key());
        if (plan.changeUtxo() != null) {
            plan.source().getUtxoPool().addUTXO(plan.changeUtxo(), plan.changeOutput());
        }
        context.checkFailure(ProtocolContext.FailurePoint.AFTER_SOURCE_DEBIT);

        context.checkFailure(ProtocolContext.FailurePoint.DURING_TARGET_CREDIT);
        context.credit(session.receipt().receiptId(), plan.target(), plan.targetUtxo(), plan.targetOutput());
        context.checkFailure(ProtocolContext.FailurePoint.AFTER_TARGET_CREDIT);

        session.markCommitted(plan.targetApprovals(), plan.targetValidators(), context.logicalTime());
    }

    private void rollback(CommitPlan plan) {
        Objects.requireNonNull(plan, "El plan de commit es obligatorio.");
        LedgerSnapshot snapshot = plan.snapshot();

        restoreUtxo(plan.target(), plan.targetUtxo(), snapshot.targetUtxoPresent(),
                snapshot.targetOutput());
        if (plan.changeUtxo() != null) {
            restoreUtxo(plan.source(), plan.changeUtxo(), snapshot.changeUtxoPresent(),
                    snapshot.changeOutput());
        }
        restoreUtxo(plan.source(), plan.sourceUtxo(), snapshot.sourceUtxoPresent(),
                snapshot.sourceOutput());

        if (snapshot.receiptConsumed()) {
            plan.target().markReceiptConsumed(plan.session().receipt().receiptId());
        } else {
            plan.target().unmarkReceiptConsumed(plan.session().receipt().receiptId());
        }
        if (snapshot.sourceLocked()) {
            plan.source().lockUtxo(plan.sourceUtxo().key());
        } else {
            plan.source().unlockUtxo(plan.sourceUtxo().key());
        }
        plan.session().restore(snapshot.sessionCheckpoint());
        if (plan.source().getUtxoPool().getOutput(plan.sourceUtxo()) != snapshot.sourceOutput()
                || plan.target().getUtxoPool().getOutput(plan.targetUtxo()) != snapshot.targetOutput()
                || (plan.changeUtxo() != null
                    && plan.source().getUtxoPool().getOutput(plan.changeUtxo()) != snapshot.changeOutput())
                || plan.source().isLocked(plan.sourceUtxo().key()) != snapshot.sourceLocked()
                || plan.target().getConsumedReceipts().contains(plan.session().receipt().receiptId())
                    != snapshot.receiptConsumed()) {
            throw new IllegalStateException("El rollback no restauro el ledger previo.");
        }
    }

    @Override
    public synchronized AbortResult abort(String transferId, String reason) {
        requireOutsideHook();
        CrossShardSession session = context.session(transferId);
        if (session == null) {
            return new AbortResult(normalizeTransferId(transferId), false,
                    CrossShardStatus.FAILED_VALIDATION, "La sesion no existe.", false);
        }
        if (session.isTerminal()) {
            return new AbortResult(transferId, false, session.status(),
                    "La sesion ya tiene una decision terminal.", false);
        }
        CrossShardSession.SessionCheckpoint checkpoint = session.checkpoint();
        Shard source = context.shard(session.transfer().sourceShardId());
        boolean locked = source.isLocked(session.transfer().sourceUtxo().key());
        try {
            source.unlockUtxo(session.transfer().sourceUtxo().key());
            session.markAborted(reason == null || reason.isBlank()
                    ? "Transferencia abortada manualmente." : reason, context.logicalTime());
            return new AbortResult(transferId, true, session.status(), session.reason(), locked);
        } catch (RuntimeException error) {
            restoreLock(source, session.transfer().sourceUtxo().key(), locked);
            session.restore(checkpoint);
            throw new ProtocolException(transferId,
                    "El abort fallo y el estado previo fue restaurado.", true, error);
        }
    }

    @Override
    public synchronized AbortResult timeout(String transferId) {
        requireOutsideHook();
        CrossShardSession session = context.session(transferId);
        if (session == null) {
            return new AbortResult(normalizeTransferId(transferId), false,
                    CrossShardStatus.FAILED_VALIDATION, "La sesion no existe.", false);
        }
        if (session.isTerminal()) {
            return new AbortResult(transferId, false, session.status(),
                    "La sesion ya tiene una decision terminal.", false);
        }
        if (context.logicalTime() < session.timeoutRound()) {
            return new AbortResult(transferId, false, session.status(),
                    "La sesion todavia no alcanzo su timeout.", false);
        }
        CrossShardSession.SessionCheckpoint checkpoint = session.checkpoint();
        Shard source = context.shard(session.transfer().sourceShardId());
        boolean locked = source.isLocked(session.transfer().sourceUtxo().key());
        try {
            source.unlockUtxo(session.transfer().sourceUtxo().key());
            session.markTimedOut("La transferencia vencio antes de ser confirmada por el shard destino.",
                    context.logicalTime());
            return new AbortResult(transferId, true, session.status(), session.reason(), locked);
        } catch (RuntimeException error) {
            restoreLock(source, session.transfer().sourceUtxo().key(), locked);
            session.restore(checkpoint);
            throw new ProtocolException(transferId,
                    "El timeout fallo y el estado previo fue restaurado.", true, error);
        }
    }

    @Override
    public synchronized Receipt lockAndCreateReceipt(CrossShardTransfer transfer) {
        requireOutsideHook();
        ProtocolResult result = begin(transfer, Integer.MAX_VALUE - context.logicalTime());
        if (!result.success()) {
            throw new ProtocolException(transfer.id(), result.message());
        }
        return context.session(transfer.id()).receipt();
    }

    @Override
    public synchronized boolean commitReceipt(Receipt receipt) {
        requireOutsideHook();
        Objects.requireNonNull(receipt, "El recibo es obligatorio.");
        CrossShardSession session = context.session(receipt.transferId());
        if (session == null || !session.receipt().equals(receipt)) {
            context.observe(session == null ? receipt.receiptId() : session.receipt().receiptId(), REJECTED);
            return false;
        }
        return commit(session.transfer().id()).success();
    }

    private void failValidationAndRelease(CrossShardSession session, String reason) {
        CrossShardSession.SessionCheckpoint checkpoint = session.checkpoint();
        Shard source = context.shard(session.transfer().sourceShardId());
        boolean locked = source.isLocked(session.transfer().sourceUtxo().key());
        try {
            source.unlockUtxo(session.transfer().sourceUtxo().key());
            session.markFailedValidation(reason, context.logicalTime());
        } catch (RuntimeException error) {
            restoreLock(source, session.transfer().sourceUtxo().key(), locked);
            session.restore(checkpoint);
            throw new ProtocolException(session.transfer().id(),
                    "El fallo de validacion no pudo aplicarse de forma atomica.", true, error);
        }
    }

    private void validateSourceTransfer(CrossShardTransfer transfer, Shard source) {
        if (transfer.sourceShardId() == transfer.targetShardId()) {
            throw new IllegalArgumentException("Una transferencia cross-shard debe mover valor entre shards distintos.");
        }
        if (!source.getUtxoPool().contains(transfer.sourceUtxo())) {
            throw new IllegalArgumentException("El UTXO origen no existe en el shard indicado.");
        }
        Transaction.Output output = source.getUtxoPool().getOutput(transfer.sourceUtxo());
        if (transfer.amount() <= 0) {
            throw new IllegalArgumentException("El monto cross-shard debe ser positivo.");
        }
        if (!transfer.isAuthorizedBy(output.getRecipient())) {
            throw new IllegalArgumentException("La firma del propietario del UTXO es obligatoria.");
        }
        if (transfer.amount() > output.getValue()) {
            throw new IllegalArgumentException("El monto cross-shard excede el valor disponible.");
        }
    }

    private Receipt createReceipt(CrossShardTransfer transfer) {
        String receiptId = Hashing.hex(Hashing.sha256(
                ("receipt:" + transfer.id()).getBytes(StandardCharsets.UTF_8)));
        return new Receipt(receiptId, transfer.id(), transfer.sourceShardId(), transfer.targetShardId(),
                transfer.sourceUtxo().key(), transfer.amount(), transfer.recipient());
    }

    private UTXO createTargetUtxo(Receipt receipt) {
        return new UTXO(Hashing.sha256(
                ("atomic-cross-shard:" + receipt.receiptId()).getBytes(StandardCharsets.UTF_8)), 0);
    }

    private UTXO createChangeUtxo(Receipt receipt) {
        return new UTXO(Hashing.sha256(
                ("cross-shard-change:" + receipt.receiptId()).getBytes(StandardCharsets.UTF_8)), 0);
    }

    private static void restoreUtxo(Shard shard, UTXO utxo, boolean present,
                                    Transaction.Output output) {
        if (present) {
            shard.getUtxoPool().addUTXO(utxo, output);
        } else {
            shard.getUtxoPool().removeUTXO(utxo);
        }
    }

    private static void restoreLock(Shard source, String utxoKey, boolean locked) {
        if (locked) {
            source.lockUtxo(utxoKey);
        } else {
            source.unlockUtxo(utxoKey);
        }
    }

    private static OperationResult missingResult(String transferId) {
        return new OperationResult(normalizeTransferId(transferId), false,
                CrossShardStatus.FAILED_VALIDATION, "La sesion no existe.");
    }

    private static String normalizeTransferId(String transferId) {
        return transferId == null || transferId.isBlank() ? "sesion-desconocida" : transferId;
    }
}
