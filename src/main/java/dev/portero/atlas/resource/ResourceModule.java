package dev.portero.atlas.resource;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.data.profile.ProfileComponentRegistry;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.pipeline.PlayerCloseContext;
import dev.portero.atlas.pipeline.PlayerReadyContext;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.scheduler.AtlasScheduler;
import dev.portero.atlas.stat.StatManager;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ResourceModule implements AtlasModule {

    @Override
    public String id() {
        return "resources";
    }

    @Override
    public void load(ModuleContext context) {
        context.service(ProfileComponentRegistry.class)
                .register(ResourceComponent.key, ResourceComponent::new);

        ResourceManager manager = new ResourceManager(
                context.service(ProfileManager.class),
                context.service(StatManager.class),
                context.service(EventBus.class));

        YamlConfiguration config = context.service(ConfigManager.class).getConfig(ConfigType.DEFAULT);
        manager.registerDefaults(config != null ? config.getConfigurationSection("resources") : null);
        context.services().register(ResourceManager.class, manager);

        PipelineRegistry pipelines = context.service(PipelineRegistry.class);
        pipelines.require("player.ready", PlayerReadyContext.class)
                .add(new LoadResourcesStage(context.service(ProfileManager.class), manager));
        pipelines.require("player.close", PlayerCloseContext.class)
                .add(new SaveResourcesStage(context.service(ProfileManager.class), manager));
    }

    @Override
    public void enable(ModuleContext context) {
        YamlConfiguration config = context.service(ConfigManager.class).getConfig(ConfigType.DEFAULT);
        long interval = config != null ? config.getLong("resources.tick-interval", 20L) : 20L;
        context.service(ResourceManager.class)
                .startRegen(context.service(AtlasScheduler.class), Math.max(1L, interval));
    }

    @Override
    public void disable(ModuleContext context) {
        ResourceManager resources = context.service(ResourceManager.class);
        context.service(ProfileManager.class).online().forEach(resources::persist);
    }
}
