package dev.portero.atlas.zombies.arena;

/**
 * Definition of a buyable door. Purchasing removes the region's blocks row by row.
 *
 * @param id unique id within the arena
 * @param region blocks that make up the door
 * @param price points required to open it
 */
public record DoorDef(String id, Cuboid region, int price) {
}
