package dev.portero.atlas;

import com.google.common.base.Stopwatch;
import dev.portero.atlas.bootstrap.AtlasBootstrap;
import dev.portero.atlas.bootstrap.ServiceRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class AtlasPlugin extends JavaPlugin {

    private AtlasBootstrap bootstrap;

    @Override
    public void onEnable() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        this.bootstrap = new AtlasBootstrap(this);
        this.bootstrap.start();
        this.getLogger().info("Atlas has been enabled in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms.");
    }

    @Override
    public void onDisable() {
        if (this.bootstrap != null) {
            this.bootstrap.stop();
        }
    }

    public ServiceRegistry services() {
        if (this.bootstrap == null) {
            throw new IllegalStateException("Atlas is not running");
        }
        return this.bootstrap.services();
    }
}
