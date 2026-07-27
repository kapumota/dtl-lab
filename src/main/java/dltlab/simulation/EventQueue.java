package dltlab.simulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Cola estable de eventos sin threads ni dependencias del reloj del sistema. */
public final class EventQueue {
    private static final Comparator<ScheduledEvent> ORDER = Comparator
            .comparingInt(ScheduledEvent::round)
            .thenComparingInt(ScheduledEvent::priority)
            .thenComparingLong(ScheduledEvent::sequence);

    private final PriorityQueue<ScheduledEvent> events = new PriorityQueue<>(ORDER);

    public void add(ScheduledEvent event) {
        events.add(event);
    }

    public ScheduledEvent poll() {
        return events.poll();
    }

    public ScheduledEvent peek() {
        return events.peek();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public int size() {
        return events.size();
    }

    public List<ScheduledEvent> snapshot() {
        List<ScheduledEvent> copy = new ArrayList<>(events);
        copy.sort(ORDER);
        return List.copyOf(copy);
    }
}
