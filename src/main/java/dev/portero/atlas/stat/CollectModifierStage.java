package dev.portero.atlas.stat;

import dev.portero.atlas.pipeline.PipelineStage;

public final class CollectModifierStage implements PipelineStage<StatContext> {

    @Override
    public String id() {
        return "stat.modifiers";
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public void process(StatContext context) {
        context.modifiers().addAll(context.player().modifiers());
    }
}
