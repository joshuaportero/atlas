package dev.portero.atlas.mechanic.impl;

import dev.portero.atlas.AtlasPlugin;
import dev.portero.atlas.mechanic.AbstractMechanic;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RespawnMechanic extends AbstractMechanic {

    private final Map<String, Location> deathLocation;

    public RespawnMechanic(AtlasPlugin plugin) {
        super(plugin);
        this.deathLocation = new HashMap<>();
    }

    @Override
    public String getName() {
        return "respawn";
    }

    @EventHandler
    public void onPlayerFakeDeath(EntityDamageEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);

            player.getWorld().spawn(player.getLocation(), Firework.class, firework -> {
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                        .withColor(Color.RED)
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .build());
                firework.setFireworkMeta(meta);
            });

            player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getDefaultValue());
            player.setFoodLevel(20);
            player.setSaturation(5);
            player.setExhaustion(0);
            player.setFireTicks(0);
            player.setGameMode(GameMode.SPECTATOR);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);

            this.deathLocation.put(player.getUniqueId().toString(), player.getLocation());

            new BukkitRunnable() {
                private int ticks = 0;

                @Override
                public void run() {
                    if (this.ticks % 20 == 0) {
                        player.sendTitle("§cYou Died!", "§7Respawning in "
                                + (5 - this.ticks / 20) + " seconds...", 20, 20, 20);
                    }
                    if (this.ticks >= 100) { // 5 seconds
                        player.setGameMode(GameMode.SURVIVAL);
                        Location respawnLocation = RespawnMechanic.this.deathLocation
                                .remove(player.getUniqueId().toString());
                        if (respawnLocation != null) {
                            player.teleport(respawnLocation);
                        }

                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                        cancel();
                    }
                    this.ticks += 20;
                }
            }.runTaskTimer(this.plugin, 0L, 20L);
        }
    }
}
