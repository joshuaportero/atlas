package dev.portero.atlas.stat;

import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.event.StatRecalculatedEvent;
import dev.portero.atlas.pipeline.Pipeline;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.player.AtlasPlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class StatManager {

    private final StatRegistry registry;
    private final Pipeline<StatContext> pipeline;
    private final EventBus events;

    public StatManager(StatRegistry registry, PipelineRegistry pipelines, EventBus events) {
        this.registry = registry;
        this.pipeline = pipelines.register("stat.recalculate", StatContext.class);
        this.events = events;

        this.pipeline.add(new CollectBaseStage(registry));
        this.pipeline.add(new CollectModifierStage());
        this.pipeline.add(new ComputeStatStage(registry));
        this.pipeline.add(new CacheStatStage());
    }

    public StatRegistry registry() {
        return this.registry;
    }

    public Pipeline<StatContext> pipeline() {
        return this.pipeline;
    }

    public void registerDefaults(ConfigurationSection section) {
        this.register("strength", "Strength", section, 0.0, 0.0, 10_000.0);
        this.register("defense", "Defense", section, 0.0, 0.0, 10_000.0);
        this.register("crit_chance", "Crit Chance", section, 0.05, 0.0, 1.0);
        this.register("crit_damage", "Crit Damage", section, 0.5, 0.0, 10.0);
        this.register("attack_damage", "Attack Damage", section, 1.0, 0.0, 10_000.0);
        this.register("magic_power", "Magic Power", section, 0.0, 0.0, 10_000.0);
        this.register("max_health", "Max Health", section, 20.0, 1.0, 2_048.0);
        this.register("max_mana", "Max Mana", section, 100.0, 0.0, 100_000.0);
        this.register("max_stamina", "Max Stamina", section, 100.0, 0.0, 100_000.0);
        this.register("max_energy", "Max Energy", section, 100.0, 0.0, 100_000.0);
    }

    public void recalculate(AtlasPlayer player) {
        StatContext context = new StatContext(player);
        this.pipeline.execute(context);
        this.applyHealth(player);
        this.events.publish(new StatRecalculatedEvent(player, player.stats()));
    }

    public double value(AtlasPlayer player, String statId) {
        return this.registry.find(statId)
                .map(type -> player.stats().get(type))
                .orElse(0.0);
    }

    public void setBase(AtlasPlayer player, StatType type, double value) {
        StatComponent component = player.profile().component(StatComponent.class)
                .orElseGet(StatComponent::new);
        component.set(type, value);
        player.profile().attach(component);
        this.recalculate(player);
    }

    public void addModifier(AtlasPlayer player, StatModifier modifier) {
        player.addModifier(modifier);
        this.recalculate(player);
    }

    public void removeModifiers(AtlasPlayer player, String source) {
        player.removeModifiers(source);
        this.recalculate(player);
    }

    private void register(String id, String displayName, ConfigurationSection section,
                          double fallback, double minimum, double maximum) {
        double configured = section != null ? section.getDouble(id, fallback) : fallback;
        this.registry.register(new StatType(id, displayName, configured, minimum, maximum));
    }

    private void applyHealth(AtlasPlayer player) {
        Player handle = player.handle();
        if (!handle.isOnline()) {
            return;
        }

        AttributeInstance attribute = handle.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) {
            return;
        }

        double maxHealth = player.stats().get(this.registry.require("max_health"));
        attribute.setBaseValue(maxHealth);
        if (handle.getHealth() > maxHealth) {
            handle.setHealth(maxHealth);
        }
    }
}
