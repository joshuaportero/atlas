package dev.portero.atlas.menu.player;

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

public final class ResourceMenu extends AtlasMenu {

    private final MenuFactory menus;

    public ResourceMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&8Resources");
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        AtlasPlayer atlas = this.menus.atlas(view.player());
        ResourceManager manager = this.menus.resources();
        view.border(MenuItem.of(ItemFactory.pane()));
        view.set(11, this.resource(atlas, manager, "mana", Material.LAPIS_LAZULI,
                "Meditate", "Spend 10 stamina to restore 25 mana"));
        view.set(13, this.resource(atlas, manager, "stamina", Material.COOKED_BEEF,
                "Rest", "Spend 20 energy to restore 40 stamina"));
        view.set(15, this.resource(atlas, manager, "energy", Material.GLOWSTONE_DUST,
                "Focus", "Spend 15 mana to restore 20 energy"));
        Menus.footer(view);
    }

    private MenuItem resource(AtlasPlayer atlas, ResourceManager manager, String id,
                              Material icon, String action, String detail) {
        ResourceType type = manager.registry().require(id);
        double current = manager.current(atlas, type);
        double max = manager.maximum(atlas, type);
        return MenuItem.of(ItemFactory.of(icon, "&b" + type.displayName(),
                        "&7" + format(current) + " / " + format(max),
                        "&eLeft-click: " + action,
                        "&7" + detail))
                .onClick(context -> {
                    boolean used = switch (id) {
                        case "mana" -> manager.consumeResource(atlas, "stamina", 10)
                                && this.restore(manager, atlas, "mana", 25);
                        case "stamina" -> manager.consumeResource(atlas, "energy", 20)
                                && this.restore(manager, atlas, "stamina", 40);
                        case "energy" -> manager.consumeResource(atlas, "mana", 15)
                                && this.restore(manager, atlas, "energy", 20);
                        default -> false;
                    };
                    if (used) {
                        context.refresh();
                    }
                });
    }

    private boolean restore(ResourceManager manager, AtlasPlayer atlas, String id, double amount) {
        manager.restore(atlas, manager.registry().require(id), amount);
        return true;
    }

    private static String format(double value) {
        return String.format("%.0f", value);
    }
}
