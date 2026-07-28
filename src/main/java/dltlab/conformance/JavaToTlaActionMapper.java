package dltlab.conformance;

import dltlab.sharding.protocol.ProtocolAction;
import dltlab.trace.TraceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Proyecta eventos Java en acciones TLA+ o expansiones documentadas. */
public final class JavaToTlaActionMapper {
    public List<AbstractAction> map(TraceEvent event) {
        Objects.requireNonNull(
                event,
                "El evento concreto es obligatorio."
        );

        if (!event.isProtocolTransition()) {
            return List.of(stutter(
                    event,
                    "La observación del simulador no modifica el estado abstracto."
            ));
        }

        ProtocolAction action = Objects.requireNonNull(
                event.javaAction(),
                "La transición concreta debe declarar su acción Java."
        );

        return switch (action) {
            case CREATE_SESSION -> List.of(stutter(
                    event,
                    "La creación concreta ya está representada por el estado inicial Pending."
            ));
            case LOCK_SOURCE -> List.of(new AbstractAction(
                    event.step(),
                    0,
                    AbstractAction.Kind.LOCK_TRANSFER,
                    event.transferId(),
                    null,
                    null,
                    false,
                    "El bloqueo del origen inicia la transición abstracta LockTransfer."
            ));
            case CREATE_RECEIPT -> List.of(stutter(
                    event,
                    "La creación concreta del recibo está agrupada en LockTransfer."
            ));
            case DELIVER_RECEIPT -> List.of(stutter(
                    event,
                    "La entrega concreta no cambia el estado abstracto antes del consumo."
            ));
            case PREPARE_DESTINATION -> mapPreparation(event);
            case COMMIT_DESTINATION -> List.of(new AbstractAction(
                    event.step(),
                    0,
                    AbstractAction.Kind.COMMIT_TRANSFER,
                    event.transferId(),
                    null,
                    null,
                    false,
                    "El commit concreto se proyecta como CommitTransfer."
            ));
            case ABORT_TRANSFER, EXPIRE_TRANSFER, FAIL_VALIDATION ->
                    List.of(new AbstractAction(
                            event.step(),
                            0,
                            AbstractAction.Kind.TIMEOUT_TRANSFER,
                            event.transferId(),
                            null,
                            null,
                            false,
                            "La decisión terminal sin commit se proyecta por sus efectos como TimeoutTransfer."
                    ));
        };
    }

    private static List<AbstractAction> mapPreparation(TraceEvent event) {
        Integer approvals = event.targetApprovals();
        Integer validators = event.targetValidators();
        if (approvals == null || validators == null) {
            throw new IllegalArgumentException(
                    "La preparación del destino debe conservar su evidencia de votos."
            );
        }

        List<AbstractAction> actions = new ArrayList<>();
        actions.add(new AbstractAction(
                event.step(),
                0,
                AbstractAction.Kind.CONSUME_RECEIPT,
                event.transferId(),
                1,
                null,
                false,
                "La preparación concreta consume la copia canónica del recibo."
        ));

        for (int index = 0; index < approvals; index++) {
            actions.add(new AbstractAction(
                    event.step(),
                    index + 1,
                    AbstractAction.Kind.CAST_VOTE,
                    event.transferId(),
                    null,
                    "v" + (index + 1),
                    true,
                    "El conteo concreto de aprobaciones se expande en votos canónicos."
            ));
        }
        return List.copyOf(actions);
    }

    private static AbstractAction stutter(
            TraceEvent event,
            String rationale
    ) {
        return new AbstractAction(
                event.step(),
                0,
                AbstractAction.Kind.STUTTER,
                event.transferId(),
                null,
                null,
                false,
                rationale
        );
    }
}
