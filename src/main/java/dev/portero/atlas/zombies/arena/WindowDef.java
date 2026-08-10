package dev.portero.atlas.zombies.arena;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Definition of a reparable window barricade. Zombies assigned to a window spawn at
 * {@code spawn} (outside) and tear the {@code planks} region down block by block;
 * players rebuild it for points.
 *
 * @param id unique id within the arena
 * @param planks region holding the barricade blocks
 * @param spawn exterior spawn location for zombies of this window
 * @param material barricade material, captured from the region during setup
 */
public record WindowDef(String id, Cuboid planks, Location spawn, Material material) {

    public WindowDef {
        spawn = spawn.clone();
    }
}
