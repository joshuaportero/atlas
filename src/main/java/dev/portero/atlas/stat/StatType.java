package dev.portero.atlas.stat;

import java.util.Objects;

public final class StatType {

    private final String id;
    private final String displayName;
    private final double fallback;
    private final double minimum;
    private final double maximum;

    public StatType(String id, String displayName, double fallback, double minimum, double maximum) {
        this.id = id;
        this.displayName = displayName;
        this.fallback = fallback;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public double fallback() {
        return this.fallback;
    }

    public double minimum() {
        return this.minimum;
    }

    public double maximum() {
        return this.maximum;
    }

    public double clamp(double value) {
        return Math.max(this.minimum, Math.min(this.maximum, value));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StatType type)) {
            return false;
        }
        return this.id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
