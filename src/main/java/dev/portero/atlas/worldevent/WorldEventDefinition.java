package dev.portero.atlas.worldevent;

import dev.portero.atlas.stat.StatModifier;
import org.bukkit.Material;

import java.util.List;

public final class WorldEventDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final Material icon;
    private final long defaultDurationMillis;
    private final List<StatModifier> modifiers;

    public WorldEventDefinition(String id, String name, String description, Material icon,
                                long defaultDurationMillis, List<StatModifier> modifiers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.defaultDurationMillis = defaultDurationMillis;
        this.modifiers = List.copyOf(modifiers);
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String description() {
        return this.description;
    }

    public Material icon() {
        return this.icon;
    }

    public long defaultDurationMillis() {
        return this.defaultDurationMillis;
    }

    public List<StatModifier> modifiers() {
        return this.modifiers;
    }
}
