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

public final class ProfileMenu extends AtlasMenu {

    private final MenuFactory menus;

    public ProfileMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&8Profile");
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        AtlasPlayer atlas = this.menus.atlas(view.player());
        String party = atlas.partyId().map(id -> id.toString().substring(0, 8)).orElse("None");
        view.border(MenuItem.of(ItemFactory.pane()));
        int level = this.menus.levels().level(atlas);
        view.set(13, MenuItem.of(ItemFactory.skull(view.player(), "&e" + atlas.name(),
                "&7Level: &f" + level,
                "&7XP: &f" + this.menus.levels().xp(atlas) + "/"
                        + this.menus.levels().xpForNext(level),
                "&7Absorption: &f" + String.format("%.0f", atlas.absorption()),
                "&7Party: &f" + party)));
        view.set(20, MenuItem.of(ItemFactory.of(Material.GOLDEN_APPLE, "&eDischarge Absorption",
                        "&7Current: &f" + String.format("%.0f", atlas.absorption()),
                        "&eClick to clear personal absorption"))
                .onClick(context -> {
                    atlas.absorption(0);
                    context.refresh();
                }));
        view.set(22, MenuItem.of(ItemFactory.of(Material.DARK_OAK_DOOR, "&cLeave Party",
                        "&7Current: &f" + party,
                        "&eClick to clear your party id"))
                .onClick(context -> {
                    this.menus.parties().leave(atlas.uniqueId(), true);
                    context.refresh();
                }));
        view.set(24, MenuItem.of(ItemFactory.of(Material.NAME_TAG, "&eRefresh Name",
                        "&7Stored: &f" + atlas.profile().name(),
                        "&eClick to sync the profile name"))
                .onClick(context -> {
                    atlas.profile().name(view.player().getName());
                    context.refresh();
                }));
        Menus.footer(view);
    }
}
