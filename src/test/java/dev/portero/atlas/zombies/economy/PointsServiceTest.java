package dev.portero.atlas.zombies.economy;

import dev.portero.atlas.zombies.game.PlayerSession;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PointsServiceTest {

    @Test
    void awardAddsPoints() {
        PlayerSession session = this.newSession(500);
        PointsService.award(session, 90);
        assertEquals(590, session.getPoints());
    }

    @Test
    void chargeSucceedsWhenAffordable() {
        PlayerSession session = this.newSession(500);
        assertTrue(PointsService.charge(session, 500));
        assertEquals(0, session.getPoints());
    }

    @Test
    void chargeFailsWhenTooExpensive() {
        PlayerSession session = this.newSession(499);
        assertFalse(PointsService.charge(session, 500));
        assertEquals(499, session.getPoints());
    }

    private PlayerSession newSession(int points) {
        return new PlayerSession(UUID.randomUUID(), new Location(null, 0, 0, 0), GameMode.SURVIVAL, points);
    }
}
