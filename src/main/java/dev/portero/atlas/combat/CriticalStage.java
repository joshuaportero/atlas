package dev.portero.atlas.combat;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.stat.StatManager;

import java.util.concurrent.ThreadLocalRandom;

public final class CriticalStage implements PipelineStage<DamageContext> {

    private final StatManager stats;

    public CriticalStage(StatManager stats) {
        this.stats = stats;
    }

    @Override
    public String id() {
        return "combat.critical";
    }

    @Override
    public int priority() {
        return 20;
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

        double chance = this.stats.value(attacker, "crit_chance");
        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }

        double multiplier = 1.0 + this.stats.value(attacker, "crit_damage");
        context.critical(true);
        context.currentDamage(context.currentDamage() * multiplier);
    }
}
