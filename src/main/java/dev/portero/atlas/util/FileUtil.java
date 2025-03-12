package dev.portero.atlas.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class FileUtil {

    /**
     * Creates a file if it doesn't exist and copies default content from the plugin jar if available.
     *
     * @param plugin   The plugin instance
     * @param fileName The name of the file to create
     * @return The file object, or null if creation failed
     */
    @Nullable
    public static File createFileIfNotExists() {
        return null;
    }

    /**
     * Loads a YAML configuration from a file, applying defaults from the plugin's resources if available.
     *
     * @param plugin   The plugin instance
     * @param file     The configuration file
     * @param fileName The name of the configuration file (for loading defaults)
     * @return The loaded configuration, or null if loading failed
     */
    @Nullable
    public static YamlConfiguration loadYamlConfig() {
        return null;
    }

    /**
     * Saves a YAML configuration to disk.
     *
     * @param config   The configuration to save
     * @param file     The file to save to
     * @param logger   The logger to use for error reporting
     * @param fileName The name of the file (for logging purposes)
     */
    public static void saveYamlConfig() {

    }

    /**
     * Ensures the plugin data folder exists.
     *
     * @param plugin The plugin instance
     */
    public static void ensurePluginFolderExists(Plugin plugin) {

    }
}