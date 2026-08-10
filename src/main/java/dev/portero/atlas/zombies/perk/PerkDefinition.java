package dev.portero.atlas.zombies.perk;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Config-backed definition of a drinkable perk. Gameplay effects are applied by
 * {@code PerkService} and the relevant hook points (gun service, revive logic).
 *
 * @param id unique id (config key), see {@link Perks} for the known ids
 * @param displayName legacy-formatted display name
 * @param price price in points
 * @param soloPrice price in a one-player game (Quick Revive style); defaults to price
 */
public record PerkDefinition(String id, String displayName, int price, int soloPrice) {

    /**
     * Parses a perk from its config section.
     *
     * @param id config key
     * @param section perk section
     * @return parsed definition
     */
    public static PerkDefinition fromYaml(String id, ConfigurationSection section) {
        int price = section.getInt("price", 1000);
        return new PerkDefinition(id, section.getString("displayName", id), price,
                section.getInt("soloPrice", price));
    }

    /**
     * Effective price for the given lobby size.
     *
     * @param players players currently in the game
     * @return solo price when alone, regular price otherwise
     */
    public int priceFor(int players) {
        return players <= 1 ? this.soloPrice : this.price;
    }
}
