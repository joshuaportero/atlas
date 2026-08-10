package dev.portero.atlas.zombies.arena;

/**
 * Per-arena tunable settings.
 *
 * @param minPlayers players required to trigger the automatic start countdown
 * @param maxPlayers hard capacity of the arena
 * @param startingPoints points each player receives when the game starts
 */
public record ArenaSettings(int minPlayers, int maxPlayers, int startingPoints) {

    private static final int DEFAULT_MIN_PLAYERS = 1;
    private static final int DEFAULT_MAX_PLAYERS = 4;
    private static final int DEFAULT_STARTING_POINTS = 500;

    /**
     * Default settings used for newly created arenas.
     *
     * @return default arena settings
     */
    public static ArenaSettings defaults() {
        return new ArenaSettings(DEFAULT_MIN_PLAYERS, DEFAULT_MAX_PLAYERS, DEFAULT_STARTING_POINTS);
    }
}
