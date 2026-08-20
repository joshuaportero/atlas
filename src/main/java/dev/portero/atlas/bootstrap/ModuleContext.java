package dev.portero.atlas.bootstrap;

import org.bukkit.plugin.Plugin;

public final class ModuleContext {

    private final Plugin plugin;
    private final ServiceRegistry services;

    public ModuleContext(Plugin plugin, ServiceRegistry services) {
        this.plugin = plugin;
        this.services = services;
    }

    public Plugin plugin() {
        return this.plugin;
    }

    public ServiceRegistry services() {
        return this.services;
    }

    public <T> T service(Class<T> type) {
        return this.services.require(type);
    }
}
