package dev.portero.atlas.zombies.game;

import lombok.Getter;
import org.bukkit.Material;

/**
 * Dropped power-ups. Activation logic lives in {@link GameSession#activatePowerUp}.
 */
@Getter
public enum PowerUpType {
    MAX_AMMO("Max Ammo", Material.GUNPOWDER),
    INSTA_KILL("Insta-Kill", Material.SKELETON_SKULL),
    DOUBLE_POINTS("Double Points", Material.GOLD_INGOT),
    NUKE("Nuke", Material.TNT);

    private final String displayName;
    private final Material icon;

    PowerUpType(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
}
