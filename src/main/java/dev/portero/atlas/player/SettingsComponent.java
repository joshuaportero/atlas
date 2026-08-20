package dev.portero.atlas.player;

import dev.portero.atlas.data.profile.ProfileComponent;
import dev.portero.atlas.util.PayloadCodec;

import java.util.Map;

public final class SettingsComponent implements ProfileComponent {

    public static final String key = "settings";

    private boolean combatFeedback = true;
    private boolean resourceRegen = true;
    private boolean eventOptIn = true;

    @Override
    public String key() {
        return key;
    }

    @Override
    public String serialize() {
        return PayloadCodec.write(Map.of(
                "combat_feedback", Boolean.toString(this.combatFeedback),
                "resource_regen", Boolean.toString(this.resourceRegen),
                "event_opt_in", Boolean.toString(this.eventOptIn)));
    }

    @Override
    public void deserialize(String payload) {
        Map<String, String> values = PayloadCodec.read(payload);
        this.combatFeedback = Boolean.parseBoolean(values.getOrDefault("combat_feedback", "true"));
        this.resourceRegen = Boolean.parseBoolean(values.getOrDefault("resource_regen", "true"));
        this.eventOptIn = Boolean.parseBoolean(values.getOrDefault("event_opt_in", "true"));
    }

    public boolean combatFeedback() {
        return this.combatFeedback;
    }

    public void combatFeedback(boolean combatFeedback) {
        this.combatFeedback = combatFeedback;
    }

    public boolean resourceRegen() {
        return this.resourceRegen;
    }

    public void resourceRegen(boolean resourceRegen) {
        this.resourceRegen = resourceRegen;
    }

    public boolean eventOptIn() {
        return this.eventOptIn;
    }

    public void eventOptIn(boolean eventOptIn) {
        this.eventOptIn = eventOptIn;
    }
}
