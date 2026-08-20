package dev.portero.atlas.skill;

import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.stat.StatManager;
import dev.portero.atlas.stat.StatModifier;

public final class SkillService {

    public static final int maxEquipped = 3;

    private final SkillRegistry registry;
    private final StatManager stats;

    public SkillService(SkillRegistry registry, StatManager stats) {
        this.registry = registry;
        this.stats = stats;
    }

    public SkillRegistry registry() {
        return this.registry;
    }

    public SkillComponent component(AtlasPlayer player) {
        SkillComponent component = player.profile().component(SkillComponent.class)
                .orElseGet(SkillComponent::new);
        if (component.unlocked().isEmpty()) {
            this.registry.values().forEach(skill -> component.unlocked().add(skill.id()));
            player.profile().attach(component);
        } else {
            player.profile().attach(component, false);
        }
        return component;
    }

    public boolean unlock(AtlasPlayer player, String skillId) {
        if (this.registry.find(skillId).isEmpty()) {
            return false;
        }
        SkillComponent component = this.component(player);
        boolean added = component.unlocked().add(skillId);
        if (added) {
            player.profile().attach(component);
        }
        return added;
    }

    public boolean revoke(AtlasPlayer player, String skillId) {
        SkillComponent component = this.component(player);
        this.unequip(player, skillId);
        boolean removed = component.unlocked().remove(skillId);
        if (removed) {
            player.profile().attach(component);
        }
        return removed;
    }

    public boolean equip(AtlasPlayer player, String skillId) {
        SkillComponent component = this.component(player);
        if (!component.unlocked().contains(skillId) || component.equipped().contains(skillId)) {
            return false;
        }
        if (component.equipped().size() >= maxEquipped) {
            return false;
        }
        SkillDefinition skill = this.registry.find(skillId).orElse(null);
        if (skill == null) {
            return false;
        }
        component.equipped().add(skillId);
        player.profile().attach(component);
        this.apply(player, skill);
        return true;
    }

    public boolean unequip(AtlasPlayer player, String skillId) {
        SkillComponent component = this.component(player);
        if (!component.equipped().remove(skillId)) {
            return false;
        }
        player.profile().attach(component);
        this.stats.removeModifiers(player, "skill:" + skillId);
        return true;
    }

    public void applyEquipped(AtlasPlayer player) {
        SkillComponent component = this.component(player);
        for (String skillId : component.equipped()) {
            this.registry.find(skillId).ifPresent(skill -> this.apply(player, skill));
        }
    }

    private void apply(AtlasPlayer player, SkillDefinition skill) {
        this.stats.removeModifiers(player, "skill:" + skill.id());
        for (StatModifier modifier : skill.modifiers()) {
            this.stats.addModifier(player, new StatModifier(
                    "skill:" + skill.id(), modifier.stat(), modifier.operation(), modifier.amount()));
        }
    }
}
