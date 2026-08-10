package dev.portero.atlas.zombies.arena;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Immutable integer block coordinate inside an arena world.
 *
 * @param x block x
 * @param y block y
 * @param z block z
 */
public record BlockPos(int x, int y, int z) {

    /**
     * Centered location of this block in the given world (for sounds/particles).
     *
     * @param world world the block lives in
     * @return location at the block center
     */
    public Location center(World world) {
        return new Location(world, this.x + 0.5, this.y + 0.5, this.z + 0.5);
    }

    /**
     * Squared distance from this block's center to the given location.
     *
     * @param location target location
     * @return squared distance
     */
    public double distanceSquaredTo(Location location) {
        double dx = this.x + 0.5 - location.getX();
        double dy = this.y + 0.5 - location.getY();
        double dz = this.z + 0.5 - location.getZ();
        return dx * dx + dy * dy + dz * dz;
    }
}
