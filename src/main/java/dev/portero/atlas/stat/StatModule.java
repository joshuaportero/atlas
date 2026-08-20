package dev.portero.atlas.stat;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.data.profile.ProfileComponentRegistry;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.pipeline.PlayerReadyContext;
import dev.portero.atlas.player.ProfileManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class StatModule implements AtlasModule {

    @Override
    public String id() {
        return "stats";
    }

    @Override
    public void load(ModuleContext context) {
        context.service(ProfileComponentRegistry.class).register(StatComponent.key, StatComponent::new);

        StatRegistry registry = new StatRegistry();
        context.services().register(StatRegistry.class, registry);

        YamlConfiguration config = context.service(ConfigManager.class).getConfig(ConfigType.DEFAULT);
        ConfigurationSection defaults = config != null ? config.getConfigurationSection("stats.defaults") : null;

        StatManager manager = new StatManager(
                registry, context.service(PipelineRegistry.class), context.service(EventBus.class));
        manager.registerDefaults(defaults);
        context.services().register(StatManager.class, manager);

        context.service(PipelineRegistry.class)
                .require("player.ready", PlayerReadyContext.class)
                .add(new LoadStatsStage(context.service(ProfileManager.class), manager));
    }
}
