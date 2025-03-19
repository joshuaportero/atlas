package dev.portero.atlas.config;

import dev.portero.atlas.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ConfigManager {

    private final Plugin plugin;
    private final Map<Config, YamlConfiguration> configs = new EnumMap<>(Config.class);

    public void loadConfig(Config config) {
        File file = FileUtil.createFileIfNotExists(this.plugin, config.getFileName());
        if (file == null) {
            log.error("Failed to create or access {}", config.getFileName());
            return;
        }

        YamlConfiguration yamlConfig = FileUtil.loadYamlConfig(file);
        if (yamlConfig == null) {
            log.error("Failed to load configuration {}", config.getFileName());
            return;
        }

        this.configs.put(config, yamlConfig);
        log.info("Successfully loaded {}", config.getFileName());
    }

    @Nullable
    public YamlConfiguration getConfig(Config config) {
        return this.configs.get(config);
    }

    public void saveConfig(Config config) {
        YamlConfiguration yamlConfig = this.configs.get(config);
        if (yamlConfig == null) {
            log.warn("Attempted to save non-loaded config: {}", config.getFileName());
            return;
        }

        File file = new File(this.plugin.getDataFolder(), config.getFileName());
        FileUtil.saveYamlConfig(yamlConfig, file);
        log.info("Saved configuration {}", config.getFileName());
    }

    public void reloadConfig(Config config) {
        this.loadConfig(config);
        log.info("Reloaded configuration {}", config.getFileName());
    }

    public void saveAll() {
        for (Config config : this.configs.keySet()) {
            this.saveConfig(config);
        }
        log.info("Saved all configurations");
    }

    public void reloadAll() {
        for (Config config : Config.values()) {
            this.reloadConfig(config);
        }
        log.info("Reloaded all configurations");
    }
}