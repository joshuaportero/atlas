package dev.portero.atlas.menu.player;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import dev.portero.atlas.player.AtlasPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

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
        return 5;
    }

    @Override
    public void populate(MenuView view) {
        AtlasPlayer atlas = this.menus.atlas(view.player());
        view.set(4, MenuItem.of(ItemFactory.of(Material.WRITABLE_BOOK, "&aPin Quest Note",
                        "&7Format: &fkey=value",
                        "&eClick and type in chat"))
                .onClick(context -> context.prompt("&eType &fkey=value &eor &ccancel", input -> {
                    if ("cancel".equalsIgnoreCase(input)) {
                        return;
                    }
                    int separator = input.indexOf('=');
                    if (separator <= 0) {
                        return;
                    }
                    atlas.questState(input.substring(0, separator).trim(),
                            input.substring(separator + 1).trim());
                })));

        int slot = 10;
        for (Map.Entry<String, String> entry : atlas.questStates().entrySet()) {
            String key = entry.getKey();
            view.set(slot, MenuItem.of(ItemFactory.of(Material.PAPER, "&e" + key,
                            "&7" + entry.getValue(),
                            "&cLeft-click to abandon"))
                    .onClick(context -> {
                        atlas.removeQuestState(key);
                        context.refresh();
                    }));
            slot++;
            if (slot % 9 == 8) {
                slot += 2;
            }
        }
        Menus.footer(view);
    }
}
