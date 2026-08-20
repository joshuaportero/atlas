package dev.portero.atlas.resource;

import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.event.StatRecalculatedEvent;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.player.SettingsComponent;
import dev.portero.atlas.scheduler.AtlasScheduler;
import dev.portero.atlas.stat.StatManager;
import org.bukkit.configuration.ConfigurationSection;

public final class ResourceManager {

    private final ResourceRegistry registry = new ResourceRegistry();
    private final ProfileManager profiles;
    private final StatManager stats;

    public ResourceManager(ProfileManager profiles, StatManager stats, EventBus events) {
        this.profiles = profiles;
        this.stats = stats;
        events.subscribe(StatRecalculatedEvent.class, event -> this.clamp(event.player()));
    }

    public ResourceRegistry registry() {
        return this.registry;
    }

    public void registerDefaults(ConfigurationSection section) {
        this.registry.register(new ResourceType(
                "mana", "Mana", "max_mana", this.regen(section, "mana", 2.0)));
        this.registry.register(new ResourceType(
                "stamina", "Stamina", "max_stamina", this.regen(section, "stamina", 5.0)));
        this.registry.register(new ResourceType(
                "energy", "Energy", "max_energy", this.regen(section, "energy", 3.0)));
    }

    public void startRegen(AtlasScheduler scheduler, long intervalTicks) {
        double seconds = intervalTicks / 20.0;
        scheduler.syncRepeat(intervalTicks, intervalTicks, () -> this.tick(seconds));
    }

    public double current(AtlasPlayer player, ResourceType type) {
        return player.resource(type.id());
    }

    public double maximum(AtlasPlayer player, ResourceType type) {
        return this.stats.value(player, type.maxStatId());
    }

    public boolean hasResource(AtlasPlayer player, String typeId, double amount) {
        return this.registry.find(typeId)
                .filter(type -> this.current(player, type) + 1.0E-6 >= amount)
                .isPresent();
    }

    public boolean hasResource(AtlasPlayer player, ResourceType type, double amount) {
        return this.current(player, type) + 1.0E-6 >= amount;
    }

    public boolean consumeResource(AtlasPlayer player, String typeId, double amount) {
        return this.registry.find(typeId)
                .filter(type -> this.consumeResource(player, type, amount))
                .isPresent();
    }

    public boolean consumeResource(AtlasPlayer player, ResourceType type, double amount) {
        if (!this.hasResource(player, type, amount)) {
            return false;
        }
        player.resource(type.id(), this.current(player, type) - amount);
        this.persist(player);
        return true;
    }

    public void restore(AtlasPlayer player, ResourceType type, double amount) {
        this.add(player, type, amount);
        this.persist(player);
    }

    public void fill(AtlasPlayer player) {
        for (ResourceType type : this.registry.values()) {
            player.resource(type.id(), this.maximum(player, type));
        }
        player.clearDirtyResources();
    }

    public void load(AtlasPlayer player, ResourceComponent component) {
        if (component.values().isEmpty()) {
            this.fill(player);
            return;
        }
        component.values().forEach(player::resource);
        this.clamp(player);
        player.clearDirtyResources();
    }

    public void persist(AtlasPlayer player) {
        if (!player.dirtyResources()) {
            return;
        }
        ResourceComponent component = player.profile().component(ResourceComponent.class)
                .orElseGet(ResourceComponent::new);
        component.values().clear();
        component.values().putAll(player.resources());
        player.profile().attach(component);
        player.clearDirtyResources();
    }

    private void add(AtlasPlayer player, ResourceType type, double amount) {
        double max = this.maximum(player, type);
        player.resource(type.id(), Math.min(max, this.current(player, type) + amount));
    }

    private void tick(double seconds) {
        for (AtlasPlayer player : this.profiles.online()) {
            if (!player.handle().isOnline()) {
                continue;
            }
            boolean regen = player.profile().component(SettingsComponent.class)
                    .map(SettingsComponent::resourceRegen)
                    .orElse(true);
            if (!regen) {
                continue;
            }
            for (ResourceType type : this.registry.values()) {
                this.add(player, type, type.regenPerSecond() * seconds);
            }
        }
    }

    private void clamp(AtlasPlayer player) {
        for (ResourceType type : this.registry.values()) {
            double max = this.maximum(player, type);
            if (this.current(player, type) > max) {
                player.resource(type.id(), max);
            }
        }
    }

    private double regen(ConfigurationSection section, String id, double fallback) {
        if (section == null) {
            return fallback;
        }
        return section.getDouble(id + ".regen-per-second", fallback);
    }
}
