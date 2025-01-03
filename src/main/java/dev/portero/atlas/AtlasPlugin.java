package dev.portero.atlas;

import dev.portero.atlas.cmd.AtlasCMD;
import dev.portero.atlas.handler.CustomInvalidUsageHandler;
import dev.portero.atlas.handler.MissingPermissionHandler;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.adventure.LiteAdventureExtension;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AtlasPlugin extends JavaPlugin {

    private LiteCommands<CommandSender> liteCommands;

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        this.liteCommands = LiteBukkitFactory.builder("atlas", this)
                .commands(new AtlasCMD())
                .extension(new LiteAdventureExtension<>(), config -> config
                        .miniMessage(true)
                        .legacyColor(true)
                        .colorizeArgument(true)
                        .serializer(this.miniMessage)
                )
                .missingPermission(new MissingPermissionHandler())
                .invalidUsage(new CustomInvalidUsageHandler())
                .build();
    }

    @Override
    public void onDisable() {
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }
    }
}