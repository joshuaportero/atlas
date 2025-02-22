package dev.portero.atlas.handler;

import dev.portero.atlas.lang.Messages;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.permission.MissingPermissions;
import org.bukkit.command.CommandSender;

public class MissingPermissionHandler implements dev.rollczi.litecommands.permission.MissingPermissionsHandler<org.bukkit.command.CommandSender> {

    @Override
    public void handle(Invocation<CommandSender> invocation, MissingPermissions missingPermissions, ResultHandlerChain<CommandSender> chain) {
        Messages.Error.NO_PERMISSION.send(invocation.sender());
    }
}
