package dltlab.simulation;

import java.util.Objects;
import java.util.function.IntConsumer;

/** Programa y extrae eventos mediante un orden total reproducible. */
public final class EventScheduler {
    private final SimulationClock clock;
    private final EventQueue queue;
    private final IntConsumer roundSynchronizer;
    private long nextSequence;

    public EventScheduler(SimulationClock clock, IntConsumer roundSynchronizer) {
        this.clock = Objects.requireNonNull(clock, "El reloj de simulacion es obligatorio.");
        this.queue = new EventQueue();
        this.roundSynchronizer = Objects.requireNonNull(roundSynchronizer,
                "El sincronizador de ronda es obligatorio.");
    }

    public ScheduledEvent schedule(int round, int priority, SimulationEventType type,
                                   String transferId, String description, Runnable action) {
        if (round < clock.now()) {
            throw new IllegalArgumentException("No se puede programar un evento en una ronda pasada.");
        }
        ScheduledEvent event = new ScheduledEvent(nextSequence++, round, priority, type,
                transferId, description, action);
        queue.add(event);
        return event;
    }

    public ScheduledEvent next() {
        ScheduledEvent event = queue.poll();
        if (event == null) {
            return null;
        }
        clock.advanceTo(event.round());
        roundSynchronizer.accept(clock.now());
        return event;
    }

    public boolean hasNext() {
        return !queue.isEmpty();
    }

    public int pendingEvents() {
        return queue.size();
    }

    public EventQueue queue() {
        return queue;
    }
}
