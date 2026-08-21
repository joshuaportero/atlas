package dev.portero.atlas.bootstrap;

import dev.portero.atlas.cmd.AtlasCommand;
import dev.portero.atlas.cmd.GameModeCommand;
import dev.portero.atlas.cmd.KothCommand;
import dev.portero.atlas.cmd.LevelCommand;
import dev.portero.atlas.cmd.PartyCommand;
import dev.portero.atlas.cmd.SettingsCommand;
import dev.portero.atlas.cmd.SkillCommand;
import dev.portero.atlas.command.CommandManager;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.combat.CombatModule;
import dev.portero.atlas.data.DataModule;
import dev.portero.atlas.database.DatabaseManager;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.koth.KothModule;
import dev.portero.atlas.koth.KothService;
import dev.portero.atlas.level.LevelModule;
import dev.portero.atlas.level.LevelService;
import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.MenuModule;
import dev.portero.atlas.menu.api.MenuService;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.placeholder.PlaceholderModule;
import dev.portero.atlas.party.PartyModule;
import dev.portero.atlas.party.PartyService;
import dev.portero.atlas.player.PlayerModule;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.resource.ResourceModule;
import dev.portero.atlas.skill.SkillModule;
import dev.portero.atlas.skill.SkillService;
import dev.portero.atlas.scheduler.AtlasScheduler;
import dev.portero.atlas.stat.StatModule;
import dev.portero.atlas.scoreboard.ScoreboardManager;
import dev.portero.atlas.worldevent.WorldEventModule;
import dev.portero.atlas.worldevent.WorldEventService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class AtlasBootstrap {

    private final JavaPlugin plugin;
    private final ServiceRegistry services = new ServiceRegistry();
    private ModuleRegistry modules;
    private CommandManager commands;
    private ScoreboardManager scoreboards;
    private DatabaseManager database;
    private AtlasScheduler scheduler;

    public AtlasBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ServiceRegistry services() {
        return this.services;
    }

    public void start() {
        this.services.register(JavaPlugin.class, this.plugin);
        this.services.register(Plugin.class, this.plugin);

        this.scheduler = new AtlasScheduler(this.plugin);
        this.services.register(AtlasScheduler.class, this.scheduler);
        this.services.register(EventBus.class, new EventBus());
        this.services.register(PipelineRegistry.class, new PipelineRegistry());

        ConfigManager configs = new ConfigManager(this.plugin);
        for (ConfigType configType : ConfigType.values()) {
            configs.loadConfig(configType);
        }
        this.services.register(ConfigManager.class, configs);

        YamlConfiguration config = configs.getConfig(ConfigType.DEFAULT);
        if (config == null) {
            throw new IllegalStateException("Failed to load config.yml");
        }

        this.database = new DatabaseManager(this.plugin, config);
        try {
            this.database.connect();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to connect to the database", exception);
        }
        this.services.register(DatabaseManager.class, this.database);

        this.scoreboards = new ScoreboardManager(this.plugin);
        this.scoreboards.initialize();
        this.services.register(ScoreboardManager.class, this.scoreboards);

        this.modules = new ModuleRegistry(new ModuleContext(this.plugin, this.services));
        this.modules.register(new DataModule());
        this.modules.register(new PlayerModule());
        this.modules.register(new StatModule());
        this.modules.register(new LevelModule());
        this.modules.register(new ResourceModule());
        this.modules.register(new CombatModule());
        this.modules.register(new SkillModule());
        this.modules.register(new WorldEventModule());
        this.modules.register(new PartyModule());
        this.modules.register(new KothModule());
        this.modules.register(new PlaceholderModule());
        this.modules.register(new MenuModule());
        this.modules.loadAll();
        this.modules.enableAll();

        MenuService menuService = this.services.require(MenuService.class);
        MenuFactory menuFactory = this.services.require(MenuFactory.class);
        ProfileManager profiles = this.services.require(ProfileManager.class);
        this.commands = new CommandManager(this.plugin);
        this.commands.register(
                new AtlasCommand(menuService, menuFactory),
                new GameModeCommand(),
                new PartyCommand(this.services.require(PartyService.class), profiles,
                        menuService, menuFactory),
                new SkillCommand(this.services.require(SkillService.class), profiles,
                        menuService, menuFactory),
                new LevelCommand(this.services.require(LevelService.class), profiles),
                new SettingsCommand(menuService, menuFactory),
                new KothCommand(this.services.require(KothService.class),
                        this.services.require(WorldEventService.class)));
        this.services.register(CommandManager.class, this.commands);
    }

    public void stop() {
        if (this.modules != null) {
            this.modules.disableAll();
        }
        if (this.commands != null) {
            this.commands.unregister();
        }
        if (this.scoreboards != null) {
            this.scoreboards.shutdown();
        }
        if (this.scheduler != null) {
            this.scheduler.shutdown();
        }
        if (this.database != null) {
            this.database.shutdown();
        }
        this.services.clear();
    }
}
