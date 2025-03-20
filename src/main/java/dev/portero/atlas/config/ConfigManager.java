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
    private final Map<ConfigType, YamlConfiguration> configs = new EnumMap<>(ConfigType.class);

    public void loadConfig(ConfigType configType) {
        File file = FileUtil.createFileIfNotExists(this.plugin, configType.getFileName());
        if (file == null) {
            log.error("Failed to create or access {}", configType.getFileName());
            return;
        }

        YamlConfiguration yamlConfig = FileUtil.loadYamlConfig(file);
        if (yamlConfig == null) {
            log.error("Failed to load configuration {}", configType.getFileName());
            return;
        }

        this.configs.put(configType, yamlConfig);
        log.info("Successfully loaded {}", configType.getFileName());
    }

    @Nullable
    public YamlConfiguration getConfig(ConfigType configType) {
        return this.configs.get(configType);
    }

    public void saveConfig(ConfigType configType) {
        YamlConfiguration yamlConfig = this.configs.get(configType);
        if (yamlConfig == null) {
            log.warn("Attempted to save non-loaded config: {}", configType.getFileName());
            return;
        }

        File file = new File(this.plugin.getDataFolder(), configType.getFileName());
        FileUtil.saveYamlConfig(yamlConfig, file);
        log.info("Saved configuration {}", configType.getFileName());
    }

    public void reloadConfig(ConfigType configType) {
        this.loadConfig(configType);
        log.info("Reloaded configuration {}", configType.getFileName());
    }

    public void saveAll() {
        for (ConfigType configType : this.configs.keySet()) {
            this.saveConfig(configType);
        }
        log.info("Saved all configurations");
    }

    public void reloadAll() {
        for (ConfigType configType : ConfigType.values()) {
            this.reloadConfig(configType);
        }
        log.info("Reloaded all configurations");
    }
}