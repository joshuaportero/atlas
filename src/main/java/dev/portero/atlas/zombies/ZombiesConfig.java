package dev.portero.atlas.zombies;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Global zombies tuning loaded from {@code zombies/config.yml} in the plugin data folder.
 */
@Getter
public class ZombiesConfig {

    private static final String FILE_NAME = "zombies" + File.separator + "config.yml";

    private final Plugin plugin;

    private int countdownSeconds;
    private int intermissionSeconds;
    private int restoreChunksPerTick;
    private int killPoints;
    private int hitPoints;
    private int repairPoints;
    private int windowBreakIntervalTicks;
    private long windowRepairCooldownMillis;
    private double windowRepairReach;
    private double windowZombieBlockReach;
    private long doorConfirmMillis;
    private int bleedoutSeconds;
    private int reviveSeconds;
    private int selfReviveSeconds;
    private double reviveReach;
    private double powerupDropChance;
    private int powerupDurationSeconds;
    private int powerupDespawnSeconds;
    private double powerupPickupReach;
    private int baseZombies;
    private int zombiesPerRound;
    private int zombiesPerExtraPlayer;
    private int maxAlive;
    private int spawnIntervalTicks;
    private double healthMultiplierPerRound;
    private double baseSpeed;
    private double sprintSpeed;
    private int sprinterFromRound;
    private double sprinterChanceStep;
    private double sprinterChanceMax;

    public ZombiesConfig(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads (or reloads) all values, writing defaults first when the file is missing.
     */
    public void load() {
        File file = new File(this.plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            this.plugin.saveResource(FILE_NAME, false);
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        this.countdownSeconds = yaml.getInt("countdownSeconds", 10);
        this.intermissionSeconds = yaml.getInt("intermissionSeconds", 10);
        this.restoreChunksPerTick = yaml.getInt("restoreChunksPerTick", 4);
        this.killPoints = yaml.getInt("points.kill", 90);
        this.hitPoints = yaml.getInt("points.hit", 10);
        this.repairPoints = yaml.getInt("points.repair", 10);
        this.windowBreakIntervalTicks = yaml.getInt("window.breakIntervalTicks", 40);
        this.windowRepairCooldownMillis = yaml.getLong("window.repairCooldownMillis", 400L);
        this.windowRepairReach = yaml.getDouble("window.repairReach", 4.0);
        this.windowZombieBlockReach = yaml.getDouble("window.zombieBlockReach", 3.5);
        this.doorConfirmMillis = yaml.getLong("door.confirmSeconds", 3L) * 1000L;
        this.bleedoutSeconds = yaml.getInt("downed.bleedoutSeconds", 30);
        this.reviveSeconds = yaml.getInt("downed.reviveSeconds", 5);
        this.selfReviveSeconds = yaml.getInt("downed.selfReviveSeconds", 5);
        this.reviveReach = yaml.getDouble("downed.reviveReach", 2.5);
        this.powerupDropChance = yaml.getDouble("powerups.dropChance", 0.03);
        this.powerupDurationSeconds = yaml.getInt("powerups.durationSeconds", 30);
        this.powerupDespawnSeconds = yaml.getInt("powerups.despawnSeconds", 20);
        this.powerupPickupReach = yaml.getDouble("powerups.pickupReach", 1.3);
        this.baseZombies = yaml.getInt("round.baseZombies", 6);
        this.zombiesPerRound = yaml.getInt("round.perRound", 2);
        this.zombiesPerExtraPlayer = yaml.getInt("round.perExtraPlayer", 3);
        this.maxAlive = yaml.getInt("round.maxAlive", 24);
        this.spawnIntervalTicks = yaml.getInt("round.spawnIntervalTicks", 20);
        this.healthMultiplierPerRound = yaml.getDouble("round.healthMultiplierPerRound", 0.15);
        this.baseSpeed = yaml.getDouble("round.baseSpeed", 0.23);
        this.sprintSpeed = yaml.getDouble("round.sprintSpeed", 0.30);
        this.sprinterFromRound = yaml.getInt("round.sprinterFromRound", 5);
        this.sprinterChanceStep = yaml.getDouble("round.sprinterChanceStep", 0.05);
        this.sprinterChanceMax = yaml.getDouble("round.sprinterChanceMax", 0.6);
    }
}
