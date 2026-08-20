package dev.portero.atlas.combat;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.stat.StatManager;

public final class MitigationStage implements PipelineStage<DamageContext> {

    private final StatManager stats;

    public MitigationStage(StatManager stats) {
        this.stats = stats;
    }

    @Override
    public String id() {
        return "combat.mitigation";
    }

    @Override
    public int priority() {
        return 30;
    }

    @Override
    public void process(DamageContext context) {
        if (context.type() == DamageType.TRUE) {
            return;
        }

        AtlasPlayer victim = context.victimPlayer();
        if (victim == null) {
            return;
        }

        double defense = this.stats.value(victim, "defense");
        double reduction = defense / (defense + 100.0);
        if (context.type() == DamageType.MAGIC) {
            reduction *= 0.5;
        }
        context.currentDamage(context.currentDamage() * (1.0 - reduction));
    }
}
