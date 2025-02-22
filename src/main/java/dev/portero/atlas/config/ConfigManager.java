package dev.portero.atlas.config;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class ConfigManager {

    private final JavaPlugin plugin;

    @Getter
    private YamlConfiguration data;
    @Getter
    private YamlConfiguration scoreboards;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfigurations() {
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveDefaultConfig();
        this.scoreboards = loadConfiguration("scoreboards.yml");
        this.data = loadConfiguration("data.yml");
        plugin.getLogger().info("The configuration files have been loaded.");
    }

    private YamlConfiguration loadConfiguration(String fileName) {
        try {
            File file = new File(plugin.getDataFolder(), fileName);
            if (!file.exists() && file.createNewFile()) {
                plugin.getLogger().info("The configuration file \"" + fileName + "\" has been created.");
            }
            return YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load configuration file: " + fileName, e);
            return new YamlConfiguration();
        }
    }
}

