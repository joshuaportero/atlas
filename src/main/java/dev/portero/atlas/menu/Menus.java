package dev.portero.atlas.menu;

import dev.portero.atlas.menu.api.ClickContext;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Menus {

    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    private Menus() {
    }

    public static Component title(String value) {
        return legacy.deserialize(value);
    }

    public static void footer(MenuView view) {
        int last = view.inventory().getSize() - 9;
        view.set(last, MenuItem.of(ItemFactory.back()).onClick(ClickContext::back));
        view.set(last + 8, MenuItem.of(ItemFactory.close()).onClick(ClickContext::close));
    }
}
