package dev.portero.atlas.zombies.game;

import dev.portero.atlas.util.MessageUtil;
import dev.portero.atlas.zombies.ZombiesModule;
import dev.portero.atlas.zombies.arena.MysteryBoxDef;
import dev.portero.atlas.zombies.combat.gun.GunDefinition;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Runtime state of one arena's mystery box: roll animation, gun pickup, and the
 * firework relocation. One instance per game session.
 */
public class MysteryBoxController {

    private static final int ROLL_STEPS = 26;
    private static final long ROLL_STEP_TICKS = 3L;
    private static final long PICKUP_TIMEOUT_TICKS = 240L;
    private static final double DISPLAY_HEIGHT = 1.2;

    private final ZombiesModule module;
    private final GameSession session;
    private final MysteryBoxDef definition;

    private int activeIndex;
    private boolean rolling;
    private UUID displayId;
    private String pendingGunId;
    private BukkitTask rollTask;
    private BukkitTask pickupTimeoutTask;

    public MysteryBoxController(ZombiesModule module, GameSession session, MysteryBoxDef definition) {
        this.module = module;
        this.session = session;
        this.definition = definition;
    }

    /**
     * Checks whether the given block is the box at its current location.
     *
     * @param block clicked block
     * @return true when it is the active mystery box
     */
    public boolean isBoxBlock(Block block) {
        Location active = this.definition.locations().get(this.activeIndex);
        return block.getWorld().getName().equals(this.session.getArena().getWorldName())
                && block.getX() == active.getBlockX()
                && block.getY() == active.getBlockY()
                && block.getZ() == active.getBlockZ();
    }

    public boolean isBoxDisplay(Entity entity) {
        return entity.getUniqueId().equals(this.displayId);
    }

    /**
     * Handles a click on the box (or its display): starts a roll, or hands out the
     * pending gun.
     *
     * @param player interacting player
     */
    public void onInteract(Player player) {
        if (this.pendingGunId != null) {
            this.takeGun(player);
            return;
        }
        if (this.rolling) {
            player.sendActionBar(MessageUtil.format("&cThe box is already rolling"));
            return;
        }
        if (!this.session.charge(player, this.definition.price())) {
            return;
        }
        this.startRoll(player);
    }

    /**
     * Cancels all tasks and removes the display entity (game end).
     */
    public void close() {
        if (this.rollTask != null) {
            this.rollTask.cancel();
        }
        if (this.pickupTimeoutTask != null) {
            this.pickupTimeoutTask.cancel();
        }
        this.removeDisplay();
    }

    private void startRoll(Player player) {
        World world = Bukkit.getWorld(this.session.getArena().getWorldName());
        if (world == null) {
            return;
        }
        this.rolling = true;
        Location displayLocation = this.activeLocation(world);
        ItemDisplay display = world.spawn(displayLocation, ItemDisplay.class, entity -> {
            entity.setItemStack(this.randomGunIcon());
            entity.setGlowing(false);
        });
        this.displayId = display.getUniqueId();

        this.rollTask = Bukkit.getScheduler().runTaskTimer(this.module.getPlugin(), new Runnable() {
            private int step;

            @Override
            public void run() {
                ItemDisplay entity = MysteryBoxController.this.display();
                if (entity == null) {
                    MysteryBoxController.this.finishRoll(player, null);
                    return;
                }
                if (++this.step >= ROLL_STEPS) {
                    MysteryBoxController.this.finishRoll(player, entity);
                    return;
                }
                entity.setItemStack(MysteryBoxController.this.randomGunIcon());
                world.playSound(displayLocation, Sound.UI_BUTTON_CLICK, 0.8f, 1.8f);
            }
        }, 0L, ROLL_STEP_TICKS);
    }

    private void finishRoll(Player player, ItemDisplay display) {
        if (this.rollTask != null) {
            this.rollTask.cancel();
            this.rollTask = null;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (random.nextDouble() < this.definition.moveChance() && this.definition.locations().size() > 1) {
            this.relocate();
            return;
        }

        Optional<GunDefinition> gun = this.module.getGunRegistry().randomBoxGun(random);
        if (display == null || gun.isEmpty()) {
            this.rolling = false;
            this.removeDisplay();
            return;
        }
        this.pendingGunId = gun.get().id();
        display.setItemStack(this.module.getGunService().createIcon(gun.get()));
        display.setGlowing(true);
        World world = display.getWorld();
        world.playSound(display.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        this.pickupTimeoutTask = Bukkit.getScheduler().runTaskLater(this.module.getPlugin(), () -> {
            MysteryBoxController.this.pendingGunId = null;
            MysteryBoxController.this.rolling = false;
            MysteryBoxController.this.removeDisplay();
        }, PICKUP_TIMEOUT_TICKS);
    }

    private void takeGun(Player player) {
        Optional<GunDefinition> gun = this.module.getGunRegistry().find(this.pendingGunId);
        if (gun.isEmpty()) {
            return;
        }
        if (this.pickupTimeoutTask != null) {
            this.pickupTimeoutTask.cancel();
            this.pickupTimeoutTask = null;
        }
        this.module.getGunService().giveGun(player, gun.get());
        player.sendActionBar(MessageUtil.format("&aYou got the " + gun.get().displayName()));
        this.pendingGunId = null;
        this.rolling = false;
        this.removeDisplay();
    }

    private void relocate() {
        int previous = this.activeIndex;
        while (this.activeIndex == previous) {
            this.activeIndex = ThreadLocalRandom.current().nextInt(this.definition.locations().size());
        }
        this.rolling = false;
        this.removeDisplay();
        World world = Bukkit.getWorld(this.session.getArena().getWorldName());
        if (world != null) {
            Location location = this.activeLocation(world);
            Firework firework = world.spawn(location, Firework.class, entity -> {
                FireworkMeta meta = entity.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                        .withColor(Color.RED).with(FireworkEffect.Type.BALL).build());
                entity.setFireworkMeta(meta);
            });
            firework.detonate();
            world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
        }
        this.session.broadcast("&dThe mystery box vanished... and reappeared elsewhere!");
    }

    private Location activeLocation(World world) {
        Location block = this.definition.locations().get(this.activeIndex);
        return new Location(world, block.getBlockX() + 0.5, block.getBlockY() + DISPLAY_HEIGHT, block.getBlockZ() + 0.5);
    }

    private ItemStack randomGunIcon() {
        return this.module.getGunRegistry().randomBoxGun(ThreadLocalRandom.current())
                .map(gun -> this.module.getGunService().createIcon(gun))
                .orElseGet(() -> new ItemStack(org.bukkit.Material.BARRIER));
    }

    private ItemDisplay display() {
        if (this.displayId == null) {
            return null;
        }
        Entity entity = Bukkit.getEntity(this.displayId);
        return entity instanceof ItemDisplay display ? display : null;
    }

    private void removeDisplay() {
        Entity entity = this.displayId != null ? Bukkit.getEntity(this.displayId) : null;
        if (entity != null) {
            entity.remove();
        }
        this.displayId = null;
    }
}
