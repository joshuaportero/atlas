package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import org.bukkit.entity.Player;

@Command(name = "do")
public class DoCommand {

    @Execute
    public void execute(@Context Player player, @Arg String... message) {
        Messages.Do.EXECUTE.broadcast(player.getName(), String.join(" ", message));
    }
}
