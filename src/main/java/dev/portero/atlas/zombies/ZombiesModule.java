package dev.portero.atlas.zombies;

import dev.portero.atlas.zombies.arena.ArenaRepository;
import dev.portero.atlas.zombies.arena.setup.SetupManager;
import dev.portero.atlas.zombies.combat.ZombieService;
import dev.portero.atlas.zombies.combat.gun.GunRegistry;
import dev.portero.atlas.zombies.combat.gun.GunService;
import dev.portero.atlas.zombies.game.GameManager;
import dev.portero.atlas.zombies.listener.GameListener;
import dev.portero.atlas.zombies.perk.PerkRegistry;
import dev.portero.atlas.zombies.perk.PerkService;
import dev.portero.atlas.zombies.worldedit.WorldEditService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Bootstrap and service registry of the zombies minigame. See
 * {@code docs/zombies-design.md} for the overall architecture.
 */
@Slf4j
@Getter
public class ZombiesModule {

    private final Plugin plugin;
    private final ZombiesKeys keys;
    private final ZombiesConfig config;
    private final ArenaRepository arenaRepository;
    private final SetupManager setupManager;
    private final ZombieService zombieService;
    private final GunRegistry gunRegistry;
    private final GunService gunService;
    private final PerkRegistry perkRegistry;
    private final PerkService perkService;
    private final GameManager gameManager;
    @Nullable
    private final WorldEditService worldEditService;

    public ZombiesModule(Plugin plugin) {
        this.plugin = plugin;
        this.keys = new ZombiesKeys(plugin);
        this.config = new ZombiesConfig(plugin);
        this.config.load();
        this.arenaRepository = new ArenaRepository(plugin);
        this.setupManager = new SetupManager();
        this.zombieService = new ZombieService(plugin, this.keys);
        this.gunRegistry = new GunRegistry(plugin);
        this.gameManager = new GameManager(this);
        this.perkRegistry = new PerkRegistry(plugin);
        this.perkService = new PerkService(this);
        this.gunService = new GunService(plugin, this.keys, this.gunRegistry, this.perkService);
        this.worldEditService = this.createWorldEditService();
    }

    /**
     * Loads arenas and registers listeners. Called once from {@code AtlasPlugin#onEnable}.
     */
    public void enable() {
        this.gunRegistry.load();
        this.perkRegistry.load();
        this.arenaRepository.loadAll();
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this.plugin);
        log.info("Zombies module enabled ({} arena(s) loaded)", this.arenaRepository.arenas().size());
    }

    /**
     * Ends all running games and restores arenas. Called from {@code AtlasPlugin#onDisable}.
     */
    public void disable() {
        this.gameManager.endAll();
    }

    /**
     * Reloads global tuning and arena definitions. Running games keep their loaded arena.
     */
    public void reload() {
        this.config.load();
        this.gunRegistry.load();
        this.perkRegistry.load();
        this.arenaRepository.loadAll();
    }

    @Nullable
    private WorldEditService createWorldEditService() {
        boolean worldEdit = Bukkit.getPluginManager().getPlugin("WorldEdit") != null;
        boolean fawe = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
        if (!worldEdit && !fawe) {
            log.warn("WorldEdit not found — arena setup and snapshot restore are disabled");
            return null;
        }
        try {
            log.info("Hooked into {}", fawe ? "FastAsyncWorldEdit" : "WorldEdit");
            return new WorldEditService(this.plugin, this.config.getRestoreChunksPerTick());
        } catch (NoClassDefFoundError error) {
            log.warn("WorldEdit classes unavailable: {}", error.getMessage());
            return null;
        }
    }
}
