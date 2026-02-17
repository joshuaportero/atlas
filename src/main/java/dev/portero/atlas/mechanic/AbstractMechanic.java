package dev.portero.atlas.mechanic;

import dev.portero.atlas.AtlasPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class AbstractMechanic implements Mechanic {

    protected final AtlasPlugin plugin;
    private boolean enabled;

    protected AbstractMechanic(AtlasPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    @Override
    public void onLoad(ConfigurationSection config) {
        if (config != null && config.isBoolean("enabled")) {
            this.setEnabled(config.getBoolean("enabled"));
        }
    }

    protected FileConfiguration getConfig() {
        return this.plugin.getConfig();
    }
}
