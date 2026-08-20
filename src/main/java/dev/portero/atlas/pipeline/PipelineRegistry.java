package dev.portero.atlas.pipeline;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PipelineRegistry {

    private final Map<String, Pipeline<?>> pipelines = new ConcurrentHashMap<>();

    public <C extends PipelineContext> Pipeline<C> register(String id, Class<C> type) {
        Pipeline<C> pipeline = new Pipeline<>(id, type);
        if (this.pipelines.putIfAbsent(id, pipeline) != null) {
            throw new IllegalStateException("Pipeline already registered: " + id);
        }
        return pipeline;
    }

    @SuppressWarnings("unchecked")
    public <C extends PipelineContext> Optional<Pipeline<C>> find(String id, Class<C> type) {
        Pipeline<?> pipeline = this.pipelines.get(id);
        if (pipeline == null) {
            return Optional.empty();
        }
        if (!pipeline.type().equals(type)) {
            throw new IllegalStateException("Pipeline " + id + " is bound to "
                    + pipeline.type().getName() + " not " + type.getName());
        }
        return Optional.of((Pipeline<C>) pipeline);
    }

    public <C extends PipelineContext> Pipeline<C> require(String id, Class<C> type) {
        return this.find(id, type).orElseThrow(() -> new IllegalStateException(
                "Missing required pipeline: " + id));
    }
}
