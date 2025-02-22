package dev.portero.atlas.command;

import dev.portero.atlas.cmd.AtlasCMD;
import dev.portero.atlas.handler.CustomInvalidUsageHandler;
import dev.portero.atlas.handler.MissingPermissionHandler;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.adventure.LiteAdventureExtension;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager {

    private final LiteCommands<CommandSender> liteCommands;

    public CommandManager(JavaPlugin plugin) {
        this.liteCommands = LiteBukkitFactory.builder("atlas", plugin)
                .commands(new AtlasCMD())
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

