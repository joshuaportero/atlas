package dev.portero.atlas.koth;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.level.LevelService;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.scheduler.AtlasScheduler;
import org.bukkit.configuration.file.YamlConfiguration;

public final class KothModule implements AtlasModule {

    @Override
    public String id() {
        return "koth";
    }

    @Override
    public void load(ModuleContext context) {
        YamlConfiguration config = context.service(ConfigManager.class).getConfig(ConfigType.DEFAULT);
        double radius = config != null ? config.getDouble("koth.radius", 8.0) : 8.0;
        long xp = config != null ? config.getLong("koth.xp-reward", 250L) : 250L;
        context.services().register(KothService.class, new KothService(
                context.service(ProfileManager.class),
                context.service(LevelService.class),
                context.service(AtlasScheduler.class),
                context.service(EventBus.class),
                radius,
                xp));
    }
}
