package dev.portero.atlas.zombies.game;

import dev.portero.atlas.zombies.arena.BlockPos;
import dev.portero.atlas.zombies.arena.Cuboid;
import dev.portero.atlas.zombies.arena.WindowDef;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Per-game state of one window barricade. Pure bookkeeping — the owning
 * {@link GameSession} applies the actual world changes, which keeps this unit-testable.
 */
public class WindowState {

    private final WindowDef definition;
    private final List<BlockPos> positions = new ArrayList<>();
    private final Set<BlockPos> broken = new LinkedHashSet<>();

    public WindowState(WindowDef definition) {
        this.definition = definition;
        Cuboid planks = definition.planks();
        for (int x = planks.minX(); x <= planks.maxX(); x++) {
            for (int y = planks.minY(); y <= planks.maxY(); y++) {
                for (int z = planks.minZ(); z <= planks.maxZ(); z++) {
                    this.positions.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    public WindowDef definition() {
        return this.definition;
    }

    /**
     * Finds the intact plank closest to the given location (the one a zombie would tear next).
     *
     * @param from location to measure from
     * @return closest intact plank, or empty when the window is fully open
     */
    public Optional<BlockPos> nearestIntact(Location from) {
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (BlockPos pos : this.positions) {
            if (this.broken.contains(pos)) {
                continue;
            }
            double distance = pos.distanceSquaredTo(from);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = pos;
            }
        }
        return Optional.ofNullable(nearest);
    }

    /**
     * Next plank to rebuild (planks come back in the order they were broken).
     *
     * @return the position to repair, or empty when the window is intact
     */
    public Optional<BlockPos> nextRepair() {
        return this.broken.stream().findFirst();
    }

    /**
     * Squared distance from the closest plank of this window to the given location.
     * Used for repair range and "zombie at the window" checks.
     *
     * @param location location to measure from
     * @return squared distance to the nearest plank position
     */
    public double distanceSquaredTo(Location location) {
        double min = Double.MAX_VALUE;
        for (BlockPos pos : this.positions) {
            min = Math.min(min, pos.distanceSquaredTo(location));
        }
        return min;
    }

    public void markBroken(BlockPos pos) {
        this.broken.add(pos);
    }

    public void markRepaired(BlockPos pos) {
        this.broken.remove(pos);
    }

    public boolean isOpen() {
        return this.broken.size() == this.positions.size();
    }

    public boolean isIntact() {
        return this.broken.isEmpty();
    }

    public int totalPlanks() {
        return this.positions.size();
    }

    public int brokenCount() {
        return this.broken.size();
    }
}
