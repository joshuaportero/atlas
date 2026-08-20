package dev.portero.atlas.stat;

import dev.portero.atlas.pipeline.PipelineStage;

public final class CacheStatStage implements PipelineStage<StatContext> {

    @Override
    public String id() {
        return "stat.cache";
    }

    @Override
    public int priority() {
        return 30;
    }

    @Override
    public void process(StatContext context) {
        context.player().stats(new StatSnapshot(context.results()));
    }
}
