package dev.portero.atlas.zombies.game;

/**
 * Lifecycle states of a {@link GameSession}. Boss and event sub-states from the design
 * document are layered on in later milestones.
 */
public enum GameState {
    LOBBY,
    STARTING,
    INTERMISSION,
    IN_ROUND,
    GAME_OVER,
    RESETTING
}
