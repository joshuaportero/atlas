package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import org.bukkit.command.CommandSender;

@Command(name = "atlas")
public class AtlasCMD {

    @Execute
    public void execute(@Context CommandSender sender) {
        Messages.WELCOME.send(sender);
    }
}
