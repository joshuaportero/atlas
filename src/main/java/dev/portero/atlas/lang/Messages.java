package dev.portero.atlas.lang;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public interface Messages {

    interface Error {
        Arg0 NO_PERMISSION = () -> text("You don't have permission to do that!", RED);
        Arg0 INVALID_USAGE = () -> text("The command doesn't support the provided arguments!", RED);
    }

    interface Restart {
        Arg1<Integer> RESTART = (seconds) -> text("[SERVER]", RED)
                .appendSpace()
                .append(text("The server will restart in", GRAY))
                .appendSpace()
                .append(text(seconds, YELLOW))
                .appendSpace()
                .append(text("seconds!", GRAY));
        Arg0 RESTARTING = () -> text("[SERVER]", RED)
                .appendSpace()
                .append(text("The server is restarting!", GRAY));
        Arg1<String> CANCELLED = (sender) -> text("[SERVER]", AQUA)
                .appendSpace()
                .append(text("The server restart has been cancelled by", GRAY))
                .appendSpace()
                .append(text(sender, YELLOW))
                .append(text("!", GRAY));
        Arg0 TASK_NOT_FOUND = () -> text("You cannot cancel a restart that is not scheduled!", RED);
    }

    interface Arg0 {
        Component build();

        default void send(CommandSender sender) {
            sender.sendMessage(build());
        }

        default void broadcast() {
            for (CommandSender sender : Bukkit.getServer().getOnlinePlayers()) {
                sender.sendMessage(build());
            }
        }
    }

    interface Arg1<A0> {
        Component build(A0 a0);

        default void send(CommandSender sender, A0 a0) {
            sender.sendMessage(build(a0));
        }

        default void broadcast(A0 a0) {
            for (CommandSender sender : Bukkit.getServer().getOnlinePlayers()) {
                sender.sendMessage(build(a0));
            }
        }
    }

}
