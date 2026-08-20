package dev.portero.atlas.menu.admin;

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

public final class AdminPlayerMenu extends AtlasMenu {

    private final MenuFactory menus;
    private final Player target;

    public AdminPlayerMenu(MenuFactory menus, Player target) {
        this.menus = menus;
        this.target = target;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&4" + this.target.getName());
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        if (!this.target.isOnline()) {
            view.set(13, MenuItem.of(ItemFactory.of(Material.BARRIER, "&cPlayer offline")));
            Menus.footer(view);
            return;
        }

        AtlasPlayer atlas = this.menus.atlas(this.target);
        view.border(MenuItem.of(ItemFactory.pane()));
        view.set(4, MenuItem.of(ItemFactory.skull(this.target, "&e" + this.target.getName(),
                "&7Points: &f" + this.menus.stats().points(atlas),
                "&7Absorption: &f" + String.format("%.0f", atlas.absorption()))));
        view.set(11, MenuItem.of(ItemFactory.of(Material.IRON_SWORD, "&eEdit Stats",
                        "&eClick to open"))
                .onClick(context -> context.open(this.menus.adminStats(this.target))));
        view.set(12, MenuItem.of(ItemFactory.of(Material.EXPERIENCE_BOTTLE, "&eGrant 5 Stat Points",
                        "&eClick to grant"))
                .onClick(context -> {
                    this.menus.stats().addPoints(atlas, 5);
                    context.refresh();
                }));
        view.set(13, MenuItem.of(ItemFactory.of(Material.POTION, "&bEdit Resources",
                        "&eClick to open"))
                .onClick(context -> context.open(this.menus.adminResources(this.target))));
        view.set(14, MenuItem.of(ItemFactory.of(Material.ENCHANTED_BOOK, "&dEdit Skills",
                        "&eClick to open"))
                .onClick(context -> context.open(this.menus.adminSkills(this.target))));
        view.set(15, MenuItem.of(ItemFactory.of(Material.CLOCK, "&eClear Cooldowns",
                        "&eClick to clear"))
                .onClick(context -> {
                    atlas.clearCooldowns();
                    context.refresh();
                }));
        view.set(21, MenuItem.of(ItemFactory.of(Material.GOLDEN_APPLE, "&eSet Absorption 20",
                        "&eLeft-click set 20  &cRight-click clear"))
                .onClick(context -> {
                    atlas.absorption(context.right() ? 0 : 20);
                    context.refresh();
                }));
        view.set(23, MenuItem.of(ItemFactory.of(Material.DARK_OAK_DOOR, "&cClear Party",
                        "&eClick to reset party id"))
                .onClick(context -> {
                    atlas.partyId(null);
                    context.refresh();
                }));
        Menus.footer(view);
    }
}
