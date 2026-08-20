package dev.portero.atlas.command;

import dev.portero.atlas.handler.CustomInvalidUsageHandler;
import dev.portero.atlas.handler.MissingPermissionHandler;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.adventure.LiteAdventureExtension;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CommandManager {

    private final Plugin plugin;
    private LiteCommands<CommandSender> liteCommands;

    public CommandManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void register(Object... commands) {
        this.liteCommands = LiteBukkitFactory.builder("atlas", this.plugin)
                .commands(commands)
                .extension(new LiteAdventureExtension<>(), config -> config
                        .miniMessage(true)
                        .legacyColor(true)
                        .colorizeArgument(true)
                        .serializer(MiniMessage.miniMessage()))
                .missingPermission(new MissingPermissionHandler())
                .invalidUsage(new CustomInvalidUsageHandler())
                .build();
    }

    public void unregister() {
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }
    }
}
