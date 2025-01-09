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

    interface Restart {
        Arg1<Integer> RESTART = (seconds) -> "&c[SERVER] &7The server will restart in &e" + seconds + "&7 seconds!";
        Arg0 RESTARTING = () -> "&c[SERVER] &7The server is restarting!";
        Arg1<String> CANCELLED = (sender) -> "&b[SERVER] &7The server restart has been cancelled by &e" + sender + "&7!";
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
            sender.sendMessage(message(a0));
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
