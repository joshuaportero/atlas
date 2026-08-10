package dev.portero.atlas.zombies.game;

import dev.portero.atlas.zombies.ZombiesConfig;

import java.util.Random;

/**
 * Pure round math: how many zombies spawn, how strong they are and how fast the round
 * trickles them in. No Bukkit state, fully unit-testable.
 */
public class RoundDirector {

    private static final double ZOMBIE_BASE_HEALTH = 20.0;
    private static final int MIN_SPAWN_INTERVAL_TICKS = 5;

    private final ZombiesConfig config;
    private final Random random = new Random();

    public RoundDirector(ZombiesConfig config) {
        this.config = config;
    }

    /**
     * Total zombies spawned over the course of the round.
     *
     * @param round round number (1-based)
     * @param players players alive in the game
     * @return zombie count for the round
     */
    public int zombiesForRound(int round, int players) {
        return this.config.getBaseZombies()
                + (round - 1) * this.config.getZombiesPerRound()
                + Math.max(0, players - 1) * this.config.getZombiesPerExtraPlayer();
    }

    /**
     * Zombie max health for the round. Smooth linear scaling for now; bosses and
     * steeper late-round curves are layered on later.
     *
     * @param round round number (1-based)
     * @return max health value
     */
    public double healthForRound(int round) {
        return ZOMBIE_BASE_HEALTH * (1.0 + (round - 1) * this.config.getHealthMultiplierPerRound());
    }

    /**
     * Movement speed for a single zombie spawn. Past {@code sprinterFromRound} a growing
     * share of zombies becomes sprinters.
     *
     * @param round round number (1-based)
     * @return movement speed attribute value
     */
    public double speedForRound(int round) {
        if (round < this.config.getSprinterFromRound()) {
            return this.config.getBaseSpeed();
        }
        double chance = Math.min(this.config.getSprinterChanceMax(),
                (round - this.config.getSprinterFromRound() + 1) * this.config.getSprinterChanceStep());
        return this.random.nextDouble() < chance
                ? this.config.getSprintSpeed()
                : this.config.getBaseSpeed();
    }

    /**
     * Ticks between zombie spawns, shrinking slightly as rounds progress.
     *
     * @param round round number (1-based)
     * @return spawn interval in ticks
     */
    public int spawnIntervalTicks(int round) {
        return Math.max(MIN_SPAWN_INTERVAL_TICKS, this.config.getSpawnIntervalTicks() - (round - 1));
    }
}
