package dev.portero.atlas.lang;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public interface Messages {

    Arg0 ERROR_NO_PERMISSION = () -> text("You don't have permission to do that!", RED);
    Arg0 ERROR_INVALID_USAGE = () -> text("The command doesn't support the provided arguments!", RED);

    Arg0 WELCOME = () -> text("Welcome to Atlas!", GOLD);

    interface Arg0 {
        Component build();

        default void send(CommandSender sender) {
            sender.sendMessage(build());
        }
    }
}
