package dev.portero.atlas.combat;

import dev.portero.atlas.event.AtlasEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AtlasPostDamageEvent extends Event implements AtlasEvent {

    private static final HandlerList handlers = new HandlerList();

    private final DamageContext context;

    public AtlasPostDamageEvent(DamageContext context) {
        this.context = context;
    }

    public DamageContext context() {
        return this.context;
    }

    public double damage() {
        return this.context.currentDamage();
    }

    public boolean critical() {
        return this.context.critical();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
