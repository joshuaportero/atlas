package dev.portero.atlas.stat;

import dev.portero.atlas.pipeline.PipelineStage;

public final class CollectBaseStage implements PipelineStage<StatContext> {

    private final StatRegistry registry;

    public CollectBaseStage(StatRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String id() {
        return "stat.base";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public void process(StatContext context) {
        StatComponent component = context.player().profile()
                .component(StatComponent.class)
                .orElseGet(StatComponent::new);

        for (StatType type : this.registry.values()) {
            context.bases().put(type.id(), component.bases().getOrDefault(type.id(), type.fallback()));
        }
    }
}
