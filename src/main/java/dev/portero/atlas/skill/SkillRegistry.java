package dev.portero.atlas.skill;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillRegistry {

    private final Map<String, SkillDefinition> skills = new ConcurrentHashMap<>();

    public void register(SkillDefinition skill) {
        if (this.skills.putIfAbsent(skill.id(), skill) != null) {
            throw new IllegalStateException("Skill already registered: " + skill.id());
        }
    }

    public Optional<SkillDefinition> find(String id) {
        return Optional.ofNullable(this.skills.get(id));
    }

    public Collection<SkillDefinition> values() {
        return this.skills.values();
    }
}
