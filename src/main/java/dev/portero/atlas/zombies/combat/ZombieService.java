package dev.portero.atlas.zombies.combat;

import dev.portero.atlas.zombies.ZombiesKeys;
import dev.portero.atlas.zombies.arena.Arena;
import dev.portero.atlas.zombies.game.RoundDirector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;

/**
 * Spawns and configures game zombies. M1 uses plain vanilla zombies scaled by round;
 * typed variants (sprinter/brute/spitter/...) plug into this service later.
 */
public class ZombieService {

    private static final double FOLLOW_RANGE = 100.0;
    private static final String DEFAULT_TYPE = "shambler";

    private final Plugin plugin;
    private final ZombiesKeys keys;
    private final Random random = new Random();

    public ZombieService(Plugin plugin, ZombiesKeys keys) {
        this.plugin = plugin;
        this.keys = keys;
    }

    /**
     * Spawns one round-scaled zombie at a random zombie spawn of the arena.
     *
     * @param arena arena to spawn in
     * @param round current round number
     * @param director round math
     * @param target preferred initial target (nearest alive player), may be null
     * @return the spawned zombie, or null when the arena world is not loaded
     */
    public Zombie spawn(Arena arena, int round, RoundDirector director, Player target) {
        List<Location> spawns = arena.getZombieSpawns();
        return this.spawnAt(arena, spawns.get(this.random.nextInt(spawns.size())), round, director, target);
    }

    /**
     * Spawns one round-scaled zombie at an explicit location (used for window-assigned spawns).
     *
     * @param arena arena to spawn in
     * @param location exact spawn location
     * @param round current round number
     * @param director round math
     * @param target preferred initial target (nearest alive player), may be null
     * @return the spawned zombie, or null when the arena world is not loaded
     */
    public Zombie spawnAt(Arena arena, Location location, int round, RoundDirector director, Player target) {
        World world = Bukkit.getWorld(arena.getWorldName());
        if (world == null) {
            return null;
        }

        double health = director.healthForRound(round);
        double speed = director.speedForRound(round);

        return world.spawn(location, Zombie.class, zombie -> {
            zombie.setAdult();
            zombie.setCanPickupItems(false);
            zombie.setRemoveWhenFarAway(false);
            this.setAttribute(zombie, Attribute.MAX_HEALTH, health);
            zombie.setHealth(health);
            this.setAttribute(zombie, Attribute.MOVEMENT_SPEED, speed);
            this.setAttribute(zombie, Attribute.FOLLOW_RANGE, FOLLOW_RANGE);

            // A helmet with zero drop chance keeps zombies from burning in daylight arenas.
            EntityEquipment equipment = zombie.getEquipment();
            if (equipment != null) {
                equipment.setHelmet(new ItemStack(Material.LEATHER_HELMET));
                equipment.setHelmetDropChance(0.0f);
            }

            zombie.getPersistentDataContainer()
                    .set(this.keys.game(), PersistentDataType.STRING, arena.getId());
            zombie.getPersistentDataContainer()
                    .set(this.keys.type(), PersistentDataType.STRING, DEFAULT_TYPE);
            zombie.setTarget(target);
        });
    }

    private void setAttribute(Zombie zombie, Attribute attribute, double value) {
        AttributeInstance instance = zombie.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}
