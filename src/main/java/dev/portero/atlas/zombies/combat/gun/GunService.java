package dev.portero.atlas.zombies.combat.gun;

import dev.portero.atlas.util.MessageUtil;
import dev.portero.atlas.zombies.ZombiesKeys;
import dev.portero.atlas.zombies.game.GameSession;
import dev.portero.atlas.zombies.perk.PerkService;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * All gun mechanics: item identity (PDC-tagged vanilla items), hitscan firing with
 * spread/recoil/tracers, ammo bookkeeping, reload, and projectile (ray gun) shots.
 * Firing is click-per-shot semi-auto; automatic fire is an open follow-up.
 */
@Slf4j
public class GunService {

    private static final double ENTITY_RAY_SIZE = 0.35;
    private static final double HEADSHOT_HEIGHT_FRACTION = 0.75;
    private static final double SPREAD_FACTOR = 0.01;
    private static final double TRACER_START_OFFSET = 1.2;
    private static final double TRACER_STEP = 0.7;
    private static final double PROJECTILE_SPEED = 2.5;

    private final Plugin plugin;
    private final ZombiesKeys keys;
    private final GunRegistry registry;
    private final PerkService perkService;
    private final Map<UUID, Long> lastFire = new HashMap<>();
    private final Map<UUID, BukkitTask> reloads = new HashMap<>();

    public GunService(Plugin plugin, ZombiesKeys keys, GunRegistry registry, PerkService perkService) {
        this.plugin = plugin;
        this.keys = keys;
        this.registry = registry;
        this.perkService = perkService;
    }

    /**
     * Builds a fresh gun item with full magazine and reserve.
     *
     * @param gun gun definition
     * @return the gun item
     */
    public ItemStack createGun(GunDefinition gun) {
        ItemStack item = new ItemStack(gun.material());
        this.writeState(item, gun, gun.magazine(), gun.reserve());
        return item;
    }

    /**
     * Builds a display-only icon of a gun (wall-buys, mystery box cycling).
     *
     * @param gun gun definition
     * @return icon item without ammo state
     */
    public ItemStack createIcon(GunDefinition gun) {
        ItemStack item = new ItemStack(gun.material());
        item.editMeta(meta -> meta.displayName(MessageUtil.format(gun.displayName())));
        return item;
    }

    /**
     * Resolves the gun definition of an item, if it is a gun.
     *
     * @param item item to inspect
     * @return the gun definition, or null for plain items
     */
    @Nullable
    public GunDefinition gunOf(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        String id = item.getItemMeta().getPersistentDataContainer()
                .get(this.keys.gunId(), PersistentDataType.STRING);
        return id != null ? this.registry.find(id).orElse(null) : null;
    }

