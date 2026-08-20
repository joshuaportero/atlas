package dev.portero.atlas.menu.player;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.quest.QuestDefinition;
import dev.portero.atlas.quest.QuestProgress;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class QuestMenu extends AtlasMenu {

    private final MenuFactory menus;

    public QuestMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&8Quests");
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        AtlasPlayer atlas = this.menus.atlas(view.player());
        view.border(MenuItem.of(ItemFactory.pane()));
        int slot = 11;
        for (QuestDefinition quest : this.menus.quests().values()) {
            QuestProgress progress = this.menus.quests().progress(atlas, quest.id());
            String status = progress.complete() ? "&aComplete"
                    : progress.active() ? "&e" + progress.amount() + "/" + quest.objective().amount()
                    : "&7Available";
            String action = progress.active() ? "&cLeft-click to abandon"
                    : progress.complete() ? "&7Already finished"
                    : "&aLeft-click to accept";
            view.set(slot, MenuItem.of(ItemFactory.of(quest.icon(), "&e" + quest.name(),
                            "&7" + quest.description(),
                            status,
                            "&7Reward: &e" + quest.xpReward() + " XP",
                            action))
                    .onClick(context -> {
                        if (progress.active()) {
                            this.menus.quests().abandon(atlas, quest.id());
                        } else if (!progress.complete()) {
                            this.menus.quests().start(atlas, quest.id());
                        }
                        context.refresh();
                    }));
            slot += 2;
        }
        Menus.footer(view);
    }
}
