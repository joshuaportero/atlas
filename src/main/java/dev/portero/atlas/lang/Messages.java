package dev.portero.atlas.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public interface Messages {

    Arg0 WELCOME = () -> Component.text("Welcome to Atlas!")
            .color(NamedTextColor.GREEN);

    interface Arg0 {
        Component build();

        default void send(CommandSender sender) {
            sender.sendMessage(build());
        }
    }
}
