package dev.portero.atlas.worldevent;

import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.player.SettingsComponent;
import dev.portero.atlas.scheduler.AtlasScheduler;
import dev.portero.atlas.stat.StatManager;
import dev.portero.atlas.stat.StatModifier;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class WorldEventService {

    private final Map<String, WorldEventDefinition> definitions = new ConcurrentHashMap<>();
    private final Map<String, ActiveWorldEvent> active = new ConcurrentHashMap<>();
    private final ProfileManager profiles;
    private final StatManager stats;
    private final AtlasScheduler scheduler;

    public WorldEventService(ProfileManager profiles, StatManager stats, AtlasScheduler scheduler) {
        this.profiles = profiles;
        this.stats = stats;
        this.scheduler = scheduler;
    }

    public void register(WorldEventDefinition definition) {
        this.definitions.put(definition.id(), definition);
    }

    public Collection<WorldEventDefinition> definitions() {
        return this.definitions.values();
    }

    public Collection<ActiveWorldEvent> active() {
        this.active.values().removeIf(ActiveWorldEvent::expired);
        return this.active.values();
    }

    public Optional<ActiveWorldEvent> active(String id) {
        ActiveWorldEvent event = this.active.get(id);
        if (event != null && event.expired()) {
            this.stop(id);
            return Optional.empty();
        }
        return Optional.ofNullable(event);
    }

    public boolean start(String id) {
        WorldEventDefinition definition = this.definitions.get(id);
        if (definition == null || this.active.containsKey(id)) {
            return false;
        }

        long expiresAt = System.currentTimeMillis() + definition.defaultDurationMillis();
        this.active.put(id, new ActiveWorldEvent(definition, expiresAt));
        this.profiles.online().forEach(player -> this.apply(player, definition));
        long delayTicks = Math.max(20L, TimeUnit.MILLISECONDS.toSeconds(definition.defaultDurationMillis()) * 20L);
        this.scheduler.syncLater(delayTicks, () -> this.stop(id));
        log.info("Started world event {}", id);
        return true;
    }

    public boolean stop(String id) {
        ActiveWorldEvent removed = this.active.remove(id);
        if (removed == null) {
            return false;
        }
        this.profiles.online().forEach(player -> this.stats.removeModifiers(player, "event:" + id));
        log.info("Stopped world event {}", id);
        return true;
    }

    public void applyActive(AtlasPlayer player) {
        if (!this.optedIn(player)) {
            return;
        }
        for (ActiveWorldEvent event : this.active()) {
            this.apply(player, event.definition());
        }
    }

    public void setOptIn(AtlasPlayer player, boolean optIn) {
        SettingsComponent settings = player.profile().component(SettingsComponent.class)
                .orElseGet(SettingsComponent::new);
        settings.eventOptIn(optIn);
        player.profile().attach(settings);
        if (optIn) {
            this.applyActive(player);
            return;
        }
        this.active().forEach(event -> this.stats.removeModifiers(player, "event:" + event.definition().id()));
    }

    private void apply(AtlasPlayer player, WorldEventDefinition definition) {
        if (!this.optedIn(player)) {
            return;
        }
        this.stats.removeModifiers(player, "event:" + definition.id());
        for (StatModifier modifier : definition.modifiers()) {
            this.stats.addModifier(player, new StatModifier(
                    "event:" + definition.id(), modifier.stat(), modifier.operation(), modifier.amount()));
        }
    }

    private boolean optedIn(AtlasPlayer player) {
        return player.profile().component(SettingsComponent.class)
                .map(SettingsComponent::eventOptIn)
                .orElse(true);
    }
}
