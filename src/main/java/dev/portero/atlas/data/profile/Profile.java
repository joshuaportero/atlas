package dev.portero.atlas.data.profile;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Profile {

    private final UUID uniqueId;
    private final long createdAt;
    private final Map<String, ProfileComponent> components = new ConcurrentHashMap<>();
    private final AtomicBoolean dirty = new AtomicBoolean();
    private volatile String name;
    private volatile long updatedAt;

    public Profile(UUID uniqueId, String name, long createdAt, long updatedAt) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Profile create(UUID uniqueId, String name) {
        long now = System.currentTimeMillis();
        Profile profile = new Profile(uniqueId, name, now, now);
        profile.markDirty();
        return profile;
    }

    public UUID uniqueId() {
        return this.uniqueId;
    }

    public String name() {
        return this.name;
    }

    public void name(String name) {
        if (!this.name.equals(name)) {
            this.name = name;
            this.markDirty();
        }
    }

    public long createdAt() {
        return this.createdAt;
    }

    public long updatedAt() {
        return this.updatedAt;
    }

    public void attach(ProfileComponent component) {
        this.attach(component, true);
    }

    public void attach(ProfileComponent component, boolean markDirty) {
        this.components.put(component.key(), component);
        if (markDirty) {
            this.markDirty();
        }
    }

    public Optional<ProfileComponent> component(String key) {
        return Optional.ofNullable(this.components.get(key));
    }

    public <T extends ProfileComponent> Optional<T> component(Class<T> type) {
        return this.components.values().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }

    public Collection<ProfileComponent> components() {
        return this.components.values();
    }

    public void markDirty() {
        this.updatedAt = System.currentTimeMillis();
        this.dirty.set(true);
    }

    public boolean dirty() {
        return this.dirty.get();
    }

    public boolean clearDirty() {
        return this.dirty.getAndSet(false);
    }
}
