package dev.portero.atlas.level;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.data.profile.ProfileComponentRegistry;
import dev.portero.atlas.stat.StatManager;
import org.bukkit.configuration.file.YamlConfiguration;

public final class LevelModule implements AtlasModule {

    @Override
    public String id() {
        return "level";
    }

    @Override
    public void load(ModuleContext context) {
        context.service(ProfileComponentRegistry.class).register(LevelComponent.key, LevelComponent::new);
        YamlConfiguration config = context.service(ConfigManager.class).getConfig(ConfigType.DEFAULT);
        int maxLevel = config != null ? config.getInt("leveling.max-level", 50) : 50;
        long baseXp = config != null ? config.getLong("leveling.base-xp", 100L) : 100L;
        int points = config != null ? config.getInt("leveling.points-per-level", 2) : 2;
        context.services().register(LevelService.class, new LevelService(
                context.service(StatManager.class), maxLevel, baseXp, points));
    }
}
