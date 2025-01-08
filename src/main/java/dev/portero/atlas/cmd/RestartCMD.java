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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Command(name = "restart")
public class RestartCMD {

    private static final int DEFAULT_RESTART_TIME = 10;

    private final Plugin plugin;
    private final RestartManager restartManager;

    @Execute
    public void execute(@Arg Optional<Integer> seconds) {
        this.startRestartCountdown(seconds.orElse(DEFAULT_RESTART_TIME));
    }

    @Execute(name = "cancel")
    public void cancel(@Context CommandSender sender) {
        this.handleCancel(sender);
    }

    private void startRestartCountdown(int seconds) {
        this.restartManager.setTask(new BukkitRunnable() {
            private int count = seconds;

            @Override
            public void run() {
                if (count > 0) {
                    handleCountdownTick(count);
                } else if (count == 0) {
                    handleRestart();
                } else {
                    plugin.getServer().shutdown();
                    this.cancel();
                }

                count--;
            }
        }.runTaskTimer(plugin, 0, 20));
    }

    private void handleCountdownTick(int seconds) {
        if ((seconds % 60 == 0) || (seconds % 10 == 0 && seconds < 60) || (seconds <= 5)) {
            Messages.Restart.RESTART.broadcast(seconds);
            this.playSoundForPlayers(this.getOnlinePlayers(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f);
        }
    }

    private void handleRestart() {
        Bukkit.getServer().savePlayers();
        Messages.Restart.RESTARTING.broadcast();
        this.playSoundForPlayers(this.getOnlinePlayers(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.5f);
    }

    private void handleCancel(CommandSender sender) {
        if (this.restartManager.getTask().isCancelled()) {
            Messages.Restart.TASK_NOT_FOUND.send(sender);
            return;
        }

        this.restartManager.getTask().cancel();
        Messages.Restart.CANCELLED.broadcast(sender instanceof Player ? sender.getName() : "CONSOLE");
        playSoundForPlayers(getOnlinePlayers(), Sound.BLOCK_ANVIL_USE, 1);
    }

    private void playSoundForPlayers(Collection<Player> players, Sound sound, float pitch) {
        players.forEach(player -> player.playSound(player.getLocation(), sound, 1, pitch));
    }

    private Collection<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
    }
}
