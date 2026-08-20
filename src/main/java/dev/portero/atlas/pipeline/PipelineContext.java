package dev.portero.atlas.pipeline;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PipelineContext {

    private final Map<String, Object> metadata = new HashMap<>();
    private boolean cancelled;

    public boolean cancelled() {
        return this.cancelled;
    }

    public void cancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public <T> void set(String key, T value) {
        this.metadata.put(key, value);
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.ofNullable(this.metadata.get(key)).map(type::cast);
    }

    public void clear() {
        this.metadata.clear();
        this.cancelled = false;
    }
}
