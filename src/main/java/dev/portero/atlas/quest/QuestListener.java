package dev.portero.atlas.quest;

import dev.portero.atlas.player.ProfileManager;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public final class QuestListener implements Listener {

    private final ProfileManager profiles;
    private final QuestService quests;

    public QuestListener(ProfileManager profiles, QuestService quests) {
        this.profiles = profiles;
        this.quests = quests;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        this.profiles.find(event.getPlayer()).ifPresent(player -> {
            Material type = event.getBlock().getType();
            if (Tag.LOGS.isTagged(type)) {
                this.quests.increment(player, "block", "logs", 1);
            }
            if (type == Material.STONE || type == Material.COBBLESTONE
                    || type == Material.DEEPSLATE) {
                this.quests.increment(player, "block", "stone", 1);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() == null) {
            return;
        }
        this.profiles.find(entity.getKiller()).ifPresent(player ->
                this.quests.increment(player, "entity",
                        entity.getType().name().toLowerCase(), 1));
    }
}
