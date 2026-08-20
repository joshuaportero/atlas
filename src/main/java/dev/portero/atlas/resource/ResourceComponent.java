package dev.portero.atlas.resource;

import dev.portero.atlas.data.profile.ProfileComponent;
import dev.portero.atlas.util.PayloadCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ResourceComponent implements ProfileComponent {

    public static final String key = "resources";

    private final Map<String, Double> values = new ConcurrentHashMap<>();

    @Override
    public String key() {
        return key;
    }

    @Override
    public String serialize() {
        return PayloadCodec.writeDoubles(this.values);
    }

    @Override
    public void deserialize(String payload) {
        this.values.clear();
        this.values.putAll(PayloadCodec.readDoubles(payload));
    }

    public Map<String, Double> values() {
        return this.values;
    }
}
