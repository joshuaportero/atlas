package dev.portero.atlas.lang;

import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.TextComponent;
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
            Args1<String> ONLY_FIRST = (cmd) -> "&cThis command doesn't support the provided arguments!";
            Args1<String> TITLE = (cmd) -> "&cAvailable commands(" + cmd.split(" ")[0] + "):";
            Args1<String> ARGS = (cmd) -> {
                String[] args = cmd.split(" ");
                String[] colors = new String[]{"&7", "&c", "&e"};

                return "&8・" + IntStream.range(0, args.length)
                        .mapToObj(i -> colors[i % colors.length] + args[i])
                        .collect(Collectors.joining(" "));
            };
        }
    }

    interface Args0 {
        String message();

        default void send(CommandSender sender) {
            sender.sendMessage(serialize(this.message()));
        }

        default void broadcast() {
            broadcastAll(() -> serialize(this.message()));
        }
    }

    interface Args1<A0> {
        String message(A0 a0);

        default void send(CommandSender sender, A0 a0) {
            sender.sendMessage(serialize(this.message(a0)));
        }

        default void broadcast(A0 a0) {
            broadcastAll(() -> serialize(this.message(a0)));
        }
    }

    interface Args2<A0, A1> {
        String message(A0 a0, A1 a1);

        default void send(CommandSender sender, A0 a0, A1 a1) {
            sender.sendMessage(serialize(this.message(a0, a1)));
        }

        default void broadcast(A0 a0, A1 a1) {
            broadcastAll(() -> serialize(this.message(a0, a1)));
        }
    }

    interface Args3<A0, A1, A2> {
        String message(A0 a0, A1 a1, A2 a2);

        default void send(CommandSender sender, A0 a0, A1 a1, A2 a2) {
            sender.sendMessage(serialize(this.message(a0, a1, a2)));
        }

        default void broadcast(A0 a0, A1 a1, A2 a2) {
            broadcastAll(() -> serialize(this.message(a0, a1, a2)));
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
