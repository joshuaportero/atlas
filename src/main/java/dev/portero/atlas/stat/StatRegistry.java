package dev.portero.atlas.stat;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class StatRegistry {

    private final Map<String, StatType> types = new ConcurrentHashMap<>();

    public void register(StatType type) {
        if (this.types.putIfAbsent(type.id(), type) != null) {
            throw new IllegalStateException("Stat already registered: " + type.id());
        }
    }

    public Optional<StatType> find(String id) {
        return Optional.ofNullable(this.types.get(id));
    }

    public StatType require(String id) {
        return this.find(id).orElseThrow(() -> new IllegalStateException("Unknown stat: " + id));
    }

    public Collection<StatType> values() {
        return this.types.values();
    }
}
