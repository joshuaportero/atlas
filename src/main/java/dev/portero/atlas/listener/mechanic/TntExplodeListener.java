package dev.portero.atlas.listener.mechanic;

import lombok.RequiredArgsConstructor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class TntExplodeListener implements Listener {

    private final Plugin plugin;

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onTntExplode(EntityExplodeEvent event) {
        if (event.getEntity().getType() != EntityType.TNT) {
            return;
        }

        if (plugin.getConfig().getBoolean("mechanics.tnt_explode.enabled")) {
            Firework firework = this.spawnFirework(event.getLocation(), FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.RED)
                    .withFade(Color.RED)
                    .trail(true)
                    .flicker(true)
                    .build());

            firework.detonate();
        }

        if (plugin.getConfig().getBoolean("mechanics.tnt_explode.enabled")) {

            List<Block> blocks = new ArrayList<>(event.blockList());
            List<FallingBlock> fallingBlocks = new ArrayList<>();

            for (Block block : blocks) {
                Location location = block.getLocation();

                FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, block.getBlockData());

                fallingBlock.setBlockData(block.getBlockData());

                fallingBlock.setVelocity(this.getRandomVelocity());
                fallingBlock.setDropItem(false);
                fallingBlock.setInvulnerable(true);

                fallingBlocks.add(fallingBlock);
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (FallingBlock fallingBlock : fallingBlocks) {
                    fallingBlock.getLocation().getBlock().setType(Material.AIR);
                    fallingBlock.remove();
                }
            }, 5 * 20);
        }

    }

    private Vector getRandomVelocity() {
        Random random = new Random();
        double x = random.nextDouble() * 2 - 1;
        double y = random.nextDouble();
        double z = random.nextDouble() * 2 - 1;
        return new Vector(x, y, z).normalize().multiply(0.8);
    }

    private Firework spawnFirework(Location location, FireworkEffect effect) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffect(effect);
        meta.setPower(0);

        firework.setFireworkMeta(meta);
        return firework;
    }
}