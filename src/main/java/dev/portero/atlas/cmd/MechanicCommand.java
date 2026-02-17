package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.mechanic.Mechanic;
import dev.portero.atlas.mechanic.MechanicManager;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

@Command(name = "mechanic")
@RequiredArgsConstructor
public class MechanicCommand {

    private final MechanicManager mechanicManager;

    @Execute
    public void base(@Context CommandSender sender) {
        Messages.Error.InvalidUsage.TITLE.send(sender, "/mechanic <list|enable|disable|reload> [mechanic]");
    }

    @Execute(name = "list")
    public void listMechanics(@Context CommandSender sender) {
        Messages.Mechanic.LIST_HEADER.send(sender);
        this.mechanicManager.getRegisteredMechanics().forEach(name -> {
            boolean enabled = this.mechanicManager.isMechanicEnabled(name);
            Messages.Mechanic.LIST_ITEM.send(sender, name, enabled);
        });
    }

    @Execute(name = "enable")
    public void enableMechanic(@Context CommandSender sender, @Arg Mechanic mechanic) {
        this.mechanicManager.enableMechanic(mechanic);
        Messages.Mechanic.ENABLED.send(sender, mechanic.getName());
    }

    @Execute(name = "disable")
    public void disableMechanic(@Context CommandSender sender, @Arg Mechanic mechanic) {
        this.mechanicManager.disableMechanic(mechanic);
        Messages.Mechanic.DISABLED.send(sender, mechanic.getName());
    }

    @Execute(name = "reload")
    public void reloadMechanics(@Context CommandSender sender) {
        this.mechanicManager.loadMechanics();
        Messages.Mechanic.RELOADED.send(sender);
    }
}
