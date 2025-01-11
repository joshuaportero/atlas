package dev.portero.atlas.manager;

import dev.portero.atlas.lang.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RestartManager {

    private final Plugin plugin;
    private BukkitTask restartTask;

    public RestartManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void initiateRestartCountdown(int durationInSeconds) {
        this.restartTask = new BukkitRunnable() {
            private int count = durationInSeconds;

            @Override
            public void run() {
                if (count > 0) {
                    handleCountdownTick(count);
                } else if (count == 0) {
                    executeServerRestart();
                    this.cancel();
                }

                count--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void cancelRestart() {
        if (this.restartTask != null) {
            this.restartTask.cancel();
        }
    }

    public boolean isRestartTaskRunning() {
        return this.restartTask != null && !this.restartTask.isCancelled();
    }

    private void executeServerRestart() {
        Bukkit.getServer().savePlayers();
        Messages.Restart.RESTARTING.broadcast();
        this.playSoundForAllPlayers(1.5f);
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getServer().shutdown();
                this.cancel();
            }
        }.runTaskLater(plugin, 10);
    }

    private void handleCountdownTick(int remainingSeconds) {
        if (shouldNotify(remainingSeconds)) {
            Messages.Restart.RESTART.broadcast(remainingSeconds);
            this.playSoundForAllPlayers(1f);
        }
    }

    private boolean shouldNotify(int seconds) {
        return seconds % 60 == 0 || (seconds % 10 == 0 && seconds < 60) || seconds <= 5;
    }

    private void playSoundForAllPlayers(float pitch) {
        Bukkit.getServer().getOnlinePlayers().forEach(player ->
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, pitch)
        );
    }
}
