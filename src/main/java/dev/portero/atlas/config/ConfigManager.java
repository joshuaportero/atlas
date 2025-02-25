package dev.portero.atlas.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

/**
 * Manages multiple YAML configuration files for a Bukkit/Spigot plugin.
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private final Map<String, YamlConfiguration> configurations;
    private final Map<String, File> configFiles;

    /**
     * Creates a new ConfigManager instance.
     *
     * @param plugin The plugin instance
     */
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configurations = new HashMap<>();
        this.configFiles = new HashMap<>();

        // Create plugin data folder if it doesn't exist
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().severe("Could not create plugin data folder!");
        }
    }

    /**
     * Loads a configuration file. If the file doesn't exist, it will be created.
     * If a default configuration exists in the plugin's resources, it will be used as a template.
     *
     * @param fileName The name of the configuration file (e.g., "config.yml")
     * @return The loaded YamlConfiguration, or null if loading failed
     */
    @Nullable
    public YamlConfiguration loadConfig(String fileName) {
        try {
            File file = this.createFileIfNotExists(fileName);
            if (file == null) {
                return null;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Look for defaults in the jar
            InputStream defaultConfigStream = this.plugin.getResource(fileName);
            if (defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));

                // Check for missing options and add defaults
                for (String key : defaultConfig.getKeys(true)) {
                    if (!config.contains(key)) {
                        config.set(key, defaultConfig.get(key));
                    }
                }

                // Save updated config with defaults included
                config.save(file);
            }

            this.configurations.put(fileName, config);
            this.configFiles.put(fileName, file);

            return config;
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to load configuration file: " + fileName, e);
            return null;
        }
    }

    /**
     * Retrieves a configuration that has already been loaded.
     *
     * @param fileName The name of the configuration file
     * @return The YamlConfiguration or null if it hasn't been loaded
     */
    @Nullable
    public YamlConfiguration getConfig(String fileName) {
        return this.configurations.get(fileName);
    }

    /**
     * Saves a configuration to disk.
     *
     * @param fileName The name of the configuration file
     * @return true if the save was successful, false otherwise
     */
    public boolean saveConfig(String fileName) {
        YamlConfiguration config = this.configurations.get(fileName);
        File file = this.configFiles.get(fileName);

        if (config == null || file == null) {
            this. plugin.getLogger().warning("Cannot save config " + fileName + " because it has not been loaded");
            return false;
        }

        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save configuration file: " + fileName, e);
            return false;
        }
    }

    /**
     * Reloads a configuration from disk.
     *
     * @param fileName The name of the configuration file
     * @return The reloaded YamlConfiguration, or null if reloading failed
     */
    @Nullable
    public YamlConfiguration reloadConfig(String fileName) {
        File file = this.configFiles.get(fileName);

        if (file == null || !file.exists()) {
            this.plugin.getLogger().warning("Cannot reload config " + fileName + " because it does not exist");
            return null;
        }

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            this.configurations.put(fileName, config);
            return config;
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to reload configuration file: " + fileName, e);
            return null;
        }
    }

    /**
     * Creates a file if it doesn't exist and copies default content from the plugin jar if available.
     *
     * @param fileName The name of the file to create
     * @return The file object, or null if creation failed
     */
    @Nullable
    private File createFileIfNotExists(String fileName) {
        File file = new File(this.plugin.getDataFolder(), fileName);

        if (file.exists()) {
            return file;
        }

        try {
            // Check if we have a default in resources
            InputStream defaultResource = this.plugin.getResource(fileName);

            if (defaultResource != null) {
                // Copy default from jar
                this.plugin.saveResource(fileName, false);
                this.plugin.getLogger().info("Created configuration file from default: " + fileName);
            } else {
                // Create empty file
                if (!file.createNewFile()) {
                    this.plugin.getLogger().severe("Could not create configuration file: " + fileName);
                    return null;
                }
                this.plugin.getLogger().info("Created empty configuration file: " + fileName);
            }

            return file;
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to create configuration file: " + fileName, e);
            return null;
        }
    }

    /**
     * Saves all loaded configurations to disk.
     */
    public void saveAll() {
        for (String fileName : this.configurations.keySet()) {
            this.saveConfig(fileName);
        }
    }

    /**
     * Reloads all loaded configurations from disk.
     */
    public void reloadAll() {
        for (String fileName : this.configurations.keySet()) {
            this.reloadConfig(fileName);
        }
    }
}