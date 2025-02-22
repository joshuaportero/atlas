package dev.portero.atlas;

import com.google.common.base.Stopwatch;
import dev.portero.atlas.command.CommandManager;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.database.DatabaseManager;
import dev.portero.atlas.scoreboard.ScoreboardManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
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
        Stopwatch stopwatch = Stopwatch.createStarted();

        ConfigManager configManager = new ConfigManager(this);
        configManager.loadConfigurations();

        this.commandManager = new CommandManager(this);

        this.scoreboardManager = new ScoreboardManager(this);
        this.scoreboardManager.initialize();

        this.getLogger().info("Atlas has been initialized in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms.");
    }

    @Override
    public void onDisable() {
        commandManager.unregister();
        scoreboardManager.shutdown();
        databaseManager.shutdown();
    }
}