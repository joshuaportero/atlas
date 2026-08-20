package dev.portero.atlas.menu.admin;

import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public final class AdminCombatMenu extends AtlasMenu {

    private final MenuFactory menus;

    public AdminCombatMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&4Combat");
    }

    @Override
    public int rows() {
        return 3;
    }

    @Override
    public void populate(MenuView view) {
        boolean enabled = this.menus.combat().enabled();
        view.border(MenuItem.of(ItemFactory.pane()));
        view.set(13, MenuItem.of(ItemFactory.of(
                        enabled ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD,
                        enabled ? "&aCombat Pipeline: On" : "&cCombat Pipeline: Off",
                        "&7Physical hits use Atlas scaling when on",
                        "&eClick to toggle"))
                .onClick(context -> {
                    boolean next = !this.menus.combat().enabled();
                    this.menus.combat().enabled(next);
                    YamlConfiguration config = this.menus.configs().getConfig(ConfigType.DEFAULT);
                    if (config != null) {
                        config.set("combat.enabled", next);
                        this.menus.configs().saveConfig(ConfigType.DEFAULT);
                    }
                    context.refresh();
                }));
        Menus.footer(view);
    }
}
