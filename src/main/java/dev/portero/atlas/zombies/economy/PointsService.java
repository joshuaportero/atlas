package dev.portero.atlas.zombies.economy;

import dev.portero.atlas.zombies.game.PlayerSession;

/**
 * Single mutation point for points. Keeping awarding and spending here makes the flow
 * auditable and gives events/hooks one place to attach later (double points, penalties).
 */
public final class PointsService {

    private PointsService() {
    }

    /**
     * Awards points to a player session.
     *
     * @param session target session
     * @param amount points to add (may be negative for penalties)
     */
    public static void award(PlayerSession session, int amount) {
        session.addPoints(amount);
    }

    /**
     * Attempts to charge points from a player session.
     *
     * @param session target session
     * @param price price to charge
     * @return true when the player could afford it and was charged
     */
    public static boolean charge(PlayerSession session, int price) {
        if (session.getPoints() < price) {
            return false;
        }
        session.addPoints(-price);
        return true;
    }
}
