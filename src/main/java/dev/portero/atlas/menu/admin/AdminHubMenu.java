package dev.portero.atlas.menu.admin;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class AdminHubMenu extends AtlasMenu {

    private final MenuFactory menus;

    public AdminHubMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&4Atlas Admin");
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        view.border(MenuItem.of(ItemFactory.pane()));
        view.set(11, MenuItem.of(ItemFactory.of(Material.PLAYER_HEAD, "&eOnline Players",
                        "&7Edit stats, resources, and skills",
                        "&eClick to browse"))
                .onClick(context -> context.open(this.menus.adminPlayers())));
        view.set(13, MenuItem.of(ItemFactory.of(Material.REDSTONE, "&cServer Settings",
                        "&7Autosave, regen rates, combat flag",
                        "&eClick to edit"))
                .onClick(context -> context.open(this.menus.adminSettings())));
        view.set(15, MenuItem.of(ItemFactory.of(Material.DIAMOND_SWORD, "&cCombat Control",
                        "&7Enable or disable the damage pipeline",
                        "&eClick to open"))
                .onClick(context -> context.open(this.menus.adminCombat())));
        view.set(21, MenuItem.of(ItemFactory.of(Material.BEACON, "&6World Events",
                        "&7Start or stop global events",
                        "&eClick to open"))
                .onClick(context -> context.open(this.menus.adminEvents())));
        view.set(23, MenuItem.of(ItemFactory.of(Material.ANVIL, "&eDatabase",
                        "&7Type: &f" + this.menus.database().getType().name().toLowerCase(),
                        "&7Online: &f" + this.menus.profiles().online().size()))
                .onClick(context -> context.refresh()));
        view.set(31, MenuItem.of(ItemFactory.close()).onClick(context -> context.close()));
    }
}
