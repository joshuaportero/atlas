package dev.portero.atlas.mechanic.impl;

import dev.portero.atlas.AtlasPlugin;
import dev.portero.atlas.mechanic.AbstractMechanic;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class TntExplosionMechanic extends AbstractMechanic {

    private final Random random = new Random();

    public TntExplosionMechanic(AtlasPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "tnt-explosion";
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        if (!(event.getEntity() instanceof TNTPrimed)) {
            return;
        }

        List<Block> blocks = event.blockList();
        for (Block block : blocks) {
            if (block.getType().isSolid()) {
                block.getWorld().spawn(block.getLocation(), FallingBlock.class, fallingBlock -> {
                    fallingBlock.setBlockData(block.getBlockData());

                    double x = (this.random.nextDouble() - 0.5) * 1.5;
                    double y = this.random.nextDouble() * 1.5;
                    double z = (this.random.nextDouble() - 0.5) * 1.5;

                    fallingBlock.setVelocity(new Vector(x, y, z));
                    fallingBlock.setDropItem(false);
                });
            }
        }
    }
}
