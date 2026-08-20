package dev.portero.atlas.bootstrap;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ServiceRegistry {

    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, T instance) {
        if (this.services.putIfAbsent(type, instance) != null) {
            throw new IllegalStateException("Service already registered: " + type.getName());
        }
    }

    public <T> Optional<T> find(Class<T> type) {
        return Optional.ofNullable(type.cast(this.services.get(type)));
    }

    public <T> T require(Class<T> type) {
        return this.find(type).orElseThrow(() -> new IllegalStateException(
                "Missing required service: " + type.getName()));
    }

    public void clear() {
        this.services.clear();
    }
}
