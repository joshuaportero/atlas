package dev.portero.atlas;

import com.google.common.base.Stopwatch;
import dev.portero.atlas.command.CommandManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.database.DatabaseManager;
import dev.portero.atlas.scoreboard.ScoreboardManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;


public class AtlasPlugin extends JavaPlugin {

    private CommandManager commandManager;
    private ScoreboardManager scoreboardManager;
    private DatabaseManager databaseManager;

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

        this.commandManager = new CommandManager(this);
        this.commandManager.register();

        this.scoreboardManager = new ScoreboardManager(this);
        this.scoreboardManager.initialize();

        YamlConfiguration config = configManager.getConfig(ConfigType.DEFAULT);
        this.databaseManager = new DatabaseManager(config);

        try {
            this.databaseManager.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        this.commandManager.unregister();
        this.scoreboardManager.shutdown();
        this.databaseManager.shutdown();
    }
}