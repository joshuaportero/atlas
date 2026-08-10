package dev.portero.atlas.zombies.combat.gun;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Config-backed definition of a gun. All stats live in {@code zombies/guns.yml};
 * adding a gun is a config-only change.
 *
 * @param id unique id (config key)
 * @param displayName legacy-formatted display name
 * @param material vanilla item the gun is built from
 * @param projectile true for projectile guns (ray gun) instead of hitscan
 * @param aimDownSights true when plain right-click keeps vanilla behavior (spyglass scope)
 *                      and firing requires sneak + right-click
 * @param starting true for the pistol every player receives at round 1
 * @param damage damage per pellet/ray
 * @param headshotMultiplier damage multiplier when the hit lands on the upper body
 * @param fireIntervalMillis minimum delay between shots (semi-auto)
 * @param magazine magazine size
 * @param reserve starting reserve ammo
 * @param reloadMillis reload duration
 * @param spreadDegrees random cone spread per shot
 * @param recoilPitch camera kick per shot, in degrees
 * @param range hitscan range in blocks
 * @param pellets rays per shot (1 for everything except shotguns)
 * @param aoeRadius explosion radius for projectile guns
 * @param fireSound sound key, e.g. {@code entity.generic.explode}
 * @param soundPitch pitch of the fire sound
 * @param boxWeight mystery box weight (0 = not in the box)
 */
public record GunDefinition(
        String id,
        String displayName,
        Material material,
        boolean projectile,
        boolean aimDownSights,
        boolean starting,
        double damage,
        double headshotMultiplier,
        long fireIntervalMillis,
        int magazine,
        int reserve,
        long reloadMillis,
        double spreadDegrees,
        double recoilPitch,
        double range,
        int pellets,
        double aoeRadius,
        String fireSound,
        float soundPitch,
        int boxWeight) {

    /**
     * Parses a gun from its config section.
     *
     * @param id config key
     * @param section gun section
     * @return parsed definition
     * @throws IllegalArgumentException when the material is unknown
     */
    public static GunDefinition fromYaml(String id, ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("material", ""));
        if (material == null) {
            throw new IllegalArgumentException("unknown material '" + section.getString("material") + "'");
        }
        return new GunDefinition(
                id,
                section.getString("displayName", id),
                material,
                section.getBoolean("projectile", false),
                section.getBoolean("aimDownSights", false),
                section.getBoolean("starting", false),
                section.getDouble("damage", 6.0),
                section.getDouble("headshotMultiplier", 1.5),
                section.getLong("fireIntervalMillis", 300L),
                section.getInt("magazine", 8),
                section.getInt("reserve", 80),
                section.getLong("reloadMillis", 1500L),
                section.getDouble("spreadDegrees", 1.0),
                section.getDouble("recoilPitch", 1.0),
                section.getDouble("range", 50.0),
                section.getInt("pellets", 1),
                section.getDouble("aoeRadius", 0.0),
                section.getString("fireSound", "entity.generic.explode"),
                (float) section.getDouble("soundPitch", 1.0),
                section.getInt("boxWeight", 10));
    }
}
