package dev.portero.atlas.worldevent;

public final class ActiveWorldEvent {

    private final WorldEventDefinition definition;
    private final long expiresAt;

    public ActiveWorldEvent(WorldEventDefinition definition, long expiresAt) {
        this.definition = definition;
        this.expiresAt = expiresAt;
    }

    public WorldEventDefinition definition() {
        return this.definition;
    }

    public long expiresAt() {
        return this.expiresAt;
    }

    public boolean expired() {
        return System.currentTimeMillis() >= this.expiresAt;
    }

    public long remainingMillis() {
        return Math.max(0L, this.expiresAt - System.currentTimeMillis());
    }
}
