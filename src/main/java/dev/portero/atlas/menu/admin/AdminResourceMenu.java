package dev.portero.atlas.menu.admin;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.resource.ResourceManager;
import dev.portero.atlas.resource.ResourceType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class AdminResourceMenu extends AtlasMenu {

    private final MenuFactory menus;
    private final Player target;

    public AdminResourceMenu(MenuFactory menus, Player target) {
        this.menus = menus;
        this.target = target;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&4Resources: " + this.target.getName());
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        if (!this.target.isOnline()) {
            Menus.footer(view);
            return;
        }
        AtlasPlayer atlas = this.menus.atlas(this.target);
        ResourceManager manager = this.menus.resources();
        view.border(MenuItem.of(ItemFactory.pane()));
        int slot = 11;
        for (ResourceType type : manager.registry().values()) {
            view.set(slot, MenuItem.of(ItemFactory.of(Material.POTION, "&b" + type.displayName(),
                            "&7" + format(manager.current(atlas, type))
                                    + " / " + format(manager.maximum(atlas, type)),
                            "&eLeft +10  Right -10",
                            "&aShift-left fill  &cShift-right empty"))
                    .onClick(context -> {
                        if (context.shift() && context.left()) {
                            manager.fill(atlas);
                            manager.persist(atlas);
                        } else if (context.shift() && context.right()) {
                            atlas.resource(type.id(), 0);
                            manager.persist(atlas);
                        } else {
                            double delta = context.right() ? -10 : 10;
                            manager.restore(atlas, type, delta);
                        }
                        context.refresh();
                    }));
            slot += 2;
        }
        Menus.footer(view);
    }

    private static String format(double value) {
        return String.format("%.0f", value);
    }
}
