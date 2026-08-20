package dev.portero.atlas.skill;

import dev.portero.atlas.stat.StatModifier;
import org.bukkit.Material;

import java.util.List;

public final class SkillDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final Material icon;
    private final List<StatModifier> modifiers;

    public SkillDefinition(String id, String name, String description, Material icon,
                           List<StatModifier> modifiers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
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

    public List<StatModifier> modifiers() {
        return this.modifiers;
    }
}
