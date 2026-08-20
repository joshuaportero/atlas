package dev.portero.atlas.resource;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ResourceRegistry {

    private final Map<String, ResourceType> types = new ConcurrentHashMap<>();

    public void register(ResourceType type) {
        if (this.types.putIfAbsent(type.id(), type) != null) {
            throw new IllegalStateException("Resource already registered: " + type.id());
        }
    }

    public Optional<ResourceType> find(String id) {
        return Optional.ofNullable(this.types.get(id));
    }

    public ResourceType require(String id) {
        return this.find(id).orElseThrow(() -> new IllegalStateException("Unknown resource: " + id));
    }

    public Collection<ResourceType> values() {
        return this.types.values();
    }
}
