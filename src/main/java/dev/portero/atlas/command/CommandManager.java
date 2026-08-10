package dev.portero.atlas.command;

import dev.portero.atlas.cmd.AtlasCommand;
import dev.portero.atlas.cmd.ZombiesCommand;
import dev.portero.atlas.handler.CustomInvalidUsageHandler;
import dev.portero.atlas.handler.MissingPermissionHandler;
import dev.portero.atlas.zombies.ZombiesModule;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.adventure.LiteAdventureExtension;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public class CommandManager {

    private final Plugin plugin;
    private final ZombiesModule zombiesModule;
    private @Nullable LiteCommands<CommandSender> liteCommands;

    public void register() {
        this.liteCommands = LiteBukkitFactory
                .builder("atlas", this.plugin)
                .commands(
                        new AtlasCommand(),
                        new ZombiesCommand(this.zombiesModule)
                )
                .extension(
                        new LiteAdventureExtension<>(),
                        config -> config
                                .miniMessage(true)
                                .legacyColor(true)
                                .colorizeArgument(true)
                                .serializer(MiniMessage.miniMessage()))

                .missingPermission(
                        new MissingPermissionHandler())
                .invalidUsage(
                        new CustomInvalidUsageHandler())
                .build();
    }

    public void unregister() {
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }
    }
}
