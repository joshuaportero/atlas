package dev.portero.atlas.quest;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.level.LevelService;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.stat.StatManager;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

public final class QuestModule implements AtlasModule {

    @Override
    public String id() {
        return "quests";
    }

    @Override
    public void load(ModuleContext context) {
        QuestService quests = new QuestService(
                context.service(LevelService.class), context.service(StatManager.class));
        quests.register(new QuestDefinition("woodcutter", "Lumberjack",
                "Chop 50 logs anywhere in the world.", Material.OAK_LOG,
                QuestObjective.blocks("logs", 50), 150, 2));
        quests.register(new QuestDefinition("stonecutter", "Apprentice Miner",
                "Mine 30 stone, cobblestone, or deepslate.", Material.STONE_PICKAXE,
                QuestObjective.blocks("stone", 30), 120, 1));
        quests.register(new QuestDefinition("hunter", "Zombie Hunter",
                "Slay 10 zombies.", Material.ROTTEN_FLESH,
                QuestObjective.entities("zombie", 10), 180, 2));
        context.services().register(QuestService.class, quests);
    }

    @Override
    public void enable(ModuleContext context) {
        Plugin plugin = context.plugin();
        plugin.getServer().getPluginManager().registerEvents(
                new QuestListener(context.service(ProfileManager.class),
                        context.service(QuestService.class)), plugin);
    }
}
