package dev.portero.atlas.menu.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class MenuView {

    private final MenuService service;
    private final AtlasMenu menu;
    private final Player player;
    private final Inventory inventory;
    private final MenuItem[] items;

    public MenuView(MenuService service, AtlasMenu menu, Player player, Inventory inventory) {
        this.service = service;
        this.menu = menu;
        this.player = player;
        this.inventory = inventory;
        this.items = new MenuItem[inventory.getSize()];
    }

    public MenuService service() {
        return this.service;
    }

    public AtlasMenu menu() {
        return this.menu;
    }

    public Player player() {
        return this.player;
    }

    public Inventory inventory() {
        return this.inventory;
    }

    public void set(int slot, MenuItem item) {
        if (slot < 0 || slot >= this.items.length) {
            return;
        }
        this.items[slot] = item;
        this.inventory.setItem(slot, item.stack());
    }

    public void fill(MenuItem item) {
        for (int slot = 0; slot < this.items.length; slot++) {
            if (this.items[slot] == null) {
                this.set(slot, item);
            }
        }
    }

    public void border(MenuItem item) {
        int rows = this.inventory.getSize() / 9;
        for (int slot = 0; slot < this.inventory.getSize(); slot++) {
            int column = slot % 9;
            int row = slot / 9;
            if (row == 0 || row == rows - 1 || column == 0 || column == 8) {
                this.set(slot, item);
            }
        }
    }

    public MenuItem item(int slot) {
        if (slot < 0 || slot >= this.items.length) {
            return null;
        }
        return this.items[slot];
    }

    public void refresh() {
        for (int slot = 0; slot < this.items.length; slot++) {
            this.items[slot] = null;
            this.inventory.setItem(slot, null);
        }
        this.menu.populate(this);
    }
}
