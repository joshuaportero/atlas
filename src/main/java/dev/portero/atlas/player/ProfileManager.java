package dev.portero.atlas.player;

import dev.portero.atlas.data.session.Session;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileManager {

    private final Map<UUID, AtlasPlayer> online = new ConcurrentHashMap<>();

    public AtlasPlayer attach(Player player, Session session) {
        AtlasPlayer atlasPlayer = new AtlasPlayer(player, session);
        this.online.put(player.getUniqueId(), atlasPlayer);
        session.set(AtlasPlayer.class, atlasPlayer);
        return atlasPlayer;
    }

    public Optional<AtlasPlayer> detach(UUID uniqueId) {
        return Optional.ofNullable(this.online.remove(uniqueId));
    }

    public Optional<AtlasPlayer> find(Player player) {
        return this.find(player.getUniqueId());
    }

    public Optional<AtlasPlayer> find(UUID uniqueId) {
        return Optional.ofNullable(this.online.get(uniqueId));
    }

    public AtlasPlayer require(Player player) {
        return this.find(player).orElseThrow(() -> new IllegalStateException(
                "No Atlas player for " + player.getName()));
    }

    public Collection<AtlasPlayer> online() {
        return this.online.values();
    }
}
