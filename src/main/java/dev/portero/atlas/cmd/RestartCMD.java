package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.manager.RestartManager;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
@Command(name = "restart")
public class RestartCMD {

    private static final int DEFAULT_RESTART_TIME = 10;
    private final RestartManager restartManager;

    @Execute
    public void execute(@Context CommandSender sender, @Arg Optional<Integer> seconds) {
        if (restartManager.isRestartTaskRunning()) {
            Messages.Restart.ALREADY_RUNNING.send(sender);
            return;
        }

        restartManager.initiateRestartCountdown(seconds.orElse(DEFAULT_RESTART_TIME));
    }

    @Execute(name = "cancel")
    public void cancel(@Context CommandSender sender) {
        if (!restartManager.isRestartTaskRunning()) {
            Messages.Restart.TASK_NOT_FOUND.send(sender);
            return;
        }

        restartManager.cancelRestart();
        Messages.Restart.CANCELLED.broadcast(sender instanceof Player ? sender.getName() : "CONSOLE");
        Bukkit.getOnlinePlayers().forEach(player ->
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1)
        );
    }
}
