package dev.portero.atlas;

import dev.portero.escape.database.DatabaseManager;
import lombok.Getter;
import lombok.NonNull;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;

public class AtlasPlugin extends JavaPlugin {

    @Getter
    private static AtlasPlugin instance;

    @Getter
    private YamlConfiguration arenas;
    @Getter
    private YamlConfiguration players;
    @Getter
    private YamlConfiguration worlds;

    private ScoreboardLibrary scoreboardLibrary;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        this.setInstance(this);
        this.loadConfiguration();
        this.connectDatabase();
        this.loadScoreboard();
    }

    private void loadScoreboard() {
        try {
            this.scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(instance);
        } catch (NoPacketAdapterAvailableException e) {
            throw new RuntimeException("No scoreboard packet adapter available!", e);
        }
    }

    private void connectDatabase() {
        this.databaseManager = new DatabaseManager(this.getLogger(), this.getConfig());

        try {
            this.databaseManager.connect();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database!", e);
        }
    }

    @Override
    public void onDisable() {
        if (this.scoreboardLibrary != null) {
            this.scoreboardLibrary.close();
        }
        this.databaseManager.close();
        this.setInstance(null);
    }

    private void setInstance(AtlasPlugin instance) {
        AtlasPlugin.instance = instance;
    }

    public void loadConfiguration() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();

        this.arenas = this.createConfiguration("arenas.yml");
        this.players = this.createConfiguration("players.yml");
        this.worlds = this.createConfiguration("worlds.yml");

        this.getLogger().info("The configuration files have been loaded.");
    }

    private @NonNull YamlConfiguration createConfiguration(String fileName) {
        try {
            File file = new File(this.getDataFolder(), fileName);
            if (!file.exists()) {
                if (file.createNewFile()) {
                    this.getLogger().info("The configuration file \"" + fileName + "\" has been created.");
                }
            }
            return YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to load configuration file: " + fileName, e);
            return new YamlConfiguration();
        }
    }
}