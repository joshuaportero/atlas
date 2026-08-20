package dev.portero.atlas.combat;

import dev.portero.atlas.event.AtlasEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AtlasDamageCalculateEvent extends Event
        implements org.bukkit.event.Cancellable, AtlasEvent, dev.portero.atlas.event.Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final DamageContext context;
    private boolean cancelled;

    public AtlasDamageCalculateEvent(DamageContext context) {
        this.context = context;
    }

    public DamageContext context() {
        return this.context;
    }

    public double damage() {
        return this.context.currentDamage();
    }

    public void damage(double damage) {
        this.context.currentDamage(damage);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        this.context.cancelled(cancelled);
    }

    @Override
    public boolean cancelled() {
        return this.cancelled;
    }

    @Override
    public void cancelled(boolean cancelled) {
        this.setCancelled(cancelled);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
