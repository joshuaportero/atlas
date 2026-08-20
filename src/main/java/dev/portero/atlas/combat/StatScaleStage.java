package dev.portero.atlas.combat;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.stat.StatManager;

public final class StatScaleStage implements PipelineStage<DamageContext> {

    private final StatManager stats;

    public StatScaleStage(StatManager stats) {
        this.stats = stats;
    }

    @Override
    public String id() {
        return "combat.scale";
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public void process(DamageContext context) {
        if (context.type() == DamageType.TRUE) {
            return;
        }

        AtlasPlayer attacker = context.attackerPlayer();
        if (attacker == null) {
            return;
        }

        String statId = context.type() == DamageType.MAGIC ? "magic_power" : "strength";
        double scaling = this.stats.value(attacker, statId);
        double attackDamage = this.stats.value(attacker, "attack_damage");
        context.currentDamage((context.currentDamage() + attackDamage) * (1.0 + scaling / 100.0));
    }
}
