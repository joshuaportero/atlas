package dev.portero.atlas.zombies.game;

import dev.portero.atlas.zombies.arena.BlockPos;
import dev.portero.atlas.zombies.arena.Cuboid;
import dev.portero.atlas.zombies.arena.WindowDef;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowStateTest {

    private WindowState newWindow() {
        Cuboid planks = new Cuboid("world", 0, 64, 0, 2, 65, 0);
        return new WindowState(new WindowDef("front", planks, new Location(null, 0, 64, 5), Material.OAK_PLANKS));
    }

    @Test
    void tracksAllPlankPositions() {
        assertEquals(6, this.newWindow().totalPlanks());
        assertTrue(this.newWindow().isIntact());
    }

    @Test
    void nearestIntactSkipsBrokenPlanks() {
        WindowState window = this.newWindow();
        Location zombie = new Location(null, 0, 64, -3);

        Optional<BlockPos> first = window.nearestIntact(zombie);
        assertTrue(first.isPresent());
        assertEquals(new BlockPos(0, 64, 0), first.get());

        window.markBroken(first.get());
        Optional<BlockPos> second = window.nearestIntact(zombie);
        assertTrue(second.isPresent());
        assertFalse(second.get().equals(first.get()));
    }

    @Test
    void repairsComeBackInBreakOrder() {
        WindowState window = this.newWindow();
        BlockPos first = new BlockPos(0, 64, 0);
        BlockPos second = new BlockPos(1, 64, 0);
        window.markBroken(first);
        window.markBroken(second);

        assertEquals(Optional.of(first), window.nextRepair());
        window.markRepaired(first);
        assertEquals(Optional.of(second), window.nextRepair());
        window.markRepaired(second);
        assertTrue(window.isIntact());
    }

    @Test
    void distanceUsesClosestPlank() {
        WindowState window = this.newWindow();
        // Directly on the nearest plank center vs. far away.
        assertTrue(window.distanceSquaredTo(new Location(null, 0.5, 64.5, 0.5)) < 0.01);
        assertTrue(window.distanceSquaredTo(new Location(null, 0.5, 64.5, 10.5)) > 90.0);
    }

    @Test
    void opensOnlyWhenEveryPlankIsBroken() {
        WindowState window = this.newWindow();
        assertFalse(window.isOpen());
        for (int x = 0; x <= 2; x++) {
            for (int y = 64; y <= 65; y++) {
                window.markBroken(new BlockPos(x, y, 0));
            }
        }
        assertTrue(window.isOpen());
        assertTrue(window.nearestIntact(new Location(null, 0, 64, -3)).isEmpty());
    }
}
