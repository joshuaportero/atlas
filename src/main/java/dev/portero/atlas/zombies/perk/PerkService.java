package dev.portero.atlas.zombies.perk;

import dev.portero.atlas.zombies.ZombiesModule;
import dev.portero.atlas.zombies.game.PlayerSession;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies and removes perk effects, and answers the perk-dependent multipliers the
 * gun/revive systems query. Perk ownership lives on the {@link PlayerSession}.
 */
public class PerkService {

    /** Extra hearts granted by Juggernog (on top of the vanilla 20). */
    public static final double JUGGERNOG_BONUS_HEALTH = 20.0;
    private static final double PLAYER_DEFAULT_HEALTH = 20.0;
    private static final double SPEED_COLA_RELOAD = 0.5;
    private static final double DOUBLE_TAP_INTERVAL = 0.75;
    private static final double DEADSHOT_SPREAD = 0.7;
    private static final double DEADSHOT_HEADSHOT = 1.25;

    private final ZombiesModule module;

    public PerkService(ZombiesModule module) {
        this.module = module;
    }

    /**
     * Checks whether the player owns a perk in their current game.
     *
     * @param player player to check
     * @param perkId perk id (see {@link Perks})
     * @return true when owned
     */
    public boolean has(Player player, String perkId) {
        return this.module.getGameManager().sessionOf(player.getUniqueId())
                .map(session -> {
                    PlayerSession playerSession = session.playerSession(player.getUniqueId());
                    return playerSession != null && playerSession.hasPerk(perkId);
                })
                .orElse(false);
    }

    /**
     * Grants a perk: stores ownership and applies its passive effect.
     *
     * @param player player receiving the perk
     * @param session their game player session
     * @param perk the perk definition
     */
    public void grant(Player player, PlayerSession session, PerkDefinition perk) {
        session.addPerk(perk.id());
        switch (perk.id()) {
            case Perks.JUGGERNOG -> {
                AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
                if (health != null) {
                    health.setBaseValue(PLAYER_DEFAULT_HEALTH + JUGGERNOG_BONUS_HEALTH);
                    player.setHealth(health.getValue());
                }
            }
            case Perks.STAMIN_UP -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                    PotionEffect.INFINITE_DURATION, 0, false, false, true));
            default -> {
                // Speed Cola, Double Tap, Deadshot and Quick Revive apply at their hook points.
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);
    }

    /**
     * Removes a single perk and its effect (Quick Revive consume on self-revive).
     *
     * @param player player losing the perk
     * @param session their game player session
     * @param perkId perk to remove
     */
    public void revoke(Player player, PlayerSession session, String perkId) {
        if (session.removePerk(perkId)) {
            this.unapply(player, perkId);
        }
    }

    /**
     * Removes all perks and their effects (bleed-out, game end).
     *
     * @param player player losing their perks
     * @param session their game player session
     */
    public void revokeAll(Player player, PlayerSession session) {
        for (String perkId : session.perks()) {
            this.unapply(player, perkId);
        }
        session.clearPerks();
    }

    public double reloadSpeedMultiplier(Player player) {
        return this.has(player, Perks.SPEED_COLA) ? SPEED_COLA_RELOAD : 1.0;
    }

    public double fireIntervalMultiplier(Player player) {
        return this.has(player, Perks.DOUBLE_TAP) ? DOUBLE_TAP_INTERVAL : 1.0;
    }

    public double spreadMultiplier(Player player) {
        return this.has(player, Perks.DEADSHOT) ? DEADSHOT_SPREAD : 1.0;
    }

    public double headshotMultiplierBonus(Player player) {
        return this.has(player, Perks.DEADSHOT) ? DEADSHOT_HEADSHOT : 1.0;
    }

    /**
     * Revive duration multiplier based on the reviver's perks.
     *
     * @param reviver the player performing the revive
     * @return 0.5 with Quick Revive, 1.0 otherwise
     */
    public double reviveSpeedMultiplier(Player reviver) {
        return this.has(reviver, Perks.QUICK_REVIVE) ? 0.5 : 1.0;
    }

    private void unapply(Player player, String perkId) {
        switch (perkId) {
            case Perks.JUGGERNOG -> {
                AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
                if (health != null) {
                    health.setBaseValue(PLAYER_DEFAULT_HEALTH);
                    if (player.getHealth() > health.getValue()) {
                        player.setHealth(health.getValue());
                    }
                }
            }
            case Perks.STAMIN_UP -> player.removePotionEffect(PotionEffectType.SPEED);
            default -> {
                // Passive perks need no teardown.
            }
        }
    }
}
