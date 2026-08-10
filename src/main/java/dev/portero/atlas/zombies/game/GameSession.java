package dev.portero.atlas.zombies.game;

import dev.portero.atlas.util.MessageUtil;
import dev.portero.atlas.zombies.ZombiesConfig;
import dev.portero.atlas.zombies.ZombiesModule;
import dev.portero.atlas.zombies.arena.Arena;
import dev.portero.atlas.zombies.arena.BlockPos;
import dev.portero.atlas.zombies.arena.DoorDef;
import dev.portero.atlas.zombies.arena.WallBuyDef;
import dev.portero.atlas.zombies.arena.WindowDef;
import dev.portero.atlas.zombies.combat.gun.GunDefinition;
import dev.portero.atlas.zombies.economy.PointsService;
import dev.portero.atlas.zombies.hud.HudService;
import dev.portero.atlas.zombies.perk.PerkDefinition;
import dev.portero.atlas.zombies.perk.Perks;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One live game of an arena. Owns players, round state, the zombie set, window/door
 * runtime state and the ticker. Disposable: when the game ends the session is closed
 * and the arena is restored from its snapshot.
 */
public class GameSession {

    /** Ticker steps per second; the ticker runs every {@link GameTicker#PERIOD_TICKS}. */
    private static final int STEPS_PER_SECOND = (int) (20L / GameTicker.PERIOD_TICKS);
    private static final long GAME_OVER_DELAY_TICKS = 100L;
    private static final double BREAK_REACH = 2.6;
    private static final long DOOR_ROW_DELAY_TICKS = 2L;

    private final ZombiesModule module;
    @Getter
    private final Arena arena;
    private final RoundDirector director;
    private final Map<UUID, PlayerSession> players = new LinkedHashMap<>();
    private final Set<UUID> zombies = new HashSet<>();
    private final Map<String, WindowState> windowStates = new LinkedHashMap<>();
    private final List<WindowDef> windowDefs;
    private final Map<UUID, String> zombieWindows = new HashMap<>();
    private final Set<String> openedDoors = new HashSet<>();
    private final Map<UUID, PendingPurchase> pendingPurchases = new HashMap<>();
    private final Map<UUID, Long> repairCooldowns = new HashMap<>();
    private final Set<UUID> wallFrameIds = new HashSet<>();
    private final MysteryBoxController mysteryBox;
    private final Map<UUID, BukkitTask> bleedoutTasks = new HashMap<>();
    private final Map<UUID, Integer> reviveProgress = new HashMap<>();
    private final Map<UUID, PowerUpDrop> powerUps = new HashMap<>();
    private boolean powerOn;
    private long instaKillUntilMillis;
    private long doublePointsUntilMillis;
    private long ticksAlive;
    private Set<UUID> reviveBusy = new HashSet<>();

    @Getter
    private GameState state = GameState.LOBBY;
    @Getter
    private int round;
    private int toSpawn;
    private int stepsLeft;
    private int spawnCooldownTicks;
    private int barricadeCooldownTicks;
    private BukkitTask task;

    public GameSession(ZombiesModule module, Arena arena) {
        this.module = module;
        this.arena = arena;
        this.director = new RoundDirector(module.getConfig());
        this.windowDefs = new ArrayList<>(arena.getWindows().values());
        arena.getWindows().forEach((id, def) -> this.windowStates.put(id, new WindowState(def)));
        this.mysteryBox = arena.getMysteryBox() != null
                ? new MysteryBoxController(module, this, arena.getMysteryBox())
                : null;
        this.powerOn = arena.getPowerSwitch() == null;
        this.spawnWallBuys();
    }

    /**
     * Adds a player to the lobby of this game and teleports them in.
     *
     * @param player joining player
     */
    public void addPlayer(Player player) {
        PlayerSession session = new PlayerSession(player.getUniqueId(), player.getLocation(),
                player.getGameMode(), this.arena.getSettings().startingPoints());
        this.players.put(player.getUniqueId(), session);
        player.teleport(this.arena.getLobbySpawn());
        this.broadcast("&e" + player.getName() + " &7joined the game (&e" + this.players.size()
                + "&7/&e" + this.arena.getSettings().maxPlayers() + "&7)");
        this.ensureTicker();

        if (this.state == GameState.LOBBY && this.players.size() >= this.arena.getSettings().minPlayers()) {
            this.beginCountdown();
        }
    }

    /**
     * Removes a player from the game, restoring their previous location and game mode.
     *
     * @param player leaving player
     */
    public void removePlayer(Player player) {
        PlayerSession session = this.players.remove(player.getUniqueId());
        if (session == null) {
            return;
        }
        BukkitTask bleedout = this.bleedoutTasks.remove(player.getUniqueId());
        if (bleedout != null) {
            bleedout.cancel();
        }
        this.restorePlayer(player, session);
        this.broadcast("&e" + player.getName() + " &7left the game");

        if (this.players.isEmpty()) {
            this.close();
            return;
        }
        if (this.state == GameState.STARTING && this.players.size() < this.arena.getSettings().minPlayers()) {
            this.state = GameState.LOBBY;
            this.broadcast("&cNot enough players — countdown stopped");
        }
        if ((this.state == GameState.IN_ROUND || this.state == GameState.INTERMISSION) && this.allDownedOrDead()) {
            this.gameOver();
        }
    }

