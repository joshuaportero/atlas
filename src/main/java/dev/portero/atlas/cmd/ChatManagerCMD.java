package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.manager.ChatManager;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.shortcut.Shortcut;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@Command(name = "chatmanager", aliases = {"cm"})
public class ChatManagerCMD {

    private final ChatManager chatManager;

    @Execute(name = "slow")
    public void execute(@Context CommandSender sender, @Arg int seconds) {
        if (seconds < 1) {
            if (chatManager.getSlowModeSeconds() == 0) {
                Messages.ChatManager.SLOW_MODE_ALREADY_DISABLED.send(sender);
                return;
            }

            chatManager.setSlowModeSeconds(0);
            Messages.ChatManager.SLOW_MODE_DISABLED.broadcast();
            return;
        }

        if(chatManager.getSlowModeSeconds() == seconds) {
            Messages.ChatManager.SLOW_MODE_ALREADY.send(sender, seconds);
            return;
        }

        chatManager.setSlowModeSeconds(seconds);
        Messages.ChatManager.SLOW_MODE_ENABLED.broadcast(seconds);
    }

    @Execute(name = "enable")
    public void enable(@Context CommandSender sender) {
        if (!chatManager.isChatDisabled()) {
            Messages.ChatManager.CHAT_ALREADY_ENABLED.send(sender);
            return;
        }

        chatManager.setChatDisabled(false);
        Messages.ChatManager.CHAT_ENABLED.broadcast();
    }

    @Execute(name = "disable")
    public void disable(@Context CommandSender sender) {
        if (chatManager.isChatDisabled()) {
            Messages.ChatManager.CHAT_ALREADY_DISABLED.send(sender);
            return;
        }

        chatManager.setChatDisabled(true);
        Messages.ChatManager.CHAT_DISABLED.broadcast();
    }

    @Execute(name = "clear")
    @Shortcut("cc")
    public void clear(@Context CommandSender sender) {
        for (Player player : sender.getServer().getOnlinePlayers()) {
            for (int i = 0; i < 30; i++) {
                player.sendMessage(Component.empty());
            }
        }
        Messages.ChatManager.CHAT_CLEARED.broadcast(sender instanceof Player ? sender.getName() : "CONSOLE");
    }
}
