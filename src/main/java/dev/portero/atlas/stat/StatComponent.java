package dev.portero.atlas.stat;

import dev.portero.atlas.data.profile.ProfileComponent;
import dev.portero.atlas.util.PayloadCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StatComponent implements ProfileComponent {

    public static final String key = "stats";

    private final Map<String, Double> bases = new ConcurrentHashMap<>();

    @Override
    public String key() {
        return key;
    }

    @Override
    public String serialize() {
        return PayloadCodec.writeDoubles(this.bases);
    }

    @Override
    public void deserialize(String payload) {
        this.bases.clear();
        this.bases.putAll(PayloadCodec.readDoubles(payload));
    }

    public Map<String, Double> bases() {
        return this.bases;
    }

    public double get(StatType type) {
        return this.bases.getOrDefault(type.id(), type.fallback());
    }

    public void set(StatType type, double value) {
        this.bases.put(type.id(), type.clamp(value));
    }
}
