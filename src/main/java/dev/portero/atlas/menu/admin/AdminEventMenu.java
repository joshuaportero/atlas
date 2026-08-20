package dev.portero.atlas.menu.admin;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import dev.portero.atlas.worldevent.ActiveWorldEvent;
import dev.portero.atlas.worldevent.WorldEventDefinition;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class AdminEventMenu extends AtlasMenu {

    private final MenuFactory menus;

    public AdminEventMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&4Events");
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        view.border(MenuItem.of(ItemFactory.pane()));
        int slot = 11;
        for (WorldEventDefinition definition : this.menus.events().definitions()) {
            ActiveWorldEvent active = this.menus.events().active(definition.id()).orElse(null);
            boolean running = active != null;
            String state = running
                    ? "&aRunning &8" + (active.remainingMillis() / 1000) + "s"
                    : "&7Stopped";
            view.set(slot, MenuItem.of(ItemFactory.of(definition.icon(), "&e" + definition.name(),
                            "&7" + definition.description(),
                            state,
                            running ? "&cClick to stop" : "&aClick to start"))
                    .onClick(context -> {
                        if (running) {
                            this.menus.events().stop(definition.id());
                        } else {
                            this.menus.events().start(definition.id());
                        }
                        context.refresh();
                    }));
            slot += 2;
        }
        Menus.footer(view);
    }
}
