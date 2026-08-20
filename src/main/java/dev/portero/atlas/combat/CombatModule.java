package dev.portero.atlas.combat;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.stat.StatManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class CombatModule implements AtlasModule {

    @Override
    public String id() {
        return "combat";
    }

    @Override
    public void load(ModuleContext context) {
        CombatManager manager = new CombatManager(
                context.service(ProfileManager.class),
                context.service(StatManager.class),
                context.service(PipelineRegistry.class),
                context.service(EventBus.class));
        context.services().register(CombatManager.class, manager);
    }

    @Override
    public void enable(ModuleContext context) {
        YamlConfiguration config = context.service(ConfigManager.class).getConfig(ConfigType.DEFAULT);
        if (config != null && !config.getBoolean("combat.enabled", true)) {
            return;
        }

        Plugin plugin = context.plugin();
        plugin.getServer().getPluginManager().registerEvents(
                new CombatListener(context.service(CombatManager.class)), plugin);
    }
}
