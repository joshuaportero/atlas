package dev.portero.atlas.mechanic;

import dev.portero.atlas.AtlasPlugin;
import dev.portero.atlas.mechanic.impl.RespawnMechanic;
import dev.portero.atlas.mechanic.impl.TntExplosionMechanic;
import org.bukkit.configuration.file.FileConfiguration;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MechanicManager {

    private final AtlasPlugin plugin;
    private final Map<String, Mechanic> mechanics;

    public MechanicManager(AtlasPlugin plugin) {
        this.plugin = plugin;
        this.mechanics = new HashMap<>();
    }

    public void registerMechanics() {
        // Register all available mechanics here
        this.registerMechanic(new TntExplosionMechanic(this.plugin));
        this.registerMechanic(new RespawnMechanic(this.plugin));

        // Load them based on config
        this.loadMechanics();
    }

    private void registerMechanic(Mechanic mechanic) {
        this.mechanics.put(mechanic.getName(), mechanic);
        this.plugin.getServer().getPluginManager().registerEvents(mechanic, this.plugin);
    }

    public void loadMechanics() {
        this.plugin.reloadConfig();
        FileConfiguration config = this.plugin.getConfig();

        for (Mechanic mechanic : this.mechanics.values()) {
            String path = "mechanics." + mechanic.getName();
            if (config.isConfigurationSection(path)) {
                mechanic.onLoad(config.getConfigurationSection(path));
            } else {
                // Fallback for old boolean style or if section missing, though we prefer
                // section now
                boolean enabled = config.getBoolean(path + ".enabled", false);
                mechanic.setEnabled(enabled);
            }
        }
    }

    public void enableMechanic(Mechanic mechanic) {
        if (mechanic.isEnabled()) {
            return;
        }

        mechanic.setEnabled(true);
        this.saveMechanicState(mechanic, true);
    }

    public void disableMechanic(Mechanic mechanic) {
        if (!mechanic.isEnabled()) {
            return;
        }

        mechanic.setEnabled(false);
        this.saveMechanicState(mechanic, false);
    }

    private void saveMechanicState(Mechanic mechanic, boolean enabled) {
        FileConfiguration config = this.plugin.getConfig();
        config.set("mechanics." + mechanic.getName() + ".enabled", enabled);
        this.plugin.saveConfig();
    }

    private @NonNull Logger getLogger() {
        return this.plugin.getLogger();
    }

    public List<String> getRegisteredMechanics() {
        return this.mechanics.keySet().stream().toList();
    }

    public boolean isMechanicEnabled(Mechanic mechanic) {
        return mechanic.isEnabled();
    }

    public boolean isMechanicEnabled(String mechanicName) {
        Mechanic mechanic = this.getMechanic(mechanicName);
        return mechanic != null && mechanic.isEnabled();
    }

    public Mechanic getMechanic(String name) {
        return this.mechanics.get(name);
    }
}
