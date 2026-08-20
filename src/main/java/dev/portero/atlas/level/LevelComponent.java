package dev.portero.atlas.level;

import dev.portero.atlas.data.profile.ProfileComponent;
import dev.portero.atlas.util.PayloadCodec;

import java.util.Map;

public final class LevelComponent implements ProfileComponent {

    public static final String key = "level";

    private int level = 1;
    private long xp;

    @Override
    public String key() {
        return key;
    }

    @Override
    public String serialize() {
        return PayloadCodec.write(Map.of(
                "level", Integer.toString(this.level),
                "xp", Long.toString(this.xp)));
    }

    @Override
    public void deserialize(String payload) {
        Map<String, String> values = PayloadCodec.read(payload);
        this.level = Math.max(1, this.parseInt(values.get("level"), 1));
        this.xp = Math.max(0L, this.parseLong(values.get("xp"), 0L));
    }

    public int level() {
        return this.level;
    }

    public void level(int level) {
        this.level = Math.max(1, level);
    }

    public long xp() {
        return this.xp;
    }

    public void xp(long xp) {
        this.xp = Math.max(0L, xp);
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private long parseLong(String value, long fallback) {
        try {
            return value == null ? fallback : Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
