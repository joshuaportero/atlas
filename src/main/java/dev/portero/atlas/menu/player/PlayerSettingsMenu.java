package dev.portero.atlas.menu.player;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.SettingsComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class PlayerSettingsMenu extends AtlasMenu {

    private final MenuFactory menus;

    public PlayerSettingsMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&8Settings");
    }

    @Override
    public int rows() {
        return 3;
    }

    @Override
    public void populate(MenuView view) {
        AtlasPlayer atlas = this.menus.atlas(view.player());
        SettingsComponent settings = this.menus.settings(atlas);
        view.border(MenuItem.of(ItemFactory.pane()));
        view.set(11, MenuItem.of(ItemFactory.of(
                        settings.resourceRegen() ? Material.LIME_DYE : Material.GRAY_DYE,
                        settings.resourceRegen() ? "&aResource Regen: On" : "&cResource Regen: Off",
                        "&7Pause natural mana/stamina/energy regen",
                        "&eClick to toggle"))
                .onClick(context -> {
                    settings.resourceRegen(!settings.resourceRegen());
                    atlas.profile().attach(settings);
                    context.refresh();
                }));
        view.set(13, MenuItem.of(ItemFactory.of(
                        settings.combatFeedback() ? Material.REDSTONE_TORCH : Material.LEVER,
                        settings.combatFeedback() ? "&aHit Feedback: On" : "&cHit Feedback: Off",
                        "&7Floating damage numbers on hits",
                        "&eClick to toggle"))
                .onClick(context -> {
                    settings.combatFeedback(!settings.combatFeedback());
                    atlas.profile().attach(settings);
                    context.refresh();
                }));
        view.set(15, MenuItem.of(ItemFactory.of(
                        settings.eventOptIn() ? Material.NETHER_STAR : Material.FIREWORK_STAR,
                        settings.eventOptIn() ? "&aEvents: On" : "&cEvents: Off",
                        "&eClick to toggle"))
                .onClick(context -> {
                    this.menus.events().setOptIn(atlas, !settings.eventOptIn());
                    context.refresh();
                }));
        Menus.footer(view);
    }
}
