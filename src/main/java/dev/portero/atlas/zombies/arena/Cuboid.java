package dev.portero.atlas.zombies.arena;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Axis-aligned block region of an arena. Bounds are normalized in the compact constructor
 * so {@code min*} is always less than or equal to {@code max*}.
 */
public record Cuboid(String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public Cuboid {
        if (minX > maxX) {
            int tmp = minX;
            minX = maxX;
            maxX = tmp;
        }
        if (minY > maxY) {
            int tmp = minY;
            minY = maxY;
            maxY = tmp;
        }
        if (minZ > maxZ) {
            int tmp = minZ;
            minZ = maxZ;
            maxZ = tmp;
        }
    }

    /**
     * Creates a cuboid spanning the two given corner locations.
     *
     * @param world world name both corners belong to
     * @param first first corner
     * @param second second corner
     * @return normalized cuboid
     */
    public static Cuboid of(String world, Location first, Location second) {
        return new Cuboid(world,
                first.getBlockX(), first.getBlockY(), first.getBlockZ(),
                second.getBlockX(), second.getBlockY(), second.getBlockZ());
    }

    /**
     * Checks whether the given location is inside this cuboid (inclusive bounds, same world).
     *
     * @param location location to test
     * @return true when inside
     */
    public boolean contains(Location location) {
        return location.getWorld() != null
                && location.getWorld().getName().equals(this.world)
                && location.getBlockX() >= this.minX && location.getBlockX() <= this.maxX
                && location.getBlockY() >= this.minY && location.getBlockY() <= this.maxY
                && location.getBlockZ() >= this.minZ && location.getBlockZ() <= this.maxZ;
    }

    /**
     * Lists all chunk columns (chunkX, chunkZ) this cuboid overlaps. Used by the
     * budgeted snapshot restore.
     *
     * @return list of {@code {chunkX, chunkZ}} pairs
     */
    public List<int[]> chunkColumns() {
        List<int[]> columns = new ArrayList<>();
        for (int chunkX = this.minX >> 4; chunkX <= this.maxX >> 4; chunkX++) {
            for (int chunkZ = this.minZ >> 4; chunkZ <= this.maxZ >> 4; chunkZ++) {
                columns.add(new int[] {chunkX, chunkZ});
            }
        }
        return columns;
    }
}
