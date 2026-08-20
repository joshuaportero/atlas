package dev.portero.atlas.combat;

import dev.portero.atlas.pipeline.PipelineStage;

public final class BaseDamageStage implements PipelineStage<DamageContext> {

    @Override
    public String id() {
        return "combat.base";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public void process(DamageContext context) {
        context.currentDamage(context.rawDamage());
    }
}
