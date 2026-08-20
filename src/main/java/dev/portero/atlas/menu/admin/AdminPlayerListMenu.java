package dev.portero.atlas.menu.admin;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class AdminPlayerListMenu extends AtlasMenu {

    private final MenuFactory menus;
    private int page;

    public AdminPlayerListMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&4Players");
    }

    @Override
    public int rows() {
        return 6;
    }

    @Override
    public void populate(MenuView view) {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        int pageSize = 45;
        int start = this.page * pageSize;
        int end = Math.min(start + pageSize, online.size());
        int slot = 0;
        for (int index = start; index < end; index++) {
            Player target = online.get(index);
            view.set(slot, MenuItem.of(ItemFactory.skull(target, "&e" + target.getName(),
                            "&7Click to manage"))
                    .onClick(context -> context.open(this.menus.adminPlayer(target))));
            slot++;
        }
        view.set(45, MenuItem.of(ItemFactory.of(Material.ARROW, "&ePrevious"))
                .onClick(context -> {
                    this.page = Math.max(0, this.page - 1);
                    context.refresh();
                }));
        view.set(49, MenuItem.of(ItemFactory.back()).onClick(context -> context.back()));
        view.set(53, MenuItem.of(ItemFactory.of(Material.ARROW, "&eNext"))
                .onClick(context -> {
                    if (end < online.size()) {
                        this.page++;
                        context.refresh();
                    }
                }));
    }
}
