package dev.portero.atlas.menu.player;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.SettingsComponent;
import dev.portero.atlas.worldevent.ActiveWorldEvent;
import dev.portero.atlas.worldevent.WorldEventDefinition;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class EventMenu extends AtlasMenu {

    private final MenuFactory menus;

    public EventMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&8Events");
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        AtlasPlayer atlas = this.menus.atlas(view.player());
        SettingsComponent settings = this.menus.settings(atlas);
        view.border(MenuItem.of(ItemFactory.pane()));
        view.set(4, MenuItem.of(ItemFactory.of(
                        settings.eventOptIn() ? Material.LIME_DYE : Material.GRAY_DYE,
                        settings.eventOptIn() ? "&aEvent Participation: On" : "&cEvent Participation: Off",
                        "&7Opted-in players receive event modifiers",
                        "&eClick to toggle"))
                .onClick(context -> {
                    this.menus.events().setOptIn(atlas, !settings.eventOptIn());
                    context.refresh();
                }));

        int slot = 10;
        for (WorldEventDefinition definition : this.menus.events().definitions()) {
            ActiveWorldEvent active = this.menus.events().active(definition.id()).orElse(null);
            String state = active == null ? "&7Inactive" : "&aActive &8" + (active.remainingMillis() / 1000) + "s";
            view.set(slot, MenuItem.of(ItemFactory.of(definition.icon(), "&e" + definition.name(),
                    "&7" + definition.description(),
                    state)));
            slot += 2;
        }
        Menus.footer(view);
    }
}
