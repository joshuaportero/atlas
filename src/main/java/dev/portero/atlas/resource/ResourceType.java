package dev.portero.atlas.resource;

import java.util.Objects;

public final class ResourceType {

    private final String id;
    private final String displayName;
    private final String maxStatId;
    private volatile double regenPerSecond;

    public ResourceType(String id, String displayName, String maxStatId, double regenPerSecond) {
        this.id = id;
        this.displayName = displayName;
        this.maxStatId = maxStatId;
        this.regenPerSecond = regenPerSecond;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public String maxStatId() {
        return this.maxStatId;
    }

    public double regenPerSecond() {
        return this.regenPerSecond;
    }

    public void regenPerSecond(double regenPerSecond) {
        this.regenPerSecond = Math.max(0.0, regenPerSecond);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResourceType type)) {
            return false;
        }
        return this.id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
