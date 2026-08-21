package dev.portero.atlas.menu;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.combat.CombatManager;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.database.DatabaseManager;
import dev.portero.atlas.level.LevelService;
import dev.portero.atlas.menu.api.MenuListener;
import dev.portero.atlas.menu.api.MenuService;
import dev.portero.atlas.party.PartyService;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.resource.ResourceManager;
import dev.portero.atlas.scheduler.AtlasScheduler;
import dev.portero.atlas.skill.SkillService;
import dev.portero.atlas.stat.StatManager;
import dev.portero.atlas.worldevent.WorldEventService;
import org.bukkit.plugin.Plugin;

public final class MenuModule implements AtlasModule {

    @Override
    public String id() {
        return "menus";
    }

    @Override
    public void load(ModuleContext context) {
        MenuService service = new MenuService(context.plugin(), context.service(AtlasScheduler.class));
        context.services().register(MenuService.class, service);
        context.services().register(MenuFactory.class, new MenuFactory(
                context.service(ProfileManager.class),
                context.service(StatManager.class),
                context.service(ResourceManager.class),
                context.service(CombatManager.class),
                context.service(SkillService.class),
                context.service(WorldEventService.class),
                context.service(ConfigManager.class),
                context.service(DatabaseManager.class),
                context.service(PartyService.class),
                context.service(LevelService.class)));
    }

    @Override
    public void enable(ModuleContext context) {
        Plugin plugin = context.plugin();
        plugin.getServer().getPluginManager().registerEvents(
                new MenuListener(context.service(MenuService.class)), plugin);
    }
}
