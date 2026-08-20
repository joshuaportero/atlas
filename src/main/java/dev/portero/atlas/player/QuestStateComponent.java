package dev.portero.atlas.player;

import dev.portero.atlas.data.profile.ProfileComponent;
import dev.portero.atlas.util.PayloadCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestStateComponent implements ProfileComponent {

    public static final String key = "quest_states";

    private final Map<String, String> states = new ConcurrentHashMap<>();

    @Override
    public String key() {
        return key;
    }

    @Override
    public String serialize() {
        return PayloadCodec.write(this.states);
    }

    @Override
    public void deserialize(String payload) {
        this.states.clear();
        this.states.putAll(PayloadCodec.read(payload));
    }

    public Map<String, String> states() {
        return this.states;
    }
}
