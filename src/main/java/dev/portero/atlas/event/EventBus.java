package dev.portero.atlas.event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class EventBus {

    private final Map<Class<? extends AtlasEvent>, List<Subscription<?>>> subscribers =
            new ConcurrentHashMap<>();

    public <E extends AtlasEvent> void subscribe(Class<E> type, Consumer<E> listener) {
        this.subscribe(type, EventPriority.NORMAL, true, listener);
    }

    public <E extends AtlasEvent> void subscribe(Class<E> type, EventPriority priority,
                                                 Consumer<E> listener) {
        this.subscribe(type, priority, true, listener);
    }

    public <E extends AtlasEvent> void subscribe(Class<E> type, EventPriority priority,
                                                 boolean ignoreCancelled, Consumer<E> listener) {
        List<Subscription<?>> listeners = this.subscribers.computeIfAbsent(type,
                key -> new ArrayList<>());
        listeners.add(new Subscription<>(priority, ignoreCancelled, listener));
        listeners.sort(Comparator.comparingInt(subscription -> subscription.priority.ordinal()));
    }

    public <E extends AtlasEvent> void publish(E event) {
        List<Subscription<?>> listeners = this.subscribers.get(event.getClass());
        if (listeners == null) {
            return;
        }

        for (Subscription<?> subscription : List.copyOf(listeners)) {
            this.dispatch(event, subscription);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends AtlasEvent> void dispatch(E event, Subscription<?> subscription) {
        if (event instanceof Cancellable cancellable
                && cancellable.cancelled()
                && subscription.ignoreCancelled) {
            return;
        }

        ((Subscription<E>) subscription).listener.accept(event);
    }

    private record Subscription<E extends AtlasEvent>(EventPriority priority,
                                                      boolean ignoreCancelled,
                                                      Consumer<E> listener) {
    }
}
