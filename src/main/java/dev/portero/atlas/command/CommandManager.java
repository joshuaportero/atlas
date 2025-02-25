package dev.portero.atlas.command;

import dev.portero.atlas.cmd.AtlasCommand;
import dev.portero.atlas.handler.CustomInvalidUsageHandler;
import dev.portero.atlas.handler.MissingPermissionHandler;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.adventure.LiteAdventureExtension;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class CommandManager {

    private final Plugin plugin;
    private LiteCommands<CommandSender> liteCommands;

    public void register() {
        this.liteCommands = LiteBukkitFactory.builder("atlas", this.plugin)
                .commands(new AtlasCommand())
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

