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
                String[] colors = {"&7", "&c", "&e"};

                return "&8・" + IntStream.range(0, args.length)
                        .mapToObj(i -> colors[i % colors.length] + args[i])
                        .collect(Collectors.joining(" "));
            };
        }
    }

    interface Level {
        Args2<Integer, Integer> UP = (level, points) -> "&aLevel up! You are now level &e"
                + level + "&a and gained &e" + points + " &astat points.";
        Args1<Integer> INFO = level -> "&7You are level &e" + level + "&7.";
        Args2<Long, Long> XP = (xp, needed) -> "&7XP: &e" + xp + "&7/&e" + needed;
    }

    interface Party {
        Args0 CREATED = () -> "&aParty created.";
        Args0 ALREADY = () -> "&cYou are already in a party.";
        Args0 NOT_IN = () -> "&cYou are not in a party.";
        Args0 NOT_LEADER = () -> "&cOnly the party leader can do that.";
        Args0 NO_INVITE = () -> "&cYou have no pending party invite.";
        Args0 FULL = () -> "&cThat party is full.";
        Args0 DISBANDED = () -> "&cThe party was disbanded.";
        Args0 LEFT = () -> "&7You left the party.";
        Args1<String> INVITED = name -> "&e" + name + " &7invited you to a party. Use &e/party accept&7.";
        Args1<String> INVITE_SENT = name -> "&7Invite sent to &e" + name + "&7.";
        Args1<String> JOINED = name -> "&e" + name + " &7joined the party.";
        Args1<String> KICKED = name -> "&e" + name + " &7was kicked from the party.";
        Args1<String> CHAT = text -> "&d[Party] &f" + text;
    }

    interface Quest {
        Args1<String> STARTED = name -> "&aQuest started: &e" + name;
        Args1<String> ABANDONED = name -> "&cQuest abandoned: &e" + name;
        Args2<String, Long> COMPLETED = (name, xp) -> "&aQuest complete: &e" + name
                + " &7(+" + xp + " XP)";
        Args0 UNKNOWN = () -> "&cUnknown quest.";
        Args0 ALREADY = () -> "&cYou already have that quest.";
        Args0 NOT_ACTIVE = () -> "&cThat quest is not active.";
    }

    interface Skill {
        Args0 OPEN = () -> "&7Opening skills.";
        Args1<String> EQUIPPED = name -> "&aEquipped skill: &e" + name;
        Args1<String> UNEQUIPPED = name -> "&7Unequipped skill: &e" + name;
        Args0 FAILED = () -> "&cYou cannot equip that skill.";
    }

    interface Koth {
        Args0 NO_HILL = () -> "&cNo hill location is set. Use &e/koth set&c.";
        Args0 SET = () -> "&aHill location set to your position.";
        Args0 STARTED = () -> "&6King of the Hill has begun!";
        Args0 STOPPED = () -> "&6King of the Hill has ended.";
        Args0 ALREADY = () -> "&cKing of the Hill is already running.";
        Args0 NOT_RUNNING = () -> "&cKing of the Hill is not running.";
        Args1<String> KING = name -> "&6" + name + " &7holds the hill!";
        Args1<String> CONTESTED = name -> "&cThe hill is contested! &7(" + name + " players)";
        Args2<String, Integer> WINNER = (name, score) -> "&6" + name
                + " &awins King of the Hill with &e" + score + " &apoints!";
    }

    interface GameMode {
        Args1<String> SELF = player -> "&7Your game mode has been changed to &e" + player + "&7.";
        Args2<String, String> TARGET = (target, gamemode) -> "&7You have changed &e" + target
                + "'s &7game mode to &e" + gamemode + "&7.";
        Args2<String, String> TARGET_SELF = (gamemode, player) -> "&7Your game mode has been changed to &e"
                + gamemode + "&7 by &e" + player + "&7.";
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