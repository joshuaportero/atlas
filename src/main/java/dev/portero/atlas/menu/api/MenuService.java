package dev.portero.atlas.menu.api;

import dev.portero.atlas.scheduler.AtlasScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class MenuService {

    private final Plugin plugin;
    private final AtlasScheduler scheduler;
    private final Map<UUID, Deque<AtlasMenu>> history = new ConcurrentHashMap<>();
    private final Map<UUID, Consumer<String>> prompts = new ConcurrentHashMap<>();

    public MenuService(Plugin plugin, AtlasScheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    public void openRoot(Player player, AtlasMenu menu) {
        this.history.remove(player.getUniqueId());
        this.open(player, menu);
    }

    public void open(Player player, AtlasMenu menu) {
        this.history.computeIfAbsent(player.getUniqueId(), key -> new ArrayDeque<>()).push(menu);
        this.render(player, menu);
    }

    public void back(Player player) {
        Deque<AtlasMenu> stack = this.history.get(player.getUniqueId());
        if (stack == null || stack.size() <= 1) {
            this.close(player);
            return;
        }
        stack.pop();
        AtlasMenu previous = stack.peek();
        if (previous == null) {
            this.close(player);
            return;
        }
        this.render(player, previous);
    }

    public void close(Player player) {
        this.history.remove(player.getUniqueId());
        player.closeInventory();
    }

    public void refresh(Player player) {
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }
        holder.view().refresh();
    }

    public Optional<MenuView> view(Player player) {
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder holder)) {
            return Optional.empty();
        }
        return Optional.ofNullable(holder.view());
    }

    public void prompt(Player player, String message, Consumer<String> handler) {
        this.prompts.put(player.getUniqueId(), handler);
        player.closeInventory();
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
        this.scheduler.syncLater(20L * 30L, () -> this.prompts.remove(player.getUniqueId(), handler));
    }

    public boolean completePrompt(Player player, String input) {
        Consumer<String> handler = this.prompts.remove(player.getUniqueId());
        if (handler == null) {
            return false;
        }
        this.scheduler.sync(player, () -> {
            handler.accept(input);
            Deque<AtlasMenu> stack = this.history.get(player.getUniqueId());
            if (player.isOnline() && stack != null && stack.peek() != null) {
                this.render(player, stack.peek());
            }
        });
        return true;
    }

    public boolean prompting(Player player) {
        return this.prompts.containsKey(player.getUniqueId());
    }

    private void render(Player player, AtlasMenu menu) {
        MenuHolder holder = new MenuHolder();
        int size = Math.max(9, Math.min(54, menu.rows() * 9));
        Component title = menu.title(player);
        Inventory inventory = this.plugin.getServer().createInventory(holder, size, title);
        MenuView view = new MenuView(this, menu, player, inventory);
        holder.view(view);
        menu.populate(view);
        player.openInventory(inventory);
    }
}
