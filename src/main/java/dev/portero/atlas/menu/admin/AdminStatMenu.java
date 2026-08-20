package dev.portero.atlas.menu.admin;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.stat.StatType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public final class AdminStatMenu extends AtlasMenu {

    private final MenuFactory menus;
    private final Player target;

    public AdminStatMenu(MenuFactory menus, Player target) {
        this.menus = menus;
        this.target = target;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&4Stats: " + this.target.getName());
    }

    @Override
    public int rows() {
        return 6;
    }

    @Override
    public void populate(MenuView view) {
        if (!this.target.isOnline()) {
            Menus.footer(view);
            return;
        }
        AtlasPlayer atlas = this.menus.atlas(this.target);
        int slot = 10;
        for (StatType type : this.menus.stats().registry().values()) {
            double base = this.menus.stats().component(atlas).get(type);
            double step = type.id().contains("chance") || type.id().contains("crit_damage") ? 0.01 : 1.0;
            view.set(slot, MenuItem.of(ItemFactory.of(Material.PAPER, "&e" + type.displayName(),
                            "&7Base: &f" + base,
                            "&eLeft +  Right -  Shift x10",
                            "&bDrop: type a value in chat"))
                    .onClick(context -> {
                        if (context.click() == ClickType.DROP
                                || context.click() == ClickType.CONTROL_DROP) {
                            context.prompt("&eEnter a new value for " + type.id(), input -> {
                                try {
                                    this.menus.stats().setBase(atlas, type, Double.parseDouble(input));
                                } catch (NumberFormatException ignored) {
                                    return;
                                }
                            });
                            return;
                        }
                        double delta = step * (context.shift() ? 10 : 1) * (context.right() ? -1 : 1);
                        this.menus.stats().setBase(atlas, type, base + delta);
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
