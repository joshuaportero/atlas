package dev.portero.atlas;

import com.google.common.base.Stopwatch;
import dev.portero.atlas.command.CommandManager;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.database.DatabaseManager;
import dev.portero.atlas.scoreboard.ScoreboardManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class AtlasPlugin extends JavaPlugin {

    private CommandManager commandManager;

    @Getter
    private ScoreboardManager scoreboardManager;
    @Getter
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        this.databaseManager = new DatabaseManager(this.getLogger(), this.getConfig());

        try {
            this.databaseManager.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.initialize();
    }

    public void initialize() {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        ConfigManager configManager = new ConfigManager(this);
        configManager.loadConfig("data.yml");

        this.commandManager = new CommandManager(this);
        this.commandManager.register();

        this.scoreboardManager = new ScoreboardManager(this);
        this.scoreboardManager.initialize();

        getLogger().info("Atlas has been initialized in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms.");
        getLogger().info(String.valueOf(Objects.requireNonNull(configManager.getConfig("data.yml"))
                .getBoolean("example")));
    }

    @Override
    public void onDisable() {
        this.commandManager.unregister();
        this.scoreboardManager.shutdown();
        this.databaseManager.shutdown();
    }
}