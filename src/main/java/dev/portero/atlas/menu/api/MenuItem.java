package dev.portero.atlas.menu.api;

import org.bukkit.inventory.ItemStack;

public final class MenuItem {

    private final ItemStack stack;
    private ClickHandler click = context -> {
    };

    private MenuItem(ItemStack stack) {
        this.stack = stack;
    }

    public static MenuItem of(ItemStack stack) {
        return new MenuItem(stack);
    }

    public MenuItem onClick(ClickHandler click) {
        this.click = click;
        return this;
    }

    public ItemStack stack() {
        return this.stack;
    }

    public ClickHandler click() {
        return this.click;
    }
}
