package dev.portero.atlas.lang;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface Messages {

    interface Error {
        Arg0 NO_PERMISSION = () -> "&cYou don't have permission to do that!";
        Arg0 INVALID_USAGE = () -> "&cThe command doesn't support the provided arguments!";
    }

    interface ChatManager {
        Arg1<Integer> SLOW_MODE_ENABLED = (seconds) -> "&9[CHAT] &7Slow mode &aenabled&7! By &e" + seconds + "&7 seconds!";
        Arg1<Integer> SLOW_MODE_ALREADY = (seconds) -> "&cSlow mode is already enabled by &e" + seconds + "&c seconds!";

        Arg0 SLOW_MODE_DISABLED = () -> "&9[CHAT] &7Slow mode is now &cdisabled&7!";
        Arg0 SLOW_MODE_ALREADY_DISABLED = () -> "&cSlow mode is already disabled!";

        Arg0 CHAT_ENABLED = () -> "&9[CHAT] &7The chat is now &aenabled&7!";
        Arg0 CHAT_ALREADY_ENABLED = () -> "&cThe chat is already enabled!";

        Arg0 CHAT_DISABLED = () -> "&9[CHAT] &7The chat is now &cdisabled&7!";
        Arg0 CHAT_ALREADY_DISABLED = () -> "&cThe chat is already disabled!";

        Arg1<String> CHAT_CLEARED = (sender) -> "&9[CHAT] &7The chat has been &bcleared &7by &e" + sender + "&7!";

        Arg0 DISABLED = () -> "&cYou cannot speak while the chat is disabled!";

        Arg1<Integer> SLOWED = (seconds) -> "&cWait &e" + seconds + "&c seconds before sending another message!";

    }

    interface Restart {
        Arg1<Integer> RESTART = (seconds) -> "&c[SERVER] &7The server will &drestart &7in &e" + seconds + "&7 seconds!";
        Arg0 RESTARTING = () -> "&c[SERVER] &7The server is &drestarting&7!";
        Arg1<String> CANCELLED = (sender) -> "&c[SERVER] &7The server &drestart &7has been &ccancelled &7by &e" + sender + "&7!";
        Arg0 TASK_NOT_FOUND = () -> "&cYou cannot cancel a restart that is not scheduled!";
    }

    interface Arg0 {
        String message();

        default void send(CommandSender sender) {
            sender.sendMessage(serialize(message()));
        }

        default void broadcast() {
            broadcastAll(() -> serialize(message()));
        }
    }

    interface Arg1<A0> {
        String message(A0 a0);

        default void send(CommandSender sender, A0 a0) {
            sender.sendMessage(serialize(message(a0)));
        }

        default void broadcast(A0 a0) {
            broadcastAll(() -> serialize(message(a0)));
        }
    }

    static @NotNull TextComponent serialize(@NotNull String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    static void broadcastAll(Supplier<TextComponent> supplier) {
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(supplier.get()));
        Bukkit.getConsoleSender().sendMessage(supplier.get());
    }
}
