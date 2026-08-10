package dev.portero.atlas.zombies.hud;

import dev.portero.atlas.util.MessageUtil;
import dev.portero.atlas.zombies.game.PlayerSession;
import org.bukkit.entity.Player;

/**
 * Action-bar HUD shown while a game runs. The full sidebar (scoreboard-library)
 * arrives with the polish milestone.
 */
public final class HudService {

    private HudService() {
    }

    /**
     * Renders the one-line game HUD onto the player's action bar.
     *
     * @param player target player
     * @param session their per-game state
     * @param round current round number
     * @param zombiesLeft zombies alive plus still queued to spawn
     */
    public static void send(Player player, PlayerSession session, int round, int zombiesLeft) {
        player.sendActionBar(MessageUtil.format(
                "&cRound " + round + " &8| &e" + session.getPoints() + " points &8| &c" + zombiesLeft + " left"));
    }
}
