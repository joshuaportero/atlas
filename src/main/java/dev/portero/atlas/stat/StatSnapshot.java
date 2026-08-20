package dev.portero.atlas.stat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class StatSnapshot {

    private static final StatSnapshot empty = new StatSnapshot(Map.of());

    private final Map<String, Double> values;

    public StatSnapshot(Map<String, Double> values) {
        this.values = Collections.unmodifiableMap(new HashMap<>(values));
    }

    public static StatSnapshot empty() {
        return empty;
    }

    public double get(StatType type) {
        return this.values.getOrDefault(type.id(), type.fallback());
    }

    public double get(String id, double fallback) {
        return this.values.getOrDefault(id, fallback);
    }

    public Map<String, Double> values() {
        return this.values;
    }
}
