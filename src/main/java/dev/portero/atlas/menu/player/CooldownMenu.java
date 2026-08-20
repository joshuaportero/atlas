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

public final class CooldownMenu extends AtlasMenu {

    private final MenuFactory menus;

    public CooldownMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&8Cooldowns");
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        AtlasPlayer atlas = this.menus.atlas(view.player());
        view.border(MenuItem.of(ItemFactory.pane()));
        int slot = 10;
        Map<String, Long> cooldowns = atlas.cooldowns();
        if (cooldowns.isEmpty()) {
            view.set(13, MenuItem.of(ItemFactory.of(Material.CLOCK, "&eNo active cooldowns")));
        } else {
            for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
                long remaining = Math.max(0L, entry.getValue() - System.currentTimeMillis());
                view.set(slot, MenuItem.of(ItemFactory.of(Material.CLOCK, "&e" + entry.getKey(),
                                "&7Remaining: &f" + (remaining / 1000) + "s",
                                "&eClick to drop this timer"))
                        .onClick(context -> {
                            atlas.removeCooldown(entry.getKey());
                            context.refresh();
                        }));
                slot++;
            }
        }
        Menus.footer(view);
    }
}
