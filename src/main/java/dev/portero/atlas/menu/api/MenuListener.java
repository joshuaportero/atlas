package dev.portero.atlas.menu.api;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class MenuListener implements Listener {

    private final MenuService menus;

    public MenuListener(MenuService menus) {
        this.menus = menus;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder) || holder.view() == null) {
            return;
        }

        MenuView view = holder.view();
        if (view.menu().cancelClicks()) {
            event.setCancelled(true);
        }
        if (event.getRawSlot() >= view.inventory().getSize()) {
            event.setCancelled(true);
            return;
        }

        MenuItem item = view.item(event.getRawSlot());
        if (item != null) {
            item.click().handle(new ClickContext(view, event));
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!this.menus.prompting(player)) {
            return;
        }
        event.setCancelled(true);
        String input = PlainTextComponentSerializer.plainText().serialize(event.message());
        this.menus.completePrompt(player, input);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.menus.close(event.getPlayer());
    }
}
