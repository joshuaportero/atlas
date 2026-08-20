package dev.portero.atlas.data.session;

import dev.portero.atlas.data.profile.Profile;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionService {

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public Session open(Player player, Profile profile) {
        Session session = new Session(player, profile);
        this.sessions.put(player.getUniqueId(), session);
        return session;
    }

    public Optional<Session> find(Player player) {
        return this.find(player.getUniqueId());
    }

    public Optional<Session> find(UUID uniqueId) {
        return Optional.ofNullable(this.sessions.get(uniqueId));
    }

    public Session require(Player player) {
        return this.find(player).orElseThrow(() -> new IllegalStateException(
                "No session for " + player.getName()));
    }

    public Optional<Session> close(UUID uniqueId) {
        Session session = this.sessions.remove(uniqueId);
        if (session != null) {
            session.clear();
        }
        return Optional.ofNullable(session);
    }

    public Collection<Session> sessions() {
        return this.sessions.values();
    }
}
