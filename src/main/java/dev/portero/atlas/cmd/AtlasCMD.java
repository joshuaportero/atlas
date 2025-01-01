package dev.portero.atlas.cmd;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import static dev.portero.atlas.util.MessageUtil.centerDecorated;
import static dev.portero.atlas.util.MessageUtil.format;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;

@Command(name = "atlas")
public class AtlasCMD {

    private final Plugin plugin;

    public AtlasCMD(Plugin plugin) {
        this.plugin = plugin;
    }


    @Execute
    @SuppressWarnings("UnstableApiUsage")
    public void execute(@Context CommandSender sender) {
        PluginMeta pluginMeta = plugin.getPluginMeta();

        String name = pluginMeta.getName();
        String version = pluginMeta.getVersion();
        String author = String.join(", ", pluginMeta.getAuthors());

        sender.sendMessage(centerDecorated(AQUA, "&r&7[ &bAtlas33 &7→ &cINFO &7]"));
        sender.sendMessage(format("&bName: &7" + name));
        sender.sendMessage(format("&bVersion: &7" + version));
        sender.sendMessage(format("&bAuthor: &7" + author));
        sender.sendMessage(centerDecorated(AQUA, ""));
    }
}
