package dev.portero.atlas.stat;

import dev.portero.atlas.data.profile.ProfileComponent;
import dev.portero.atlas.util.PayloadCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StatComponent implements ProfileComponent {

    public static final String key = "stats";

    private final Map<String, Double> bases = new ConcurrentHashMap<>();
    private int points;

    @Override
    public String key() {
        return key;
    }

    @Override
    public String serialize() {
        Map<String, Double> encoded = new ConcurrentHashMap<>(this.bases);
        encoded.put("_points", (double) this.points);
        return PayloadCodec.writeDoubles(encoded);
    }

    @Override
    public void deserialize(String payload) {
        this.bases.clear();
        Map<String, Double> decoded = PayloadCodec.readDoubles(payload);
        this.points = decoded.getOrDefault("_points", 0.0).intValue();
        decoded.remove("_points");
        this.bases.putAll(decoded);
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

    public int points() {
        return this.points;
    }

    public void points(int points) {
        this.points = Math.max(0, points);
    }
}