    /**
     * Checks whether the player carries the given gun anywhere in their inventory.
     *
     * @param player player to check
     * @param gunId gun id
     * @return true when owned
     */
    public boolean ownsGun(Player player, String gunId) {
        for (ItemStack item : player.getInventory()) {
            GunDefinition gun = this.gunOf(item);
            if (gun != null && gun.id().equals(gunId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gives a gun to a player following CoD slot rules: replaces the currently held gun,
     * takes a free slot when under the two-weapon limit, otherwise replaces the held slot.
     *
     * @param player receiver
     * @param gun gun to give
     */
    public void giveGun(Player player, GunDefinition gun) {
        ItemStack item = this.createGun(gun);
        PlayerInventory inventory = player.getInventory();
        int owned = 0;
        for (ItemStack stack : inventory) {
            if (this.gunOf(stack) != null) {
                owned++;
            }
        }
        ItemStack hand = inventory.getItemInMainHand();
        if (this.gunOf(hand) != null || owned >= 2 || inventory.firstEmpty() < 0) {
            inventory.setItemInMainHand(item);
        } else if (hand.getType().isAir()) {
            inventory.setItemInMainHand(item);
        } else {
            inventory.addItem(item);
        }
    }

    /**
     * Refills the reserve ammo of a gun the player owns (wall-buy ammo purchase).
     *
     * @param player owner
     * @param gunId gun id
     * @return true when the gun was found and refilled
     */
    public boolean refillAmmo(Player player, String gunId) {
        for (ItemStack item : player.getInventory()) {
            GunDefinition gun = this.gunOf(item);
            if (gun != null && gun.id().equals(gunId)) {
                this.writeState(item, gun, this.ammoMag(item), gun.reserve());
                return true;
            }
        }
        return false;
    }

    /**
     * Fully refills magazine and reserve of every gun the player owns (Max Ammo power-up).
     *
     * @param player owner
     */
    public void refillAllAmmo(Player player) {
        for (ItemStack item : player.getInventory()) {
            GunDefinition gun = this.gunOf(item);
            if (gun != null) {
                this.writeState(item, gun, gun.magazine(), gun.reserve());
            }
        }
    }

    /**
     * Fires the gun held by the player. Handles cooldown, ammo, hitscan/projectile,
     * damage attribution to the game session, tracer, sound and recoil.
     *
     * @param player shooter
     * @param item gun item (main hand)
     * @param gun gun definition
     * @param session game the shot belongs to
     */
    public void fire(Player player, ItemStack item, GunDefinition gun, GameSession session) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        long interval = (long) (gun.fireIntervalMillis() * this.perkService.fireIntervalMultiplier(player));
        Long last = this.lastFire.get(playerId);
        if ((last != null && now - last < interval) || this.reloads.containsKey(playerId)) {
            return;
        }
        int mag = this.ammoMag(item);
        if (mag <= 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.6f);
            this.startReload(player, item, gun);
            return;
        }
        this.lastFire.put(playerId, now);
        this.writeState(item, gun, mag - 1, this.ammoReserve(item));

        World world = player.getWorld();
        this.playFireSound(world, player.getLocation(), gun);
        this.applyRecoil(player, gun);

        if (gun.projectile()) {
            Snowball ball = player.launchProjectile(Snowball.class,
                    player.getEyeLocation().getDirection().multiply(PROJECTILE_SPEED));
            ball.setItem(new ItemStack(gun.material()));
            ball.getPersistentDataContainer().set(this.keys.gunProjectile(), PersistentDataType.STRING, gun.id());
            return;
        }

        Location eye = player.getEyeLocation();
        for (int pellet = 0; pellet < gun.pellets(); pellet++) {
            this.fireRay(world, eye, gun, player, session);
        }
    }

    /**
     * Starts a reload. No-op when already reloading, the magazine is full or no reserve
     * ammo is left.
     *
     * @param player player reloading
     * @param item gun item
     * @param gun gun definition
     */
    public void startReload(Player player, ItemStack item, GunDefinition gun) {
        UUID playerId = player.getUniqueId();
        if (this.reloads.containsKey(playerId)
                || this.ammoMag(item) >= gun.magazine()
                || this.ammoReserve(item) <= 0) {
            return;
        }
        long ticks = Math.max(1L, (long) (gun.reloadMillis() * this.perkService.reloadSpeedMultiplier(player)) / 50L);
        player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, 1.6f);
        BukkitTask task = this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            this.reloads.remove(playerId);
            ItemStack hand = player.getInventory().getItemInMainHand();
            GunDefinition held = this.gunOf(hand);
            if (held == null || !held.id().equals(gun.id())) {
                return;
            }
            int needed = gun.magazine() - this.ammoMag(hand);
            int transfer = Math.min(needed, this.ammoReserve(hand));
            this.writeState(hand, gun, this.ammoMag(hand) + transfer, this.ammoReserve(hand) - transfer);
            player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 1.2f);
        }, ticks);
        this.reloads.put(playerId, task);
    }

    /**
     * Cancels a pending reload (weapon swap, drop, quit).
     *
     * @param playerId player uuid
     */
    public void cancelReload(UUID playerId) {
        BukkitTask task = this.reloads.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    public boolean isReloading(UUID playerId) {
        return this.reloads.containsKey(playerId);
    }

    /**
     * Resolves the gun id stored on a ray-gun projectile.
     *
     * @param entity projectile entity
     * @return gun id, or null when not a gun projectile
     */
    @Nullable
    public String projectileGunId(Entity entity) {
        return entity.getPersistentDataContainer().get(this.keys.gunProjectile(), PersistentDataType.STRING);
    }

    private void fireRay(World world, Location eye, GunDefinition gun, Player player, GameSession session) {
        double spread = gun.spreadDegrees() * this.perkService.spreadMultiplier(player);
        Vector direction = this.applySpread(eye.getDirection().normalize(), spread);
        RayTraceResult blockHit = world.rayTraceBlocks(eye, direction, gun.range());
        double blockDistance = blockHit != null
                ? eye.toVector().distance(blockHit.getHitPosition())
                : Double.MAX_VALUE;

        RayTraceResult entityHit = world.rayTraceEntities(eye, direction, gun.range(), ENTITY_RAY_SIZE,
                entity -> !entity.getUniqueId().equals(player.getUniqueId())
                        && session.hasZombie(entity.getUniqueId()));

        Vector tracerEnd;
        if (entityHit != null && entityHit.getHitEntity() instanceof LivingEntity target
                && eye.toVector().distance(entityHit.getHitPosition()) < blockDistance) {
            double damage = gun.damage();
            if (session.isInstaKillActive()) {
                damage = 1000.0;
            } else {
                double hitHeight = entityHit.getHitPosition().getY() - target.getLocation().getY();
                if (hitHeight >= target.getHeight() * HEADSHOT_HEIGHT_FRACTION) {
                    damage *= gun.headshotMultiplier() * this.perkService.headshotMultiplierBonus(player);
                }
            }
            target.damage(damage, player);
            tracerEnd = entityHit.getHitPosition();
        } else if (blockHit != null) {
            tracerEnd = blockHit.getHitPosition();
        } else {
            tracerEnd = eye.toVector().add(direction.clone().multiply(gun.range()));
        }
        this.spawnTracer(world, eye, tracerEnd);
    }

    private void spawnTracer(World world, Location eye, Vector hit) {
        Vector position = eye.toVector().add(eye.getDirection().clone().multiply(TRACER_START_OFFSET));
        double length = position.distance(hit);
        if (length <= 0.0) {
            return;
        }
        Vector step = hit.clone().subtract(position).normalize().multiply(TRACER_STEP);
        for (double traveled = 0; traveled < length; traveled += TRACER_STEP) {
            world.spawnParticle(Particle.CRIT, position.getX(), position.getY(), position.getZ(), 1, 0, 0, 0, 0);
            position.add(step);
        }
    }

    private void applyRecoil(Player player, GunDefinition gun) {
        if (gun.recoilPitch() <= 0.0) {
            return;
        }
        Location location = player.getLocation();
        location.setPitch(Math.max(-90.0f, location.getPitch() - (float) gun.recoilPitch()));
        player.teleport(location);
    }

    private Vector applySpread(Vector direction, double degrees) {
        if (degrees <= 0.0) {
            return direction;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double amount = degrees * SPREAD_FACTOR;
        return direction.add(new Vector(
                (random.nextDouble() - 0.5) * amount,
                (random.nextDouble() - 0.5) * amount,
                (random.nextDouble() - 0.5) * amount)).normalize();
    }

    private void playFireSound(World world, Location location, GunDefinition gun) {
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(gun.fireSound()));
        if (sound != null) {
            world.playSound(location, sound, 1.0f, gun.soundPitch());
        }
    }

    private int ammoMag(ItemStack item) {
        return this.ammo(item, this.keys.ammoMag());
    }

    private int ammoReserve(ItemStack item) {
        return this.ammo(item, this.keys.ammoReserve());
    }

    private int ammo(ItemStack item, NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 0;
        }
        Integer value = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return value != null ? value : 0;
    }

    private void writeState(ItemStack item, GunDefinition gun, int mag, int reserve) {
        item.editMeta(meta -> {
            meta.displayName(MessageUtil.format(gun.displayName()));
            meta.lore(List.of(
                    MessageUtil.format("&7Ammo: &e" + mag + " &8/ &7" + reserve),
                    MessageUtil.format("&8[Atlas Zombies]")));
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(this.keys.gunId(), PersistentDataType.STRING, gun.id());
            pdc.set(this.keys.ammoMag(), PersistentDataType.INTEGER, mag);
            pdc.set(this.keys.ammoReserve(), PersistentDataType.INTEGER, reserve);
            if (meta instanceof Damageable damageable && gun.material().getMaxDurability() > 0) {
                double fraction = gun.magazine() > 0 ? (double) mag / gun.magazine() : 1.0;
                damageable.setDamage((int) ((1.0 - fraction) * gun.material().getMaxDurability()));
            }
        });
    }
}
