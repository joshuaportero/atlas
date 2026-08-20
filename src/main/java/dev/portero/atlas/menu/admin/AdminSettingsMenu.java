package dev.portero.atlas.menu.admin;

import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import dev.portero.atlas.resource.ResourceType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public final class AdminSettingsMenu extends AtlasMenu {

    private final MenuFactory menus;

    public AdminSettingsMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&4Server Settings");
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        YamlConfiguration config = this.menus.configs().getConfig(ConfigType.DEFAULT);
        long autosave = config != null ? config.getLong("data.autosave-seconds", 300L) : 300L;
        view.border(MenuItem.of(ItemFactory.pane()));
        view.set(10, MenuItem.of(ItemFactory.of(Material.CLOCK, "&eAutosave",
                        "&7" + autosave + " seconds",
                        "&eLeft +30  Right -30",
                        "&7Applies after restart"))
                .onClick(context -> {
                    if (config == null) {
                        return;
                    }
                    long next = Math.max(30L, autosave + (context.right() ? -30 : 30));
                    config.set("data.autosave-seconds", next);
                    this.menus.configs().saveConfig(ConfigType.DEFAULT);
                    context.refresh();
                }));

        int slot = 12;
        for (ResourceType type : this.menus.resources().registry().values()) {
            view.set(slot, MenuItem.of(ItemFactory.of(Material.EXPERIENCE_BOTTLE,
                            "&b" + type.displayName() + " Regen",
                            "&7" + type.regenPerSecond() + "/s",
                            "&eLeft +0.5  Right -0.5"))
                    .onClick(context -> {
                        double next = type.regenPerSecond() + (context.right() ? -0.5 : 0.5);
                        type.regenPerSecond(next);
                        if (config != null) {
                            config.set("resources." + type.id() + ".regen-per-second",
                                    type.regenPerSecond());
                            this.menus.configs().saveConfig(ConfigType.DEFAULT);
                        }
                        context.refresh();
                    }));
            slot++;
        }
        Menus.footer(view);
    }
}
