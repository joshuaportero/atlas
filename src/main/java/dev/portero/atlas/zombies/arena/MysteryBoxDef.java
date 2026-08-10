package dev.portero.atlas.zombies.arena;

import org.bukkit.Location;

import java.util.List;

/**
 * Definition of the mystery box: a price, a chance the box relocates instead of paying
 * out, and the fixed locations it can sit at (the first is the initial one).
 *
 * @param price price per roll
 * @param moveChance probability the roll ends with the box moving away
 * @param locations box block locations; needs at least two for relocation to work
 */
public record MysteryBoxDef(int price, double moveChance, List<Location> locations) {

    public MysteryBoxDef {
        locations = List.copyOf(locations);
    }
}
