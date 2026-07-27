package dltlab.sharding.protocol;

import dltlab.sharding.CrossShardStatus;

import java.util.Objects;

/** Transicion permitida entre dos estados mediante una accion concreta. */
public record ProtocolTransition(
        CrossShardStatus from,
        ProtocolAction action,
        CrossShardStatus to
) {
    public ProtocolTransition {
        from = Objects.requireNonNull(from, "El estado de origen es obligatorio.");
        action = Objects.requireNonNull(action, "La accion es obligatoria.");
        to = Objects.requireNonNull(to, "El estado de destino es obligatorio.");
        if (from == to) {
            throw new IllegalArgumentException("Una transicion debe cambiar el estado de la sesion.");
        }
    }

    public boolean matches(CrossShardStatus current, ProtocolAction candidateAction,
                           CrossShardStatus next) {
        return from == current && action == candidateAction && to == next;
    }
}
