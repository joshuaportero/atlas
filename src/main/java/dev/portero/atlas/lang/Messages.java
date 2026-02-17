package dev.portero.atlas.lang;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Messages {

    interface Error {
        Args0 NO_PERMISSION = () -> "&cYou don't have permission to do that!";

        interface InvalidUsage {
            Args1<String> ONLY_FIRST = cmd -> "&cThis command doesn't support the provided arguments!";
            Args1<String> TITLE = cmd -> "&cAvailable commands(" + cmd.split(" ")[0] + "):";
            Args1<String> ARGS = cmd -> {
                String[] args = cmd.split(" ");
                String[] colors = { "&7", "&c", "&e" };

                return "&8・" + IntStream.range(0, args.length)
                        .mapToObj(i -> colors[i % colors.length] + args[i])
                        .collect(Collectors.joining(" "));
            };
        }
    }

    interface Mechanic {
        Args1<String> ENABLED = name -> "&aMechanic &e" + name + " &ahas been enabled!";
        Args1<String> DISABLED = name -> "&cMechanic &e" + name + " &chas been disabled!";
        Args0 LIST_HEADER = () -> "&8&m----------------[&r &6Mechanics &8&m]----------------";
        Args2<String, Boolean> LIST_ITEM = (name, enabled) -> " &8- &e" + name + " &8(&" + (enabled ? "a" : "c")
                + (enabled ? "enabled" : "disabled") + "&8)";
        Args0 RELOADED = () -> "&aMechanics configuration reloaded!";
    }

    interface ArgsBase {
        default void send(CommandSender sender, Supplier<String> messageSupplier) {
            sender.sendMessage(serialize(messageSupplier.get()));
        }

        default void broadcast(Supplier<String> messageSupplier) {
            broadcastAll(() -> serialize(messageSupplier.get()));
        }
    }

    interface Args0 extends ArgsBase {
        String message();

        default void send(CommandSender sender) {
            send(sender, this::message);
        }

        default void broadcast() {
            broadcast(this::message);
        }
    }

    interface Args1<A0> extends ArgsBase {
        String message(A0 a0);

        default void send(CommandSender sender, A0 a0) {
            this.send(sender, () -> this.message(a0));
        }

        default void broadcast(A0 a0) {
            this.broadcast(() -> this.message(a0));
        }
    }

    interface Args2<A0, A1> extends ArgsBase {
        String message(A0 a0, A1 a1);

        default void send(CommandSender sender, A0 a0, A1 a1) {
            send(sender, () -> this.message(a0, a1));
        }

        default void broadcast(A0 a0, A1 a1) {
            broadcast(() -> this.message(a0, a1));
        }
    }

    interface Args3<A0, A1, A2> extends ArgsBase {
        String message(A0 a0, A1 a1, A2 a2);

        default void send(CommandSender sender, A0 a0, A1 a1, A2 a2) {
            send(sender, () -> this.message(a0, a1, a2));
        }

        default void broadcast(A0 a0, A1 a1, A2 a2) {
            broadcast(() -> this.message(a0, a1, a2));
        }
    }

    static @NotNull TextComponent serialize(@NotNull String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    static void broadcastAll(Supplier<TextComponent> supplier) {
        TextComponent message = supplier.get();
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(message));
        Bukkit.getConsoleSender().sendMessage(message);
    }
}