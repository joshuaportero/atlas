package dev.portero.atlas.placeholder;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.level.LevelService;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.resource.ResourceManager;
import dev.portero.atlas.stat.StatManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PlaceholderModule implements AtlasModule {

    @Override
    public String id() {
        return "placeholders";
    }

    @Override
    public void enable(ModuleContext context) {
        if (context.plugin().getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            log.info("PlaceholderAPI not found; stat/resource placeholders are inactive");
            return;
        }

        new AtlasPlaceholderExpansion(
                context.plugin(),
                context.service(ProfileManager.class),
                context.service(StatManager.class),
                context.service(ResourceManager.class),
                context.service(LevelService.class)).register();
        log.info("Registered PlaceholderAPI expansion (%atlas_%)");
    }
}
