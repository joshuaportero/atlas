package dev.portero.atlas.menu.api;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class ClickContext {

    private final MenuView view;
    private final InventoryClickEvent event;

    public ClickContext(MenuView view, InventoryClickEvent event) {
        this.view = view;
        this.event = event;
    }

    public Player player() {
        return this.view.player();
    }

    public MenuView view() {
        return this.view;
    }

    public ClickType click() {
        return this.event.getClick();
    }

    public boolean left() {
        return this.event.isLeftClick();
    }

    public boolean right() {
        return this.event.isRightClick();
    }

    public boolean shift() {
        return this.event.isShiftClick();
    }

    public int slot() {
        return this.event.getRawSlot();
    }

    public void refresh() {
        this.view.refresh();
    }

    public void close() {
        this.view.service().close(this.player());
    }

    public void open(AtlasMenu menu) {
        this.view.service().open(this.player(), menu);
    }

    public void back() {
        this.view.service().back(this.player());
    }

    public void prompt(String message, java.util.function.Consumer<String> handler) {
        this.view.service().prompt(this.player(), message, handler);
    }
}
