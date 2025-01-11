package dev.portero.atlas.listener;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.manager.ChatManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class ChatManagerListener implements Listener {

    private final ChatManager chatManager;

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (chatManager.isChatDisabled()) {
            event.setCancelled(true);
            Messages.ChatManager.DISABLED.send(player);
            return;
        }

        if (chatManager.getSlowModeDuration() > 0) {
            if (chatManager.isUserOnCooldown(player.getUniqueId())) {
                int remainingCooldown = chatManager.getRemainingCooldown(player.getUniqueId());
                if (remainingCooldown <= 0) {
                    chatManager.refreshUserCooldown(player.getUniqueId());
                    return;
                }
                Messages.ChatManager.SLOWED.send(player, remainingCooldown);
                event.setCancelled(true);
                return;
            }
            chatManager.addUserToCooldown(player.getUniqueId());
        }
    }
}
