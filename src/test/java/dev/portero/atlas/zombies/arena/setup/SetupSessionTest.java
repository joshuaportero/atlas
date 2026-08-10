package dev.portero.atlas.zombies.arena.setup;

import dev.portero.atlas.zombies.arena.Cuboid;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupSessionTest {

    @Test
    void freshSessionReportsEverythingMissing() {
        SetupSession session = SetupSession.create("Test");
        assertEquals("test", session.getArenaId());
        assertEquals(4, session.missing().size());
        assertFalse(session.isComplete());
    }

    @Test
    void sessionBecomesCompleteWhenAllStepsAreSet() {
        SetupSession session = SetupSession.create("test");
        session.setRegion(new Cuboid("world", 0, 0, 0, 10, 10, 10));
        session.setLobbySpawn(new Location(null, 0, 0, 0));
        session.getPlayerSpawns().add(new Location(null, 1, 0, 0));
        session.getZombieSpawns().add(new Location(null, 2, 0, 0));

        assertTrue(session.isComplete());
        assertTrue(session.missing().isEmpty());
    }
}
