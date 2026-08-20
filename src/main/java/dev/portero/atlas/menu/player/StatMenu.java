package dev.portero.atlas.menu.player;

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

import java.util.List;

public final class StatMenu extends AtlasMenu {

    private static final List<String> allocatable = List.of(
            "strength", "defense", "attack_damage", "magic_power",
            "max_health", "max_mana", "max_stamina", "max_energy");

    private final MenuFactory menus;

    public StatMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&8Stats");
    }

    @Override
    public int rows() {
        return 6;
    }

    @Override
    public void populate(MenuView view) {
        AtlasPlayer atlas = this.menus.atlas(view.player());
        int points = this.menus.stats().points(atlas);
        view.set(4, MenuItem.of(ItemFactory.of(Material.EXPERIENCE_BOTTLE, "&eUnspent Points",
                "&f" + points,
                points > 0 ? "&aLeft-click a stat to invest" : "&7Earn points from an admin")));

        int slot = 10;
        for (StatType type : this.menus.stats().registry().values()) {
            boolean spendable = allocatable.contains(type.id());
            double base = this.menus.stats().component(atlas).get(type);
            double total = atlas.stats().get(type);
            view.set(slot, MenuItem.of(ItemFactory.of(icon(type.id()), "&e" + type.displayName(),
                            "&7Base: &f" + format(base),
                            "&7Total: &a" + format(total),
                            spendable ? "&eLeft-click to spend 1 point" : "&7Not allocatable"))
                    .onClick(context -> {
                        if (spendable && this.menus.stats().spendPoint(atlas, type)) {
                            context.refresh();
                        }
                    }));
            slot++;
            if (slot % 9 == 8) {
                slot += 2;
            }
        }
        Menus.footer(view);
    }

    private static Material icon(String id) {
        return switch (id) {
            case "strength" -> Material.IRON_SWORD;
            case "defense" -> Material.SHIELD;
            case "crit_chance", "crit_damage" -> Material.GOLDEN_SWORD;
            case "attack_damage" -> Material.DIAMOND_SWORD;
            case "magic_power" -> Material.BLAZE_ROD;
            case "max_health" -> Material.GOLDEN_APPLE;
            case "max_mana" -> Material.LAPIS_LAZULI;
            case "max_stamina" -> Material.COOKED_BEEF;
            case "max_energy" -> Material.GLOWSTONE_DUST;
            default -> Material.PAPER;
        };
    }

    private static String format(double value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        return String.format("%.2f", value);
    }
}
