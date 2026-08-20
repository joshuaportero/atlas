package dev.portero.atlas.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class AtlasScheduler {

    private final Plugin plugin;
    private final ExecutorService asyncExecutor;
    private final List<ScheduledTask> tasks = new ArrayList<>();

    public AtlasScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.asyncExecutor = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("atlas-async-", 0).factory());
    }

    public void sync(Runnable task) {
        this.plugin.getServer().getGlobalRegionScheduler().run(this.plugin, scheduled -> task.run());
    }

    public void sync(Entity entity, Runnable task) {
        entity.getScheduler().run(this.plugin, scheduled -> task.run(), null);
    }

    public ScheduledTask syncLater(long delayTicks, Runnable task) {
        ScheduledTask scheduledTask = this.plugin.getServer().getGlobalRegionScheduler()
                .runDelayed(this.plugin, scheduled -> task.run(), delayTicks);
        this.tasks.add(scheduledTask);
        return scheduledTask;
    }

    public ScheduledTask syncRepeat(long delayTicks, long periodTicks, Runnable task) {
        ScheduledTask scheduledTask = this.plugin.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(this.plugin, scheduled -> task.run(), delayTicks, periodTicks);
        this.tasks.add(scheduledTask);
        return scheduledTask;
    }

    public CompletableFuture<Void> async(Runnable task) {
        return CompletableFuture.runAsync(task, this.asyncExecutor);
    }

    public <T> CompletableFuture<T> async(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }, this.asyncExecutor);
    }

    public ExecutorService asyncExecutor() {
        return this.asyncExecutor;
    }

    public void shutdown() {
        for (ScheduledTask task : List.copyOf(this.tasks)) {
            task.cancel();
        }
        this.tasks.clear();
        this.asyncExecutor.shutdown();
        try {
            if (!this.asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                this.asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            this.asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
