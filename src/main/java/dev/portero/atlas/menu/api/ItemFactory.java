package dev.portero.atlas.menu.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public final class ItemFactory {

    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    private ItemFactory() {
    }

    public static ItemStack of(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        stack.editMeta(meta -> {
            meta.displayName(text(name));
            if (lore.length > 0) {
                List<Component> lines = new ArrayList<>();
                for (String line : lore) {
                    lines.add(text(line));
                }
                meta.lore(lines);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        });
        return stack;
    }

    public static ItemStack skull(OfflinePlayer owner, String name, String... lore) {
        ItemStack stack = of(Material.PLAYER_HEAD, name, lore);
        stack.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(owner));
        return stack;
    }

    public static ItemStack pane() {
        return of(Material.GRAY_STAINED_GLASS_PANE, " ");
    }

    public static ItemStack back() {
        return of(Material.ARROW, "&cBack", "&7Return to the previous menu");
    }

    public static ItemStack close() {
        return of(Material.BARRIER, "&cClose");
    }

    private static Component text(String value) {
        return legacy.deserialize(value).decoration(TextDecoration.ITALIC, false);
    }
}
