package dev.portero.atlas.zombies.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serializes {@link Location} instances to plain maps for YAML storage and back.
 * The world is not stored per location; the owning arena defines it.
 */
public final class LocationCodec {

    private LocationCodec() {
    }

    /**
     * Converts a location to a YAML-friendly map.
     *
     * @param location location to serialize
     * @return ordered map with x, y, z, yaw, pitch
     */
    public static Map<String, Object> serialize(Location location) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", location.getX());
        map.put("y", location.getY());
        map.put("z", location.getZ());
        map.put("yaw", (double) location.getYaw());
        map.put("pitch", (double) location.getPitch());
        return map;
    }

    /**
     * Reads a location from a map previously written by {@link #serialize(Location)}.
     *
     * @param worldName world the location belongs to
     * @param map raw map (usually from {@code getMapList})
     * @return the location, or null when the world is missing or values are malformed
     */
    @Nullable
    public static Location deserialize(String worldName, Map<?, ?> map) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        try {
            double x = ((Number) map.get("x")).doubleValue();
            double y = ((Number) map.get("y")).doubleValue();
            double z = ((Number) map.get("z")).doubleValue();
            float yaw = map.get("yaw") instanceof Number number ? number.floatValue() : 0.0f;
            float pitch = map.get("pitch") instanceof Number number ? number.floatValue() : 0.0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (ClassCastException | NullPointerException error) {
            return null;
        }
    }
}
