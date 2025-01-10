package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.mechanic.MechanicType;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.execute.Execute;

@Command(name = "mechanicmanager", aliases = {"mm"})
public class MechanicCMD {

    @Execute(name = "enable")
    public void enable(@Arg MechanicType mechanic) {
        Messages.MechanicCommand.ENABLED.broadcast(mechanic);
    }

    @Execute(name = "disable")
    public void disable(@Arg MechanicType mechanic) {
        Messages.MechanicCommand.DISABLED.broadcast(mechanic);
    }
}
