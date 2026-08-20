package dev.portero.atlas.combat;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.player.AtlasPlayer;

public final class AbsorptionStage implements PipelineStage<DamageContext> {

    @Override
    public String id() {
        return "combat.absorption";
    }

    @Override
    public int priority() {
        return 40;
    }

    @Override
    public void process(DamageContext context) {
        if (context.type() == DamageType.TRUE) {
            return;
        }

        AtlasPlayer victim = context.victimPlayer();
        if (victim == null || victim.absorption() <= 0.0) {
            return;
        }

        double absorbed = Math.min(victim.absorption(), context.currentDamage());
        victim.absorption(victim.absorption() - absorbed);
        context.currentDamage(context.currentDamage() - absorbed);
    }
}
