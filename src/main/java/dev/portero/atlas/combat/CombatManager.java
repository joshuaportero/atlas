package dev.portero.atlas.combat;

import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.pipeline.Pipeline;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.stat.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class CombatManager {

    private final ProfileManager profiles;
    private final Pipeline<DamageContext> pipeline;
    private final EventBus events;
    private final ThreadLocal<Boolean> applying = ThreadLocal.withInitial(() -> false);
    private volatile boolean enabled = true;

    public CombatManager(ProfileManager profiles, StatManager stats,
                         PipelineRegistry pipelines, EventBus events) {
        this.profiles = profiles;
        this.pipeline = pipelines.register("combat.damage", DamageContext.class);
        this.events = events;

        this.pipeline.add(new BaseDamageStage());
        this.pipeline.add(new StatScaleStage(stats));
        this.pipeline.add(new CriticalStage(stats));
        this.pipeline.add(new MitigationStage(stats));
        this.pipeline.add(new AbsorptionStage());
    }

    public Pipeline<DamageContext> pipeline() {
        return this.pipeline;
    }

    public boolean applying() {
        return Boolean.TRUE.equals(this.applying.get());
    }

    public boolean enabled() {
        return this.enabled;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DamageContext calculate(Entity attacker, Entity victim, double rawDamage, DamageType type) {
        AtlasPlayer attackerPlayer = attacker instanceof Player player
                ? this.profiles.find(player).orElse(null)
                : null;
        AtlasPlayer victimPlayer = victim instanceof Player player
                ? this.profiles.find(player).orElse(null)
                : null;

        DamageContext context = new DamageContext(
                attacker, victim, attackerPlayer, victimPlayer, type, rawDamage);
        this.pipeline.execute(context);

        AtlasDamageCalculateEvent calculate = new AtlasDamageCalculateEvent(context);
        this.events.publish(calculate);
        Bukkit.getPluginManager().callEvent(calculate);
        return context;
    }

    public void finish(DamageContext context) {
        AtlasPostDamageEvent post = new AtlasPostDamageEvent(context);
        this.events.publish(post);
        Bukkit.getPluginManager().callEvent(post);
    }

    public DamageContext attack(Entity attacker, Entity victim, double rawDamage, DamageType type) {
        DamageContext context = this.calculate(attacker, victim, rawDamage, type);
        if (context.cancelled() || context.currentDamage() <= 0.0
                || !(victim instanceof LivingEntity living) || !living.isValid()) {
            return context;
        }

        this.applying.set(true);
        try {
            living.damage(context.currentDamage(), attacker);
        } finally {
            this.applying.set(false);
        }
        this.finish(context);
        return context;
    }
}
