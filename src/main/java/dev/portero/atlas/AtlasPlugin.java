package dev.portero.atlas;

import com.google.common.base.Stopwatch;
import dev.portero.atlas.command.CommandManager;
import dev.portero.atlas.config.Config;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.database.DatabaseManager;
import dev.portero.atlas.scoreboard.ScoreboardManager;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;


public class AtlasPlugin extends JavaPlugin {

    private CommandManager commandManager;

    @Getter
    private ConfigManager configManager;
    @Getter
    private ScoreboardManager scoreboardManager;
    @Getter
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        // Initialize the plugin
        this.initialize();

        getLogger().info("Atlas has been enabled in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms.");
    }

    private void initialize() {
        this.configManager = new ConfigManager(this);

        for (Config config : Config.values()) {
            this.configManager.loadConfig(config);
        }

        this.commandManager = new CommandManager(this);
        this.commandManager.register();

        this.scoreboardManager = new ScoreboardManager(this);
        this.scoreboardManager.initialize();

        this.databaseManager = new DatabaseManager(this.getLogger(), this.getConfig());

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

    public YamlConfiguration getConfig(Config config) {
        return this.configManager.getConfig(config);
    }
}