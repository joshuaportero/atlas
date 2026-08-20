package dev.portero.atlas.cmd;

import dev.portero.atlas.koth.KothService;
import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.worldevent.WorldEventService;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;

@Command(name = "koth")
public class KothCommand {

    private final KothService koth;
    private final WorldEventService events;

    public KothCommand(KothService koth, WorldEventService events) {
        this.koth = koth;
        this.events = events;
    }

    @Execute(name = "set")
    @Permission("atlas.admin")
    public void set(@Context Player player) {
        this.koth.setHill(player.getLocation());
        Messages.Koth.SET.send(player);
    }

    @Execute(name = "start")
    @Permission("atlas.admin")
    public void start(@Context Player player) {
        if (this.koth.hill() == null) {
            Messages.Koth.NO_HILL.send(player);
            return;
        }
        if (!this.events.start("koth")) {
            if (this.koth.running()) {
                Messages.Koth.ALREADY.send(player);
                return;
            }
            this.koth.start();
        }
    }

    @Execute(name = "stop")
    @Permission("atlas.admin")
    public void stop(@Context Player player) {
        if (!this.events.stop("koth") && !this.koth.stop()) {
            Messages.Koth.NOT_RUNNING.send(player);
        }
    }

    @Execute
    public void info(@Context Player player) {
        if (!this.koth.running()) {
            Messages.Koth.NOT_RUNNING.send(player);
            return;
        }
        Messages.Koth.STARTED.send(player);
    }
}
