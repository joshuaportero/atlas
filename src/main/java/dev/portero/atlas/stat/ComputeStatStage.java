package dev.portero.atlas.stat;

import dev.portero.atlas.pipeline.PipelineStage;

public final class ComputeStatStage implements PipelineStage<StatContext> {

    private final StatRegistry registry;

    public ComputeStatStage(StatRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String id() {
        return "stat.compute";
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public void process(StatContext context) {
        for (StatType type : this.registry.values()) {
            double base = context.bases().getOrDefault(type.id(), type.fallback());
            double flat = 0.0;
            double percent = 0.0;

            for (StatModifier modifier : context.modifiers()) {
                if (!modifier.stat().equals(type)) {
                    continue;
                }
                if (modifier.operation() == ModifierOperation.FLAT) {
                    flat += modifier.amount();
                } else {
                    percent += modifier.amount();
                }
            }

            context.results().put(type.id(), type.clamp((base + flat) * (1.0 + percent)));
        }
    }
}
