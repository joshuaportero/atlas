package dev.portero.atlas.data.profile;

public interface ProfileComponent {

    String key();

    String serialize();

    void deserialize(String payload);
}
