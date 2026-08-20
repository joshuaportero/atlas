package dev.portero.atlas.bootstrap;

import dev.portero.atlas.command.CommandManager;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.data.DataModule;
import dev.portero.atlas.database.DatabaseManager;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.scheduler.AtlasScheduler;
import dev.portero.atlas.scoreboard.ScoreboardManager;
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

        this.commands = new CommandManager(this.plugin);
        this.commands.register();
        this.services.register(CommandManager.class, this.commands);

        this.scoreboards = new ScoreboardManager(this.plugin);
        this.scoreboards.initialize();
        this.services.register(ScoreboardManager.class, this.scoreboards);

        this.modules = new ModuleRegistry(new ModuleContext(this.plugin, this.services));
        this.modules.register(new DataModule());
        this.modules.loadAll();
        this.modules.enableAll();
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
