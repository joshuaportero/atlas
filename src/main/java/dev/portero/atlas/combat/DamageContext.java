package dev.portero.atlas.combat;

import dev.portero.atlas.pipeline.PipelineContext;
import dev.portero.atlas.player.AtlasPlayer;
import org.bukkit.entity.Entity;

public final class DamageContext extends PipelineContext {

    private final Entity attacker;
    private final Entity victim;
    private final AtlasPlayer attackerPlayer;
    private final AtlasPlayer victimPlayer;
    private final DamageType type;
    private final double rawDamage;
    private double currentDamage;
    private boolean critical;

    public DamageContext(Entity attacker, Entity victim, AtlasPlayer attackerPlayer,
                         AtlasPlayer victimPlayer, DamageType type, double rawDamage) {
        this.attacker = attacker;
        this.victim = victim;
        this.attackerPlayer = attackerPlayer;
        this.victimPlayer = victimPlayer;
        this.type = type;
        this.rawDamage = rawDamage;
        this.currentDamage = rawDamage;
    }

    public Entity attacker() {
        return this.attacker;
    }

    public Entity victim() {
        return this.victim;
    }

    public AtlasPlayer attackerPlayer() {
        return this.attackerPlayer;
    }

    public AtlasPlayer victimPlayer() {
        return this.victimPlayer;
    }

    public DamageType type() {
        return this.type;
    }

    public double rawDamage() {
        return this.rawDamage;
    }

    public double currentDamage() {
        return this.currentDamage;
    }

    public void currentDamage(double currentDamage) {
        this.currentDamage = Math.max(0.0, currentDamage);
    }

    public boolean critical() {
        return this.critical;
    }

    public void critical(boolean critical) {
        this.critical = critical;
    }
}
