package dev.portero.atlas.pipeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Pipeline<C extends PipelineContext> {

    private final String id;
    private final Class<C> type;
    private final List<PipelineStage<C>> stages = new ArrayList<>();

    public Pipeline(String id, Class<C> type) {
        this.id = id;
        this.type = type;
    }

    public String id() {
        return this.id;
    }

    public Class<C> type() {
        return this.type;
    }

    public Pipeline<C> add(PipelineStage<C> stage) {
        this.stages.add(stage);
        this.stages.sort(Comparator.comparingInt(PipelineStage::priority));
        return this;
    }

    public C execute(C context) {
        for (PipelineStage<C> stage : this.stages) {
            if (context.cancelled()) {
                break;
            }
            stage.process(context);
        }
        return context;
    }

    public List<PipelineStage<C>> stages() {
        return List.copyOf(this.stages);
    }
}
