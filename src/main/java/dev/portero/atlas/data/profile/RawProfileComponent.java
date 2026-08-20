package dev.portero.atlas.data.profile;

public final class RawProfileComponent implements ProfileComponent {

    private final String key;
    private String payload;

    public RawProfileComponent(String key, String payload) {
        this.key = key;
        this.payload = payload;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public String serialize() {
        return this.payload;
    }

    @Override
    public void deserialize(String payload) {
        this.payload = payload;
    }
}
