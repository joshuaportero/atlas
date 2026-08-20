package dev.portero.atlas.menu.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class MenuHolder implements InventoryHolder {

    private MenuView view;

    public MenuView view() {
        return this.view;
    }

    public void view(MenuView view) {
        this.view = view;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.view.inventory();
    }
}