    /**
     * Skips the lobby requirement and starts the countdown immediately (admin force start).
     */
    public void forceStart() {
        if (this.state == GameState.LOBBY && !this.players.isEmpty()) {
            this.beginCountdown();
        }
    }

    /**
     * Registers a zombie kill, awards points, maybe drops a power-up and checks for the
     * end of the round.
     *
     * @param zombieId uuid of the dead zombie
     * @param killer killing player, may be null
     * @param location death location (power-up drop spot)
     */
    public void onZombieDeath(UUID zombieId, Player killer, Location location) {
        this.zombies.remove(zombieId);
        this.zombieWindows.remove(zombieId);
        if (killer != null) {
            PlayerSession session = this.players.get(killer.getUniqueId());
            if (session != null) {
                session.addKill();
                this.awardPoints(session, this.config().getKillPoints());
            }
            if (ThreadLocalRandom.current().nextDouble() < this.config().getPowerupDropChance()) {
                this.spawnPowerUp(location);
            }
        }
        this.checkRoundEnd();
    }

    /**
     * Awards hit points for a damaging hit on one of this game's zombies.
     *
     * @param player hitting player
     */
    public void onZombieHit(Player player) {
        PlayerSession session = this.players.get(player.getUniqueId());
        if (session != null && session.isAlive()) {
            this.awardPoints(session, this.config().getHitPoints());
        }
    }

    /**
     * Marks a player as dead (post bleed-out). The game ends when nobody is standing.
     *
     * @param player dead player
     */
    public void onPlayerDeath(Player player) {
        PlayerSession session = this.players.get(player.getUniqueId());
        if (session != null) {
            session.setAlive(false);
            session.setDowned(false);
        }
        BukkitTask bleedout = this.bleedoutTasks.remove(player.getUniqueId());
        if (bleedout != null) {
            bleedout.cancel();
        }
        this.broadcast("&c" + player.getName() + " died");
        if (this.allDownedOrDead()) {
            this.gameOver();
        }
    }

    /**
     * Intercepts incoming damage: blocks damage for downed/dead players and converts
     * fatal damage into the downed state instead of dying.
     *
     * @param player damaged player
     * @param event the damage event
     */
    public void onPlayerDamage(Player player, EntityDamageEvent event) {
        PlayerSession session = this.players.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        if (!session.isAlive() || session.isDowned()
                || (this.state != GameState.IN_ROUND && this.state != GameState.INTERMISSION)) {
            event.setCancelled(true);
            return;
        }
        if (event.getFinalDamage() >= player.getHealth()) {
            event.setCancelled(true);
            this.down(player, session);
        }
    }

    /**
     * Perks of a player in this game.
     *
     * @param playerId player uuid
     * @return the player session, or null when not in this game
     */
    public PlayerSession playerSession(UUID playerId) {
        return this.players.get(playerId);
    }

    public boolean isInstaKillActive() {
        return System.currentTimeMillis() < this.instaKillUntilMillis;
    }

    public boolean isDoublePointsActive() {
        return System.currentTimeMillis() < this.doublePointsUntilMillis;
    }

    private void down(Player player, PlayerSession session) {
        session.setDowned(true);
        player.setHealth(1.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                PotionEffect.INFINITE_DURATION, 4, false, false, true));
        player.setPose(Pose.SWIMMING);
        player.showTitle(Title.title(MessageUtil.format("&4&lDOWNED"),
                MessageUtil.format("&7A teammate can revive you — or you bleed out")));
        this.broadcast("&c" + player.getName() + " is down!");
        this.playSound(Sound.ENTITY_WITHER_HURT, 0.6f);

        long bleedoutTicks = this.config().getBleedoutSeconds() * 20L;
        this.bleedoutTasks.put(player.getUniqueId(), Bukkit.getScheduler()
                .runTaskLater(this.module.getPlugin(), () -> this.bleedOut(player.getUniqueId()), bleedoutTicks));

