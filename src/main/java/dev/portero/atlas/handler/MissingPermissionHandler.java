package dev.portero.atlas.handler;

import dev.portero.atlas.lang.Messages;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.permission.MissingPermissions;
import dev.rollczi.litecommands.permission.MissingPermissionsHandler;
import org.bukkit.command.CommandSender;

public class MissingPermissionHandler implements MissingPermissionsHandler<CommandSender> {

    @Override
    public void handle(Invocation<CommandSender> invocation, MissingPermissions missingPermissions,
                       ResultHandlerChain<CommandSender> chain) {
        Messages.Error.NO_PERMISSION.send(invocation.sender());
    }
}
