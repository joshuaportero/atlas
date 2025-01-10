package dev.portero.atlas.lang;

import dev.portero.atlas.mechanic.MechanicType;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface Messages {

    interface Error {
        Arg0 NO_PERMISSION = () -> "&cYou don't have permission to do that!";

        interface InvalidUsage {
            Arg1<String> ONLY_FIRST = (cmd) -> "&cThis command doesn't support the provided arguments!";
            Arg1<String> TITLE = (cmd) -> "&cAvailable commands(" + cmd.split(" ")[0] + "):";
            Arg1<String> ARGS = (cmd) -> {
                String[] args = cmd.split(" ");
                StringBuilder builder = new StringBuilder();
                String[] colors = new String[]{"&7", "&c"};

                builder.append("&8・");
                for (int i = 0; i < args.length; i++) {
                    builder.append(colors[i % colors.length]).append(args[i]).append(" ");
                }
                return builder.toString();
            };
        }
    }

    interface MechanicCommand {
        Arg1<MechanicType> ENABLED = (mechanic) -> "&9[MECHANIC] &7The mechanic &e" + mechanic + "&7 is now &aenabled&7!";
        Arg1<MechanicType> DISABLED = (mechanic) -> "&9[MECHANIC] &7The mechanic &e" + mechanic + "&7 is now &cdisabled&7!";
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

    interface Arg2<A0, A1> {
        String message(A0 a0, A1 a1);

        default void send(CommandSender sender, A0 a0, A1 a1) {
            sender.sendMessage(serialize(message(a0, a1)));
        }

        default void broadcast(A0 a0, A1 a1) {
            broadcastAll(() -> serialize(message(a0, a1)));
        }
    }

    interface Arg3<A0, A1, A2> {
        String message(A0 a0, A1 a1, A2 a2);

        default void send(CommandSender sender, A0 a0, A1 a1, A2 a2) {
            sender.sendMessage(serialize(message(a0, a1, a2)));
        }

        default void broadcast(A0 a0, A1 a1, A2 a2) {
            broadcastAll(() -> serialize(message(a0, a1, a2)));
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
