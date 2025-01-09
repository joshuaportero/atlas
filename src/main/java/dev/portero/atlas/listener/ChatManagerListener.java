package dev.portero.atlas.listener;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.manager.ChatManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
public class ChatManagerListener implements Listener {

    private final ChatManager chatManager;

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (chatManager.isDisabled()) {
            event.setCancelled(true);
            Messages.ChatManager.DISABLED.send(player);
            return;
        }

        if (chatManager.getSlowModeSeconds() > 0) {
            Map<String, Instant> messageCooldowns = chatManager.getMessageCooldowns();
            if (messageCooldowns.containsKey(player.getName())) {
                Instant lastMessage = messageCooldowns.get(player.getName());
                long timeSinceLastMessage = System.currentTimeMillis() - lastMessage.toEpochMilli();
                if (timeSinceLastMessage < chatManager.getSlowModeSeconds() * 1000L) {
                    event.setCancelled(true);
                    Messages.ChatManager.SLOWED.send(player, (int) (chatManager.getSlowModeSeconds() - timeSinceLastMessage / 1000L));
                    return;
                } else {
                    messageCooldowns.remove(player.getName());
                }
            }
            messageCooldowns.put(player.getName(), Instant.now());
        }
    }
}
