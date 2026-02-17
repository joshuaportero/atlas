package dev.portero.atlas.cmd;

import dev.portero.atlas.util.MessageUtil;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

@Command(name = "atlas")
public class AtlasCommand {

    @Execute
    public void execute(@Context CommandSender sender) {
        sender.sendMessage(MessageUtil.centerDecorated(NamedTextColor.GRAY, "&7[ &c&lINFO &7]"));
    }
}
