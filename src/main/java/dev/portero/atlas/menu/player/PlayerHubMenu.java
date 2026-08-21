package dev.portero.atlas.menu.player;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class PlayerHubMenu extends AtlasMenu {

    private final MenuFactory menus;

    public PlayerHubMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&8Atlas");
    }

    @Override
    public int rows() {
        return 5;
    }

    @Override
    public void populate(MenuView view) {
        Player player = view.player();
        view.border(MenuItem.of(ItemFactory.pane()));
        view.set(10, MenuItem.of(ItemFactory.skull(player, "&eProfile",
                        "&7Identity, party, and absorption", "&eClick to manage"))
                .onClick(context -> context.open(this.menus.profileMenu())));
        view.set(12, MenuItem.of(ItemFactory.of(Material.IRON_SWORD, "&eStats",
                        "&7Spend points and review totals", "&eClick to open"))
                .onClick(context -> context.open(this.menus.statMenu())));
        view.set(14, MenuItem.of(ItemFactory.of(Material.EXPERIENCE_BOTTLE, "&bResources",
                        "&7Convert mana, stamina, and energy", "&eClick to open"))
                .onClick(context -> context.open(this.menus.resourceMenu())));
        view.set(16, MenuItem.of(ItemFactory.of(Material.ENCHANTED_BOOK, "&dSkills",
                        "&7Equip up to 3 passive skills", "&eClick to open"))
                .onClick(context -> context.open(this.menus.skillMenu())));
        view.set(20, MenuItem.of(ItemFactory.of(Material.NETHER_STAR, "&6Events",
                        "&7Opt in or out of world events", "&eClick to open"))
                .onClick(context -> context.open(this.menus.eventMenu())));
        view.set(22, MenuItem.of(ItemFactory.of(Material.SHIELD, "&cCombat",
                        "&7Toggle hit feedback", "&eClick to open"))
                .onClick(context -> context.open(this.menus.combatSettingsMenu())));
        view.set(24, MenuItem.of(ItemFactory.of(Material.WHITE_BANNER, "&dParty",
                        "&7Create or manage your party", "&eClick to open"))
                .onClick(context -> context.open(this.menus.partyMenu())));
        view.set(30, MenuItem.of(ItemFactory.of(Material.CLOCK, "&eCooldowns",
                        "&7Track remaining ability timers", "&eClick to open"))
                .onClick(context -> context.open(this.menus.cooldownMenu())));
        view.set(32, MenuItem.of(ItemFactory.of(Material.COMPARATOR, "&7Settings",
                        "&7Regen, events, and combat options", "&eClick to open"))
                .onClick(context -> context.open(this.menus.settingsMenu())));
        view.set(40, MenuItem.of(ItemFactory.close()).onClick(context -> context.close()));
    }
}
