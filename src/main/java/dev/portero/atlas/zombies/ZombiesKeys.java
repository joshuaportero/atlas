package dev.portero.atlas.zombies;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * Persistent data container keys used to tag zombies (and later other game entities)
 * so kills, cleanup and events can be attributed to the exact game.
 */
public final class ZombiesKeys {

    private final NamespacedKey game;
    private final NamespacedKey type;
    private final NamespacedKey gunId;
    private final NamespacedKey ammoMag;
    private final NamespacedKey ammoReserve;
    private final NamespacedKey wallBuy;
    private final NamespacedKey gunProjectile;

    public ZombiesKeys(Plugin plugin) {
        this.game = new NamespacedKey(plugin, "zombies_game");
        this.type = new NamespacedKey(plugin, "zombies_type");
        this.gunId = new NamespacedKey(plugin, "gun_id");
        this.ammoMag = new NamespacedKey(plugin, "ammo_mag");
        this.ammoReserve = new NamespacedKey(plugin, "ammo_reserve");
        this.wallBuy = new NamespacedKey(plugin, "wallbuy");
        this.gunProjectile = new NamespacedKey(plugin, "gun_projectile");
    }

    public NamespacedKey game() {
        return this.game;
    }

    public NamespacedKey type() {
        return this.type;
    }

    public NamespacedKey gunId() {
        return this.gunId;
    }

    public NamespacedKey ammoMag() {
        return this.ammoMag;
    }

    public NamespacedKey ammoReserve() {
        return this.ammoReserve;
    }

    public NamespacedKey wallBuy() {
        return this.wallBuy;
    }

    public NamespacedKey gunProjectile() {
        return this.gunProjectile;
    }
}
