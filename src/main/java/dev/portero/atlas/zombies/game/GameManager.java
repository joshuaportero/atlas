package dev.portero.atlas.zombies.game;

import dev.portero.atlas.zombies.ZombiesModule;
import dev.portero.atlas.zombies.arena.Arena;
import dev.portero.atlas.zombies.worldedit.WorldEditService;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns all active game sessions and the join/leave lifecycle. At most one session per
 * arena exists at any time; arenas stay join-blocked while their world is restoring.
 */
public class GameManager {

    private final ZombiesModule module;
    private final Map<String, GameSession> byArena = new ConcurrentHashMap<>();
    private final Map<UUID, GameSession> byPlayer = new ConcurrentHashMap<>();
    private final Set<String> resettingArenas = ConcurrentHashMap.newKeySet();

    public GameManager(ZombiesModule module) {
        this.module = module;
    }

    /**
     * Joins a player into an arena, creating the session lazily on first join.
     *
     * @param player joining player
     * @param arena target arena
     * @return result describing the outcome to message the player
     */
    public JoinResult join(Player player, Arena arena) {
        if (this.byPlayer.containsKey(player.getUniqueId())) {
            return JoinResult.ALREADY_IN_GAME;
        }
        if (this.resettingArenas.contains(arena.getId())) {
            return JoinResult.ARENA_RESETTING;
        }
        GameSession session = this.byArena.computeIfAbsent(arena.getId(), id -> new GameSession(this.module, arena));
        if (!session.isJoinable()) {
            return JoinResult.NOT_JOINABLE;
        }
        session.addPlayer(player);
        this.byPlayer.put(player.getUniqueId(), session);
        return JoinResult.SUCCESS;
    }

    /**
     * Removes a player from whichever game they are in, if any.
     *
     * @param player leaving player
     */
    public void leave(Player player) {
        GameSession session = this.byPlayer.remove(player.getUniqueId());
        if (session != null) {
            session.removePlayer(player);
        }
    }

    /**
     * Force-starts the countdown of an arena's session, creating it when empty.
     *
     * @param arena arena to start
     * @return true when a countdown is now running
     */
    public boolean forceStart(Arena arena) {
        if (this.resettingArenas.contains(arena.getId())) {
            return false;
        }
        GameSession session = this.byArena.get(arena.getId());
        if (session == null) {
            return false;
        }
        session.forceStart();
        return true;
    }

    /**
     * Stops a running arena game immediately.
     *
     * @param arenaId arena id
     * @return true when a session was closed
     */
    public boolean stop(String arenaId) {
        GameSession session = this.byArena.get(arenaId);
        if (session == null) {
            return false;
        }
        session.close();
        return true;
    }

    /**
     * Closes every session (plugin shutdown). Restores run as usual per session.
     */
    public void endAll() {
        for (GameSession session : this.byArena.values()) {
            session.close();
        }
        this.byArena.clear();
        this.byPlayer.clear();
    }

    public Optional<GameSession> sessionOf(UUID playerId) {
        return Optional.ofNullable(this.byPlayer.get(playerId));
    }

    public Optional<GameSession> sessionOfArena(String arenaId) {
        return Optional.ofNullable(this.byArena.get(arenaId));
    }

    /**
     * Removes a closed session from the registry and kicks off the arena world restore.
     * Called by {@link GameSession#close()}.
     *
     * @param session closed session
     */
    void dispose(GameSession session) {
        String arenaId = session.getArena().getId();
        this.byArena.remove(arenaId, session);
        session.playerIds().forEach(this.byPlayer::remove);

        WorldEditService worldEdit = this.module.getWorldEditService();
        File schematic = this.module.getArenaRepository().schematicFile(arenaId);
        if (worldEdit == null || !schematic.exists()) {
            return;
        }
        this.resettingArenas.add(arenaId);
        worldEdit.restore(session.getArena(), schematic, () -> this.resettingArenas.remove(arenaId));
    }

    /** Join attempt outcomes, mapped to messages by the command layer. */
    public enum JoinResult {
        SUCCESS,
        ALREADY_IN_GAME,
        NOT_JOINABLE,
        ARENA_RESETTING
    }
}
