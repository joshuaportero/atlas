package dev.portero.atlas.handler;

import dev.portero.atlas.lang.Messages;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;
import org.bukkit.command.CommandSender;

public class CustomInvalidUsageHandler implements dev.rollczi.litecommands.invalidusage.InvalidUsageHandler<org.bukkit.command.CommandSender> {

    @Override
    public void handle(Invocation<CommandSender> invocation, InvalidUsage<CommandSender> result, ResultHandlerChain<CommandSender> chain) {
        CommandSender sender = invocation.sender();
        Schematic schematic = result.getSchematic();

        if (schematic.isOnlyFirst()) {
            Messages.Error.InvalidUsage.ONLY_FIRST.send(sender, schematic.first());
            return;
        }

        Messages.Error.InvalidUsage.TITLE.send(sender, schematic.first());
        schematic.all().forEach(command -> Messages.Error.InvalidUsage.ARGS.send(sender, command));
    }
}
