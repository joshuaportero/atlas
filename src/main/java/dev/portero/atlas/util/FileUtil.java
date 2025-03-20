package dev.portero.atlas.util;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

@Slf4j
public class FileUtil {

    @Nullable
    public static File createFileIfNotExists(Plugin plugin, String fileName) {
        // Create plugin data folder if it doesn't exist
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            log.error("Could not create plugin data folder");
            return null;
        }

        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            // Check if resource exists in plugin jar
            if (plugin.getResource(fileName) != null) {
                plugin.saveResource(fileName, false);
                return file;
            }

            // Create empty file if resource doesn't exist
            try {
                if (!file.createNewFile()) {
                    log.error("Could not create file: {}", fileName);
                    return null;
                }
            } catch (IOException e) {
                log.error("Error creating file {}: {}", fileName, e.getMessage());
                return null;
            }
        }

        return file;
    }

    @Nullable
    public static YamlConfiguration loadYamlConfig(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public static void saveYamlConfig(YamlConfiguration config, File file) {
        if (config == null || file == null) {
            return;
        }

        try {
            config.save(file);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}