package dev.portero.atlas.zombies.arena.setup;

import dev.portero.atlas.zombies.arena.ArenaRepository;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Tracks active setup wizard sessions, one per admin.
 */
public class SetupManager {

    private final Map<UUID, SetupSession> sessions = new HashMap<>();

    /**
     * Starts a setup session for the given arena id, editing the existing arena when present.
     *
     * @param player admin running the wizard
     * @param arenaId arena id
     * @param repository repository to pre-load existing arenas from
     * @return the new session
     */
    public SetupSession start(Player player, String arenaId, ArenaRepository repository) {
        SetupSession session = repository.find(arenaId)
                .map(SetupSession::edit)
                .orElseGet(() -> SetupSession.create(arenaId));
        this.sessions.put(player.getUniqueId(), session);
        return session;
    }

    public Optional<SetupSession> sessionOf(UUID playerId) {
        return Optional.ofNullable(this.sessions.get(playerId));
    }

    public void end(UUID playerId) {
        this.sessions.remove(playerId);
    }
}
