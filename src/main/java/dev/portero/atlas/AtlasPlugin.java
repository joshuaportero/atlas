package dev.portero.atlas;

import com.google.common.base.Stopwatch;
import dev.portero.atlas.command.CommandManager;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.scoreboard.ScoreboardManager;
import dev.portero.atlas.zombies.ZombiesModule;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class AtlasPlugin extends JavaPlugin {

    private CommandManager commandManager;
    private ScoreboardManager scoreboardManager;
    private ZombiesModule zombiesModule;

    @Override
    public void onEnable() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        // Initialize the plugin
        this.initialize();

        getLogger().info("Atlas has been enabled in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms.");
    }

    private void initialize() {
        ConfigManager configManager = new ConfigManager(this);

        // Load all configuration files
        for (ConfigType configType : ConfigType.values()) {
            configManager.loadConfig(configType);
        }

        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();

        this.zombiesModule = new ZombiesModule(this);
        this.zombiesModule.enable();

        this.commandManager = new CommandManager(this, this.zombiesModule);
        this.commandManager.register();

        this.scoreboardManager = new ScoreboardManager(this);
        this.scoreboardManager.initialize();

        // Disabled database connection for now, as it's not being used yet.
        // Will be re-enabled once we have some database functionality to implement.
    }

    @Override
    public void onDisable() {
        this.commandManager.unregister();
        this.scoreboardManager.shutdown();
        this.zombiesModule.disable();
    }
}