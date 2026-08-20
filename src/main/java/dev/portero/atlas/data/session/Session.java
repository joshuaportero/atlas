package dev.portero.atlas.data.session;

import dev.portero.atlas.data.profile.Profile;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Session {

    private final Player player;
    private final Profile profile;
    private final Map<Class<?>, Object> attachments = new ConcurrentHashMap<>();

    public Session(Player player, Profile profile) {
        this.player = player;
        this.profile = profile;
    }

    public Player player() {
        return this.player;
    }

    public UUID uniqueId() {
        return this.player.getUniqueId();
    }

    public Profile profile() {
        return this.profile;
    }

    public <T> void set(Class<T> type, T value) {
        this.attachments.put(type, value);
    }

    public <T> Optional<T> get(Class<T> type) {
        return Optional.ofNullable(this.attachments.get(type)).map(type::cast);
    }

    public <T> T require(Class<T> type) {
        return this.get(type).orElseThrow(() -> new IllegalStateException(
                "Missing session attachment: " + type.getName()));
    }

    public void clear() {
        this.attachments.clear();
    }
}
