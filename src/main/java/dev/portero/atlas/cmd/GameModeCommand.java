package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.shortcut.Shortcut;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@Command(name = "gamemode")
public class GameModeCommand {

    @Shortcut("gmc")
    @Execute(name = "creative")
    public void creative(@Context Player player, @OptionalArg Player target) {
        this.changeGameMode(player, target, GameMode.CREATIVE);
    }

    @Shortcut("gms")
    @Execute(name = "survival")
    public void survival(@Context Player player, @OptionalArg Player target) {
        this.changeGameMode(player, target, GameMode.SURVIVAL);
    }

    @Shortcut("gma")
    @Execute(name = "adventure")
    public void adventure(@Context Player player, @OptionalArg Player target) {
        this.changeGameMode(player, target, GameMode.ADVENTURE);
    }

    @Shortcut("gmsp")
    @Execute(name = "spectator")
    public void spectator(@Context Player player, @OptionalArg Player target) {
        this.changeGameMode(player, target, GameMode.SPECTATOR);
    }

    private void changeGameMode(Player player, Player target, GameMode gameMode) {
        target = (target == null) ? player : target;

        boolean isSelf = player.getUniqueId().equals(target.getUniqueId());
        String gameModeName = gameMode.name().toLowerCase();

        target.setGameMode(gameMode);

        if (isSelf) {
            Messages.GameMode.SELF.send(player, gameModeName);
        } else {
            Messages.GameMode.TARGET.send(player, target.getName(), gameModeName);
            Messages.GameMode.TARGET_SELF.send(target, gameModeName, player.getName());
        }
    }
}