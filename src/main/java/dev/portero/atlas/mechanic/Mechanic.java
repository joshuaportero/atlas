package dev.portero.atlas.mechanic;

import org.bukkit.event.Listener;

public interface Mechanic extends Listener {

    /**
     * Gets the unique identifier/name of the mechanic.
     * This name corresponds to the key in the configuration file.
     *
     * @return The mechanic name.
     */
    String getName();

    /**
     * Checks if the mechanic is currently enabled.
     *
     * @return True if enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Sets the enabled state of the mechanic.
     *
     * @param enabled The new enabled state.
     */
    void setEnabled(boolean enabled);

    /**
     * Called when the mechanic is loaded.
     * Use this to load configuration settings.
     *
     * @param config The configuration section for this mechanic.
     */
    default void onLoad(org.bukkit.configuration.ConfigurationSection config) {
    }

    /**
     * Called when the mechanic is enabled.
     */
    default void onEnable() {
    }

    /**
     * Called when the mechanic is disabled.
     */
    default void onDisable() {
    }
}
