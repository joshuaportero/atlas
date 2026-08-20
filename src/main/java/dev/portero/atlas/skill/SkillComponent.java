package dev.portero.atlas.skill;

import dev.portero.atlas.data.profile.ProfileComponent;
import dev.portero.atlas.util.PayloadCodec;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SkillComponent implements ProfileComponent {

    public static final String key = "skills";

    private final Set<String> unlocked = new LinkedHashSet<>();
    private final Set<String> equipped = new LinkedHashSet<>();

    @Override
    public String key() {
        return key;
    }

    @Override
    public String serialize() {
        return PayloadCodec.write(Map.of(
                "unlocked", String.join(",", this.unlocked),
                "equipped", String.join(",", this.equipped)));
    }

    @Override
    public void deserialize(String payload) {
        Map<String, String> values = PayloadCodec.read(payload);
        this.unlocked.clear();
        this.equipped.clear();
        this.unlocked.addAll(this.split(values.get("unlocked")));
        this.equipped.addAll(this.split(values.get("equipped")));
    }

    public Set<String> unlocked() {
        return this.unlocked;
    }

    public Set<String> equipped() {
        return this.equipped;
    }

    private Set<String> split(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .filter(part -> !part.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
