package dev.portero.atlas.zombies.game;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * The single repeating task driving one {@link GameSession}. Precision work (hits, deaths)
 * stays event-driven; this ticker only advances timers, spawn pacing and the HUD.
 */
public class GameTicker extends BukkitRunnable {

    /** Tick period in server ticks; the session counts time in these steps. */
    static final long PERIOD_TICKS = 10L;

    private final GameSession session;

    public GameTicker(GameSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        this.session.tick();
    }
}
