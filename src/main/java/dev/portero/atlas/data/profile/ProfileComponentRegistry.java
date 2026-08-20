package dev.portero.atlas.data.profile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class ProfileComponentRegistry {

    private final Map<String, Supplier<ProfileComponent>> factories = new ConcurrentHashMap<>();

    public void register(String key, Supplier<ProfileComponent> factory) {
        if (this.factories.putIfAbsent(key, factory) != null) {
            throw new IllegalStateException("Profile component already registered: " + key);
        }
    }

    public ProfileComponent create(String key, String payload) {
        Supplier<ProfileComponent> factory = this.factories.get(key);
        if (factory == null) {
            return new RawProfileComponent(key, payload);
        }

        ProfileComponent component = factory.get();
        component.deserialize(payload);
        return component;
    }
}