        if (this.allDownedOrDead()) {
            if (this.players.size() == 1 && session.hasPerk(Perks.QUICK_REVIVE)) {
                this.broadcast("&aQuick Revive kicks in...");
                Bukkit.getScheduler().runTaskLater(this.module.getPlugin(),
                        () -> this.selfRevive(player.getUniqueId()),
                        this.config().getSelfReviveSeconds() * 20L);
            } else {
                this.gameOver();
            }
        }
    }

    private void reviveTick() {
        if (this.players.values().stream().noneMatch(PlayerSession::isDowned)) {
            this.reviveBusy = new HashSet<>();
            return;
        }
        double reachSquared = this.square(this.config().getReviveReach());
        int baseSteps = (int) (this.config().getReviveSeconds() * 20L / GameTicker.PERIOD_TICKS);
        Set<UUID> busy = new HashSet<>();

        for (Map.Entry<UUID, PlayerSession> entry : this.players.entrySet()) {
            if (!entry.getValue().isDowned()) {
                continue;
            }
            Player downed = Bukkit.getPlayer(entry.getKey());
            if (downed == null) {
                continue;
            }
            Player reviver = this.findReviver(downed, reachSquared);
            if (reviver == null) {
                this.reviveProgress.remove(entry.getKey());
                continue;
            }
            int progress = this.reviveProgress.getOrDefault(entry.getKey(), 0) + 1;
            this.reviveProgress.put(entry.getKey(), progress);
            busy.add(entry.getKey());
            busy.add(reviver.getUniqueId());
            int required = (int) Math.max(1, Math.round(baseSteps
                    * this.module.getPerkService().reviveSpeedMultiplier(reviver)));

            int percent = Math.min(100, progress * 100 / required);
            reviver.sendActionBar(MessageUtil.format("&aReviving &e" + downed.getName() + " &8(&a" + percent + "%&8)"));
            downed.sendActionBar(MessageUtil.format("&aBeing revived &8(&a" + percent + "%&8)"));
            if (progress >= required) {
                this.reviveProgress.remove(entry.getKey());
                this.finishRevive(downed, entry.getValue());
            }
        }
        this.reviveBusy = busy;
    }

    private Player findReviver(Player downed, double reachSquared) {
        Player best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Map.Entry<UUID, PlayerSession> entry : this.players.entrySet()) {
            PlayerSession candidate = entry.getValue();
            if (entry.getKey().equals(downed.getUniqueId()) || !candidate.isAlive() || candidate.isDowned()) {
                continue;
            }
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isSneaking()) {
                continue;
            }
            double distance = player.getLocation().distanceSquared(downed.getLocation());
            if (distance <= reachSquared && distance < bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        return best;
    }

    private void finishRevive(Player player, PlayerSession session) {
        BukkitTask bleedout = this.bleedoutTasks.remove(player.getUniqueId());
        if (bleedout != null) {
            bleedout.cancel();
        }
        session.setDowned(false);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.setPose(Pose.STANDING);
        player.setHealth(this.maxHealth(player));
        this.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
        this.broadcast("&a" + player.getName() + " was revived");
    }

    private void selfRevive(UUID playerId) {
        PlayerSession session = this.players.get(playerId);
        Player player = Bukkit.getPlayer(playerId);
        if (session == null || !session.isDowned() || player == null) {
            return;
        }
        this.module.getPerkService().revoke(player, session, Perks.QUICK_REVIVE);
        this.finishRevive(player, session);
    }

    private void bleedOut(UUID playerId) {
        PlayerSession session = this.players.get(playerId);
        if (session == null || !session.isDowned()) {
            return;
        }
        session.setDowned(false);
        session.setAlive(false);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            player.setPose(Pose.STANDING);
            this.module.getPerkService().revokeAll(player, session);
            player.setHealth(0.0); // real death: PlayerDeathEvent handles the rest
        }
        if (this.allDownedOrDead()) {
            this.gameOver();
        }
    }

    private void respawnDeadPlayers() {
        List<Location> spawns = this.arena.getPlayerSpawns();
        for (Map.Entry<UUID, PlayerSession> entry : this.players.entrySet()) {
            PlayerSession session = entry.getValue();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (session.isAlive() || player == null) {
                continue;
            }
            session.setAlive(true);
            session.setDowned(false);
            player.teleport(spawns.get(ThreadLocalRandom.current().nextInt(spawns.size())));
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(this.maxHealth(player));
            player.setFoodLevel(20);
            player.sendMessage(MessageUtil.format("&8[&cZA&8] &aBack in the fight!"));
        }
    }

    private boolean allDownedOrDead() {
        return this.players.values().stream()
                .noneMatch(session -> session.isAlive() && !session.isDowned());
    }

    private void awardPoints(PlayerSession session, int base) {
        PointsService.award(session, this.isDoublePointsActive() ? base * 2 : base);
    }

    private void powerUpTick() {
        if (this.powerUps.isEmpty()) {
            return;
        }
        double pickupSquared = this.square(this.config().getPowerupPickupReach());
        long despawnTicks = this.config().getPowerupDespawnSeconds() * 20L;
        for (Map.Entry<UUID, PowerUpDrop> entry : new HashMap<>(this.powerUps).entrySet()) {
            Entity entity = Bukkit.getEntity(entry.getKey());
            if (entity == null) {
                this.powerUps.remove(entry.getKey());
                continue;
            }
            if (this.ticksAlive - entry.getValue().spawnedAtTick() > despawnTicks) {
                entity.remove();
                this.powerUps.remove(entry.getKey());
                continue;
            }
            for (Map.Entry<UUID, PlayerSession> playerEntry : this.players.entrySet()) {
                PlayerSession session = playerEntry.getValue();
                Player player = Bukkit.getPlayer(playerEntry.getKey());
                if (player == null || !session.isAlive() || session.isDowned()
                        || player.getLocation().distanceSquared(entity.getLocation()) > pickupSquared) {
                    continue;
                }
                this.activatePowerUp(entry.getValue().type(), player);
                entity.remove();
                this.powerUps.remove(entry.getKey());
                break;
            }
        }
    }

    private void spawnPowerUp(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        PowerUpType[] types = PowerUpType.values();
        PowerUpType type = types[ThreadLocalRandom.current().nextInt(types.length)];
        ItemDisplay display = world.spawn(location.clone().add(0, 0.5, 0), ItemDisplay.class, entity -> {
            entity.setItemStack(new ItemStack(type.getIcon()));
            entity.setGlowing(true);
        });
        this.powerUps.put(display.getUniqueId(), new PowerUpDrop(type, this.ticksAlive));
    }

    private void activatePowerUp(PowerUpType type, Player player) {
        long durationMillis = this.config().getPowerupDurationSeconds() * 1000L;
        this.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1.2f);
        switch (type) {
            case MAX_AMMO -> {
                this.players.forEach((id, session) -> {
                    Player online = Bukkit.getPlayer(id);
                    if (online != null) {
                        this.module.getGunService().refillAllAmmo(online);
                    }
                });
                this.broadcast("&6" + player.getName() + " picked up &eMax Ammo&6!");
            }
            case INSTA_KILL -> {
                this.instaKillUntilMillis = System.currentTimeMillis() + durationMillis;
                this.broadcast("&6" + player.getName() + " picked up &cInsta-Kill&6!");
            }
            case DOUBLE_POINTS -> {
                this.doublePointsUntilMillis = System.currentTimeMillis() + durationMillis;
                this.broadcast("&6" + player.getName() + " picked up &eDouble Points&6!");
            }
            case NUKE -> {
                this.broadcast("&6" + player.getName() + " picked up &4&lNUKE&6!");
                for (UUID zombieId : new HashSet<>(this.zombies)) {
                    Entity entity = Bukkit.getEntity(zombieId);
                    if (entity instanceof LivingEntity zombie) {
                        zombie.damage(1000.0, player);
                    }
                }
            }
            default -> {
            }
        }
    }

    /**
     * Handles a right-click on a block inside the arena (door purchases; windows repair
     * via sneaking, see {@link #repairTick()}).
     *
     * @param player interacting player
     * @param block clicked block
     * @return true when the interaction was consumed and the event should cancel
     */
    public boolean onBlockInteract(Player player, Block block) {
        if (this.state != GameState.IN_ROUND && this.state != GameState.INTERMISSION) {
            return false;
        }
        PlayerSession session = this.players.get(player.getUniqueId());
        if (session == null || !session.isAlive() || session.isDowned()) {
            return false;
        }
        if (this.tryPowerSwitch(player, block)) {
            return true;
        }
        if (this.tryDoorPurchase(player, session, block)) {
            return true;
        }
        return this.tryDrinkPurchase(player, session, block);
    }

    /**
     * Respawn location for players who die mid-game: the arena lobby, as spectator,
     * until the game ends (round-based respawns arrive with the downed/revive milestone).
     *
     * @param player respawning player
     * @return where they should respawn
     */
    public Location respawnLocation(Player player) {
        Bukkit.getScheduler().runTaskLater(this.module.getPlugin(), () -> {
            if (this.players.containsKey(player.getUniqueId())) {
                player.setGameMode(GameMode.SPECTATOR);
            }
        }, 1L);
        return this.arena.getLobbySpawn();
    }

    public boolean isPlaying(UUID playerId) {
        return this.players.containsKey(playerId);
    }

    public boolean hasZombie(UUID entityId) {
        return this.zombies.contains(entityId);
    }

    /**
     * Whether the player can currently fight (alive, game running). Gun fire is gated on this.
     *
     * @param player player to check
     * @return true when the player may shoot
     */
    public boolean canFight(Player player) {
        PlayerSession session = this.players.get(player.getUniqueId());
        return session != null && session.isAlive()
                && (this.state == GameState.IN_ROUND || this.state == GameState.INTERMISSION);
    }

    /**
     * Charges a player points for a purchase, messaging them on failure.
     *
     * @param player buyer
     * @param price price in points
     * @return true when charged
     */
    public boolean charge(Player player, int price) {
        PlayerSession session = this.players.get(player.getUniqueId());
        if (session == null || !session.isAlive() || session.isDowned()) {
            return false;
        }
        if (!PointsService.charge(session, price)) {
            player.sendActionBar(MessageUtil.format("&cNot enough points (&e" + price + "&c)"));
            return false;
        }
        return true;
    }

    /**
     * Gun id of the wall-buy frame, when the entity is one of this game's wall-buys.
     *
     * @param entity clicked entity
     * @return gun id or null
     */
    public String wallBuyGunOf(Entity entity) {
        if (!this.wallFrameIds.contains(entity.getUniqueId())) {
            return null;
        }
        return entity.getPersistentDataContainer()
                .get(this.module.getKeys().wallBuy(), PersistentDataType.STRING);
    }

    /**
     * Handles a click on a wall-buy frame: buys the gun, or its ammo when already owned.
     *
     * @param player buyer
     * @param gunId gun id from the frame
     */
    public void purchaseWallBuy(Player player, String gunId) {
        if (this.state != GameState.IN_ROUND && this.state != GameState.INTERMISSION) {
            return;
        }
        WallBuyDef wallBuy = this.arena.getWallBuys().values().stream()
                .filter(def -> def.gunId().equals(gunId)).findFirst().orElse(null);
        Optional<GunDefinition> gun = this.module.getGunRegistry().find(gunId);
        if (wallBuy == null || gun.isEmpty()) {
            return;
        }
        if (this.module.getGunService().ownsGun(player, gunId)) {
            if (this.charge(player, wallBuy.ammoPrice()) && this.module.getGunService().refillAmmo(player, gunId)) {
                player.sendActionBar(MessageUtil.format("&aAmmo refilled for " + gun.get().displayName()));
                this.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1.0f);
            }
            return;
        }
        if (this.charge(player, wallBuy.price())) {
            this.module.getGunService().giveGun(player, gun.get());
            player.sendActionBar(MessageUtil.format("&aBought " + gun.get().displayName()));
            this.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.5f);
        }
    }

    public boolean isBoxBlock(Block block) {
        return this.mysteryBox != null && this.mysteryBox.isBoxBlock(block);
    }

    public boolean isBoxDisplay(Entity entity) {
        return this.mysteryBox != null && this.mysteryBox.isBoxDisplay(entity);
    }

    /**
     * Forwards a click on the mystery box block or its display to the controller.
     *
     * @param player interacting player
     */
    public void interactBox(Player player) {
        if (this.mysteryBox == null || this.players.get(player.getUniqueId()) == null) {
            return;
        }
        if (this.state != GameState.IN_ROUND && this.state != GameState.INTERMISSION) {
            return;
        }
        if (!this.powerOn) {
            player.sendActionBar(MessageUtil.format("&cThe power is off! Turn on the power switch first"));
            return;
        }
        this.mysteryBox.onInteract(player);
    }

    /**
     * Snapshot of the player ids currently in this game (used by the manager on dispose).
     *
     * @return copy of the player id set
     */
    public Set<UUID> playerIds() {
        return new HashSet<>(this.players.keySet());
    }

    public boolean isJoinable() {
        return (this.state == GameState.LOBBY || this.state == GameState.STARTING)
                && this.players.size() < this.arena.getSettings().maxPlayers();
    }

    /**
     * Shuts the game down: cancels ticking, purges game entities, returns players and
     * hands the arena back to the manager for world restoration.
     */
    public void close() {
        if (this.task != null) {
            this.task.cancel();
        }
        this.state = GameState.RESETTING;
        if (this.mysteryBox != null) {
            this.mysteryBox.close();
        }
        this.bleedoutTasks.values().forEach(BukkitTask::cancel);
        this.bleedoutTasks.clear();
        this.powerUps.keySet().forEach(id -> {
            Entity entity = Bukkit.getEntity(id);
            if (entity != null) {
                entity.remove();
            }
        });
        this.powerUps.clear();
        this.wallFrameIds.forEach(id -> {
            Entity frame = Bukkit.getEntity(id);
            if (frame != null) {
                frame.remove();
            }
        });
        this.wallFrameIds.clear();
        this.purgeEntities();
        this.players.forEach((id, session) -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                this.restorePlayer(player, session);
            }
        });
        this.module.getGameManager().dispose(this);
        this.players.clear();
        this.zombies.clear();
        this.zombieWindows.clear();
    }

    void tick() {
        this.ticksAlive++;
        switch (this.state) {
            case STARTING -> {
                this.announceCountdown();
                if (--this.stepsLeft <= 0) {
                    this.beginRound(1);
                }
            }
            case INTERMISSION -> {
                if (--this.stepsLeft <= 0) {
                    this.beginRound(this.round + 1);
                }
                this.repairTick();
                this.reviveTick();
                this.powerUpTick();
            }
            case IN_ROUND -> {
                this.spawnTick();
                this.barricadeTick();
                this.repairTick();
                this.reviveTick();
                this.powerUpTick();
            }
            default -> {
                // LOBBY, GAME_OVER and RESETTING are event-driven only.
            }
        }
        this.refreshHud();
    }

    private void beginCountdown() {
        this.state = GameState.STARTING;
        this.stepsLeft = this.config().getCountdownSeconds() * STEPS_PER_SECOND;
        this.broadcast("&aGame starting in " + this.config().getCountdownSeconds() + "s...");
    }

    private void beginRound(int round) {
        this.round = round;
        this.toSpawn = this.director.zombiesForRound(round, this.players.size());
        this.spawnCooldownTicks = 0;
        this.state = GameState.IN_ROUND;
        this.showTitle("&4Round " + round, "");
        this.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f);
        this.respawnDeadPlayers();
        if (round == 1) {
            this.module.getGunRegistry().starting().ifPresent(gun -> this.players.forEach((id, session) -> {
                Player player = Bukkit.getPlayer(id);
                if (player != null) {
                    this.module.getGunService().giveGun(player, gun);
                }
            }));
            if (!this.windowStates.isEmpty()) {
                this.broadcast("&7Sneak next to a damaged window to rebuild its barricade");
            }
        }
    }

    private void spawnTick() {
        int intervalTicks = this.director.spawnIntervalTicks(this.round);
        if (this.toSpawn > 0 && this.zombies.size() < this.config().getMaxAlive()) {
            this.spawnCooldownTicks += (int) GameTicker.PERIOD_TICKS;
            if (this.spawnCooldownTicks >= intervalTicks) {
                this.spawnCooldownTicks = 0;
                this.spawnZombie();
            }
        }
        this.checkRoundEnd();
    }

    private void spawnZombie() {
        WindowDef window = this.randomWindow();
        Player target = this.nearestTarget();
        Zombie zombie = window != null
                ? this.module.getZombieService().spawnAt(this.arena, window.spawn(), this.round, this.director, target)
                : this.module.getZombieService().spawn(this.arena, this.round, this.director, target);
        if (zombie != null) {
            this.zombies.add(zombie.getUniqueId());
            if (window != null) {
                this.zombieWindows.put(zombie.getUniqueId(), window.id());
            }
            this.toSpawn--;
        }
    }

    /**
     * Zombies pressing against their assigned window tear one plank per window per
     * configured interval. Movement stays vanilla: zombies crowd the barricade while
     * pathing to players, we only handle the tearing.
     */
    private void barricadeTick() {
        if (this.windowStates.isEmpty() || this.zombies.isEmpty()) {
            return;
        }
        this.barricadeCooldownTicks += (int) GameTicker.PERIOD_TICKS;
        if (this.barricadeCooldownTicks < this.config().getWindowBreakIntervalTicks()) {
            return;
        }
        this.barricadeCooldownTicks = 0;

        World world = Bukkit.getWorld(this.arena.getWorldName());
        if (world == null) {
            return;
        }
        Set<String> tornThisTick = new HashSet<>();
        for (UUID zombieId : this.zombies) {
            String windowId = this.zombieWindows.get(zombieId);
            if (windowId == null || tornThisTick.contains(windowId)) {
                continue;
            }
            WindowState window = this.windowStates.get(windowId);
            Entity zombie = Bukkit.getEntity(zombieId);
            if (window == null || window.isOpen() || zombie == null) {
                continue;
            }
            Optional<BlockPos> target = window.nearestIntact(zombie.getLocation());
            if (target.isEmpty() || target.get().distanceSquaredTo(zombie.getLocation()) > BREAK_REACH * BREAK_REACH) {
                continue;
            }
            BlockPos pos = target.get();
            world.getBlockAt(pos.x(), pos.y(), pos.z()).setType(Material.AIR);
            world.playSound(pos.center(world), Sound.BLOCK_WOOD_BREAK, 1.0f, 0.8f);
            window.markBroken(pos);
            tornThisTick.add(windowId);
        }
    }

    private boolean tryDoorPurchase(Player player, PlayerSession session, Block block) {
        DoorDef door = null;
        for (DoorDef def : this.arena.getDoors().values()) {
            if (def.region().contains(block.getLocation())) {
                door = def;
                break;
            }
        }
        if (door == null) {
            return false;
        }
        if (this.openedDoors.contains(door.id())) {
            return true;
        }

        long now = System.currentTimeMillis();
        PendingPurchase pending = this.pendingPurchases.get(player.getUniqueId());
        if (pending != null && pending.doorId().equals(door.id()) && pending.expiresAtMillis() >= now) {
            this.pendingPurchases.remove(player.getUniqueId());
            if (!PointsService.charge(session, door.price())) {
                player.sendActionBar(MessageUtil.format("&cNot enough points (&e" + door.price() + "&c)"));
                return true;
            }
            this.openDoor(door);
            this.broadcast("&e" + player.getName() + " &7opened a door &8(-" + door.price() + ")");
            return true;
        }

        this.pendingPurchases.put(player.getUniqueId(),
                new PendingPurchase(door.id(), now + this.config().getDoorConfirmMillis()));
        player.sendActionBar(MessageUtil.format("&eClick again to open &8(&6" + door.price() + " points&8)"));
        this.playSound(player, Sound.UI_BUTTON_CLICK);
        return true;
    }

    private void openDoor(DoorDef door) {
        this.openedDoors.add(door.id());
        World world = Bukkit.getWorld(this.arena.getWorldName());
        if (world == null) {
            return;
        }
        Location center = new Location(world,
                (door.region().minX() + door.region().maxX()) / 2.0 + 0.5,
                (door.region().minY() + door.region().maxY()) / 2.0 + 0.5,
                (door.region().minZ() + door.region().maxZ()) / 2.0 + 0.5);
        world.playSound(center, Sound.BLOCK_PISTON_EXTEND, 1.0f, 0.6f);
        for (int y = door.region().minY(); y <= door.region().maxY(); y++) {
            final int row = y;
            long delay = (long) (y - door.region().minY()) * DOOR_ROW_DELAY_TICKS;
            Bukkit.getScheduler().runTaskLater(this.module.getPlugin(), () -> {
                for (int x = door.region().minX(); x <= door.region().maxX(); x++) {
                    for (int z = door.region().minZ(); z <= door.region().maxZ(); z++) {
                        world.getBlockAt(x, row, z).setType(Material.AIR);
                    }
                }
                world.playSound(center, Sound.BLOCK_STONE_BREAK, 0.8f, 0.9f);
            }, delay);
        }
    }

    /**
     * Sneak-to-repair: players crouching next to a damaged window rebuild it plank by
     * plank on the configured interval. Repair stops when the player stands up, moves
     * out of range, or a zombie is at the window.
     */
    private void repairTick() {
        if (this.windowStates.isEmpty()) {
            return;
        }
        World world = Bukkit.getWorld(this.arena.getWorldName());
        if (world == null) {
            return;
        }

        double zombieBlockReachSquared = this.square(this.config().getWindowZombieBlockReach());
        Set<String> blockedWindows = new HashSet<>();
        for (UUID zombieId : this.zombies) {
            Entity zombie = Bukkit.getEntity(zombieId);
            if (zombie == null) {
                continue;
            }
            for (WindowState window : this.windowStates.values()) {
                if (!blockedWindows.contains(window.definition().id())
                        && window.distanceSquaredTo(zombie.getLocation()) <= zombieBlockReachSquared) {
                    blockedWindows.add(window.definition().id());
                }
            }
        }

        double reachSquared = this.square(this.config().getWindowRepairReach());
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, PlayerSession> entry : this.players.entrySet()) {
            PlayerSession session = entry.getValue();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !session.isAlive() || session.isDowned() || !player.isSneaking()) {
                continue;
            }
            Long lastRepair = this.repairCooldowns.get(entry.getKey());
            if (lastRepair != null && now - lastRepair < this.config().getWindowRepairCooldownMillis()) {
                continue;
            }

            WindowState nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (WindowState window : this.windowStates.values()) {
                if (window.isIntact() || blockedWindows.contains(window.definition().id())) {
                    continue;
                }
                double distance = window.distanceSquaredTo(player.getLocation());
                if (distance <= reachSquared && distance < nearestDistance) {
                    nearest = window;
                    nearestDistance = distance;
                }
            }
            if (nearest == null) {
                continue;
            }

            Optional<BlockPos> next = nearest.nextRepair();
            if (next.isEmpty()) {
                continue;
            }
            this.repairCooldowns.put(entry.getKey(), now);
            BlockPos pos = next.get();
            world.getBlockAt(pos.x(), pos.y(), pos.z()).setType(nearest.definition().material());
            world.playSound(pos.center(world), Sound.BLOCK_WOOD_PLACE, 1.0f, 1.0f);
            nearest.markRepaired(pos);
            PointsService.award(session, this.config().getRepairPoints());
        }
    }

    private double square(double value) {
        return value * value;
    }

    private void checkRoundEnd() {
        if (this.state != GameState.IN_ROUND || this.toSpawn > 0 || !this.zombies.isEmpty()) {
            return;
        }
        this.state = GameState.INTERMISSION;
        this.stepsLeft = this.config().getIntermissionSeconds() * STEPS_PER_SECOND;
        this.broadcast("&aRound " + this.round + " cleared!");
    }

    private void gameOver() {
        this.state = GameState.GAME_OVER;
        this.showTitle("&4GAME OVER", "&7You survived &e" + this.round + " &7rounds");
        this.broadcast("&4Game over — survived " + this.round + " round(s)");
        Bukkit.getScheduler().runTaskLater(this.module.getPlugin(), this::close, GAME_OVER_DELAY_TICKS);
    }

    private void purgeEntities() {
        World world = Bukkit.getWorld(this.arena.getWorldName());
        if (world == null) {
            return;
        }
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player) {
                continue;
            }
            if (this.zombies.contains(entity.getUniqueId()) || this.arena.getRegion().contains(entity.getLocation())) {
                entity.remove();
            }
        }
        this.zombies.clear();
    }

    private void restorePlayer(Player player, PlayerSession session) {
        this.module.getPerkService().revokeAll(player, session);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.setPose(Pose.STANDING);
        player.teleport(session.getReturnLocation());
        player.setGameMode(session.getReturnGameMode());
        player.setFireTicks(0);
        player.setHealth(Math.min(20.0, this.maxHealth(player)));
        player.setFoodLevel(20);
    }

    private double maxHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        return attribute != null ? attribute.getValue() : 20.0;
    }

    private WindowDef randomWindow() {
        if (this.windowDefs.isEmpty()) {
            return null;
        }
        return this.windowDefs.get(ThreadLocalRandom.current().nextInt(this.windowDefs.size()));
    }

    private Player nearestTarget() {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Map.Entry<UUID, PlayerSession> entry : this.players.entrySet()) {
            if (!entry.getValue().isAlive()) {
                continue;
            }
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !this.arena.getRegion().contains(player.getLocation())) {
                continue;
            }
            double distance = player.getLocation().distanceSquared(this.arena.getLobbySpawn());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private boolean tryPowerSwitch(Player player, Block block) {
        BlockPos powerSwitch = this.arena.getPowerSwitch();
        if (powerSwitch == null || !this.matches(powerSwitch, block)) {
            return false;
        }
        if (this.powerOn) {
            player.sendActionBar(MessageUtil.format("&7The power is already on"));
            return true;
        }
        this.powerOn = true;
        this.broadcast("&eThe power hums to life!");
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        return true;
    }

    private boolean tryDrinkPurchase(Player player, PlayerSession session, Block block) {
        String perkId = null;
        for (Map.Entry<String, BlockPos> entry : this.arena.getDrinks().entrySet()) {
            if (this.matches(entry.getValue(), block)) {
                perkId = entry.getKey();
                break;
            }
        }
        if (perkId == null) {
            return false;
        }
        if (!this.powerOn) {
            player.sendActionBar(MessageUtil.format("&cThe power is off! Turn on the power switch first"));
            return true;
        }
        Optional<PerkDefinition> perk = this.module.getPerkRegistry().find(perkId);
        if (perk.isEmpty()) {
            return true;
        }
        if (session.hasPerk(perkId)) {
            player.sendActionBar(MessageUtil.format("&cYou already own " + perk.get().displayName()));
            return true;
        }
        if (session.perks().size() >= this.module.getPerkRegistry().getMaxPerks()) {
            player.sendActionBar(MessageUtil.format("&cPerk limit reached (&e"
                    + this.module.getPerkRegistry().getMaxPerks() + "&c)"));
            return true;
        }
        if (!this.charge(player, perk.get().priceFor(this.players.size()))) {
            return true;
        }
        this.module.getPerkService().grant(player, session, perk.get());
        player.sendActionBar(MessageUtil.format("&aDrank " + perk.get().displayName()));
        return true;
    }

    private boolean matches(BlockPos pos, Block block) {
        return block.getWorld().getName().equals(this.arena.getWorldName())
                && block.getX() == pos.x() && block.getY() == pos.y() && block.getZ() == pos.z();
    }

    private void announceCountdown() {
        if (this.stepsLeft % STEPS_PER_SECOND == 0 && this.stepsLeft > 0) {
            int seconds = this.stepsLeft / STEPS_PER_SECOND;
            this.showTitle("&e" + seconds, "");
            this.playSound(Sound.UI_BUTTON_CLICK, 1.0f);
        }
    }

    private void refreshHud() {
        if (this.state != GameState.IN_ROUND && this.state != GameState.INTERMISSION) {
            return;
        }
        int zombiesLeft = this.zombies.size() + this.toSpawn;
        this.players.forEach((id, session) -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null && !this.reviveBusy.contains(id)) {
                HudService.send(player, session, this.round, zombiesLeft);
            }
        });
    }

    private void spawnWallBuys() {
        World world = Bukkit.getWorld(this.arena.getWorldName());
        if (world == null) {
            return;
        }
        for (WallBuyDef def : this.arena.getWallBuys().values()) {
            Optional<GunDefinition> gun = this.module.getGunRegistry().find(def.gunId());
            if (gun.isEmpty()) {
                this.module.getPlugin().getLogger()
                        .warning("Wall-buy '" + def.id() + "' references unknown gun '" + def.gunId() + "'");
                continue;
            }
            Block block = world.getBlockAt(def.pos().x(), def.pos().y(), def.pos().z());
            Location frameLocation = block.getRelative(def.facing()).getLocation();
            ItemFrame frame = world.spawn(frameLocation, ItemFrame.class, entity -> {
                entity.setFacingDirection(def.facing(), true);
                entity.setItem(this.module.getGunService().createIcon(gun.get()), false);
                entity.setVisible(false);
                entity.setGlowing(true);
                entity.setFixed(true);
                entity.setInvulnerable(true);
                entity.getPersistentDataContainer()
                        .set(this.module.getKeys().wallBuy(), PersistentDataType.STRING, def.gunId());
            });
            this.wallFrameIds.add(frame.getUniqueId());
        }
    }

    private void showTitle(String title, String subtitle) {
        Title display = Title.title(MessageUtil.format(title), MessageUtil.format(subtitle));
        this.players.forEach((id, session) -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                player.showTitle(display);
            }
        });
    }

    private void playSound(Sound sound, float pitch) {
        this.players.forEach((id, session) -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                this.playSound(player, sound, pitch);
            }
        });
    }

    private void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    private void playSound(Player player, Sound sound, float pitch) {
        player.playSound(player.getLocation(), sound, 1.0f, pitch);
    }

    void broadcast(String message) {
        Component component = MessageUtil.format("&8[&cZA&8] " + message);
        this.players.forEach((id, session) -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                player.sendMessage(component);
            }
        });
    }

    private ZombiesConfig config() {
        return this.module.getConfig();
    }

    private void ensureTicker() {
        if (this.task == null) {
            this.task = new GameTicker(this)
                    .runTaskTimer(this.module.getPlugin(), GameTicker.PERIOD_TICKS, GameTicker.PERIOD_TICKS);
        }
    }

    private record PendingPurchase(String doorId, long expiresAtMillis) {
    }

    private record PowerUpDrop(PowerUpType type, long spawnedAtTick) {
    }
}
