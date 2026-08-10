package dev.portero.atlas.zombies.arena;

import org.bukkit.block.BlockFace;

/**
 * Definition of a wall-buy station: an invisible, glowing item frame on a wall block
 * selling a gun (or its ammo when already owned).
 *
 * @param id unique id within the arena
 * @param gunId id of the gun sold
 * @param pos wall block the frame hangs on
 * @param facing face of the block pointing toward the room
 * @param price price of the gun
 * @param ammoPrice price of a full reserve refill
 */
public record WallBuyDef(String id, String gunId, BlockPos pos, BlockFace facing, int price, int ammoPrice) {
}
