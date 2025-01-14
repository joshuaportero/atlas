package dev.portero.atlas;

import dev.portero.atlas.cmd.AtlasCMD;
import dev.portero.atlas.cmd.ChatManagerCMD;
import dev.portero.atlas.cmd.MechanicCMD;
import dev.portero.atlas.cmd.RestartCMD;
import dev.portero.atlas.handler.CustomInvalidUsageHandler;
import dev.portero.atlas.handler.MissingPermissionHandler;
import dev.portero.atlas.listener.ChatManagerListener;
import dev.portero.atlas.listener.mechanic.TntExplodeListener;
import dev.portero.atlas.manager.ChatManager;
import dev.portero.atlas.manager.RestartManager;
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
        RestartManager restartManager = new RestartManager(this);
        ChatManager chatManager = new ChatManager();

        this.loadConfig();

        this.liteCommands = LiteBukkitFactory.builder("atlas", this)
                .commands(
                        new AtlasCMD(this),
                        new RestartCMD(restartManager),
                        new ChatManagerCMD(chatManager),
                        new MechanicCMD(this)
                )
                .extension(new LiteAdventureExtension<>(), config -> config
                        .miniMessage(true)
                        .legacyColor(true)
                        .colorizeArgument(true)
                        .serializer(this.miniMessage)
                )
                .missingPermission(new MissingPermissionHandler())
                .invalidUsage(new CustomInvalidUsageHandler())
                .build();

        this.getServer().getPluginManager().registerEvents(new ChatManagerListener(chatManager), this);
        this.getServer().getPluginManager().registerEvents(new TntExplodeListener(this), this);
    }

    @Override
    public void onDisable() {
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }
    }

    private void loadConfig() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
    }
}