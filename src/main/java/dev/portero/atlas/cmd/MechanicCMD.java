package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.mechanic.MechanicType;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.execute.Execute;
import org.bukkit.plugin.Plugin;

@Command(name = "mechanicmanager", aliases = {"mm"})
public class MechanicCMD {

    private final Plugin plugin;

    public MechanicCMD(Plugin plugin) {
        this.plugin = plugin;
    }

    @Execute(name = "enable")
    public void enable(@Arg MechanicType mechanic) {
        plugin.getConfig().set(mechanic.getPath(), true);
        plugin.saveConfig();
        Messages.MechanicCommand.ENABLED.broadcast(mechanic);
    }

    @Execute(name = "disable")
    public void disable(@Arg MechanicType mechanic) {
        plugin.getConfig().set(mechanic.getPath(), false);
        plugin.saveConfig();
        Messages.MechanicCommand.DISABLED.broadcast(mechanic);
    }
}
