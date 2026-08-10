package dev.portero.atlas.zombies.listener;

import dev.portero.atlas.zombies.ZombiesModule;
import dev.portero.atlas.zombies.combat.gun.GunDefinition;
import dev.portero.atlas.zombies.game.PlayerSession;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Thin Bukkit listener layer for the zombies module. All logic is delegated to the
 * matching service or {@link dev.portero.atlas.zombies.game.GameSession} method.
 */
public class GameListener implements Listener {

    private final ZombiesModule module;

    public GameListener(ZombiesModule module) {
        this.module = module;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.module.getGunService().cancelReload(event.getPlayer().getUniqueId());
        this.module.getGameManager().leave(event.getPlayer());
    }

    @EventHandler
    public void onZombieDeath(EntityDeathEvent event) {
        String arenaId = event.getEntity().getPersistentDataContainer()
                .get(this.module.getKeys().game(), PersistentDataType.STRING);
        if (arenaId == null) {
            return;
        }
        event.getDrops().clear();
        event.setDroppedExp(0);
        this.module.getGameManager().sessionOfArena(arenaId)
                .ifPresent(session -> session.onZombieDeath(event.getEntity().getUniqueId(),
                        event.getEntity().getKiller(), event.getEntity().getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            this.module.getGameManager().sessionOf(player.getUniqueId())
                    .ifPresent(session -> session.onPlayerDamage(player, event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }
        String arenaId = event.getEntity().getPersistentDataContainer()
                .get(this.module.getKeys().game(), PersistentDataType.STRING);
        if (arenaId == null) {
            return;
        }
        this.module.getGameManager().sessionOfArena(arenaId).ifPresent(session -> {
            PlayerSession target = session.playerSession(player.getUniqueId());
            if (target != null && (target.isDowned() || !target.isAlive())) {
                // Zombies lose interest in downed and dead players.
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            String arenaId = event.getEntity().getPersistentDataContainer()
                    .get(this.module.getKeys().game(), PersistentDataType.STRING);
            if (arenaId != null) {
                this.module.getGameManager().sessionOfArena(arenaId)
                        .ifPresent(session -> {
                            if (session.isInstaKillActive()) {
                                event.setDamage(1000.0);
                            }
                            session.onZombieHit(player);
                        });
            }
        }

        // No friendly fire inside a game.
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player damager) {
            boolean sameGame = this.module.getGameManager().sessionOf(victim.getUniqueId())
                    .map(session -> session.isPlaying(damager.getUniqueId()))
                    .orElse(false);
            if (sameGame) {
                event.setCancelled(true);
            }
        }

        // Wall-buy frames are invulnerable anyway; belt and braces for creative punches.
        if (event.getEntity().getPersistentDataContainer()
                .has(this.module.getKeys().wallBuy(), PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.module.getGameManager().sessionOf(event.getEntity().getUniqueId())
                .ifPresent(session -> {
                    event.setKeepInventory(true);
                    event.getDrops().clear();
                    event.setKeepLevel(true);
                    event.setDroppedExp(0);
                    session.onPlayerDeath(event.getEntity());
                });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        this.module.getGameManager().sessionOf(event.getPlayer().getUniqueId())
                .ifPresent(session -> event.setRespawnLocation(session.respawnLocation(event.getPlayer())));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        boolean rightClick = event.getAction() == Action.RIGHT_CLICK_BLOCK
                || event.getAction() == Action.RIGHT_CLICK_AIR;
        if (!rightClick) {
            return;
        }
        Player player = event.getPlayer();
        this.module.getGameManager().sessionOf(player.getUniqueId()).ifPresent(session -> {
            Block block = event.getClickedBlock();

            // Interactables win over firing: pointing at a door or the box means "use".
            if (block != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (session.onBlockInteract(player, block)) {
                    event.setCancelled(true);
                    return;
                }
                if (session.isBoxBlock(block)) {
                    event.setCancelled(true);
                    session.interactBox(player);
                    return;
                }
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            GunDefinition gun = this.module.getGunService().gunOf(item);
            if (gun == null || !session.canFight(player)) {
                return;
            }
            if (gun.aimDownSights() && !player.isSneaking()) {
                return; // vanilla spyglass scope
            }
            event.setCancelled(true);
            this.module.getGunService().fire(player, item, gun, session);
        });
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        this.module.getGameManager().sessionOf(player.getUniqueId()).ifPresent(session -> {
            String wallBuyGun = session.wallBuyGunOf(event.getRightClicked());
            if (wallBuyGun != null) {
                event.setCancelled(true);
                session.purchaseWallBuy(player, wallBuyGun);
                return;
            }
            if (session.isBoxDisplay(event.getRightClicked())) {
                event.setCancelled(true);
                session.interactBox(player);
            }
        });
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        ItemStack item = event.getMainHandItem();
        GunDefinition gun = this.module.getGunService().gunOf(item);
        if (gun == null) {
            return;
        }
        event.setCancelled(true);
        this.module.getGameManager().sessionOf(event.getPlayer().getUniqueId()).ifPresent(session -> {
            if (session.canFight(event.getPlayer())) {
                this.module.getGunService().startReload(event.getPlayer(), item, gun);
            }
        });
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        this.module.getGunService().cancelReload(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (this.module.getGunService().gunOf(event.getItemDrop().getItemStack()) != null
                && this.module.getGameManager().sessionOf(event.getPlayer().getUniqueId()).isPresent()) {
            event.setCancelled(true);
            this.module.getGunService().cancelReload(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        // The durability bar is the ammo gauge; guns never wear out.
        if (this.module.getGunService().gunOf(event.getItem()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        String gunId = this.module.getGunService().projectileGunId(projectile);
        if (gunId == null || !(projectile.getShooter() instanceof Player player)) {
            return;
        }
        GunDefinition gun = this.module.getGunRegistry().find(gunId).orElse(null);
        if (gun == null) {
            return;
        }
        this.module.getGameManager().sessionOf(player.getUniqueId()).ifPresent(session -> {
            Location location = projectile.getLocation();
            World world = projectile.getWorld();
            world.spawnParticle(Particle.EXPLOSION_EMITTER, location, 2);
            world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
            for (Entity entity : world.getNearbyEntities(location, gun.aoeRadius(), gun.aoeRadius(), gun.aoeRadius())) {
                if (entity instanceof LivingEntity target && session.hasZombie(target.getUniqueId())) {
                    target.damage(gun.damage(), player);
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getEntity().getPersistentDataContainer()
                .has(this.module.getKeys().wallBuy(), PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (this.isProtectedGameBlock(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (this.isProtectedGameBlock(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private boolean isProtectedGameBlock(Player player, Block block) {
        return this.module.getGameManager().sessionOf(player.getUniqueId())
                .map(session -> session.getArena().getRegion().contains(block.getLocation()))
                .orElse(false);
    }
}
