package dev.portero.atlas.config;

import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class ConfigManager {

    private Plugin plugin;

    public void loadConfig(String fileName) {
    }

    @Nullable
    public YamlConfiguration getConfig(String fileName) {
        return null;
    }

    public void saveConfig(String fileName) {}

    public void reloadConfig(String fileName) {}

    public void saveAll() {}

    public void reloadAll() {}
}