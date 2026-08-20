package dev.portero.atlas.pipeline;

public interface PipelineStage<C extends PipelineContext> {

    String id();

    default int priority() {
        return 0;
    }

    void process(C context);
}
