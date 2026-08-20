package dev.portero.atlas.data;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.data.lifecycle.ConnectionListener;
import dev.portero.atlas.data.lifecycle.DataLifecycleService;
import dev.portero.atlas.data.migration.MigrationService;
import dev.portero.atlas.data.migration.ProfileSchemaMigration;
import dev.portero.atlas.data.profile.ProfileComponentRegistry;
import dev.portero.atlas.data.profile.ProfileRepository;
import dev.portero.atlas.data.profile.SqlProfileRepository;
import dev.portero.atlas.data.session.SessionService;
import dev.portero.atlas.database.DatabaseManager;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.pipeline.PlayerCloseContext;
import dev.portero.atlas.pipeline.PlayerReadyContext;
import dev.portero.atlas.scheduler.AtlasScheduler;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class DataModule implements AtlasModule {

    private DataLifecycleService lifecycle;

    @Override
    public String id() {
        return "data";
    }

    @Override
    public void load(ModuleContext context) {
        context.services().register(ProfileComponentRegistry.class, new ProfileComponentRegistry());
        context.services().register(SessionService.class, new SessionService());
        context.service(PipelineRegistry.class).register("player.ready", PlayerReadyContext.class);
        context.service(PipelineRegistry.class).register("player.close", PlayerCloseContext.class);
    }

    @Override
    public void enable(ModuleContext context) {
        AtlasScheduler scheduler = context.service(AtlasScheduler.class);
        SqlExecutor executor = new SqlExecutor(
                context.service(DatabaseManager.class).getDataSource(), scheduler);
        context.services().register(SqlExecutor.class, executor);

        new MigrationService(executor, List.of(new ProfileSchemaMigration())).migrate();

        ProfileRepository profiles = new SqlProfileRepository(
                executor, context.service(ProfileComponentRegistry.class));
        context.services().register(ProfileRepository.class, profiles);

        this.lifecycle = new DataLifecycleService(
                profiles,
                context.service(SessionService.class),
                context.service(EventBus.class),
                context.service(PipelineRegistry.class),
                scheduler);
        context.services().register(DataLifecycleService.class, this.lifecycle);

        Plugin plugin = context.plugin();
        plugin.getServer().getPluginManager().registerEvents(
                new ConnectionListener(this.lifecycle), plugin);

        YamlConfiguration config = context.service(ConfigManager.class).getConfig(ConfigType.DEFAULT);
        long seconds = config != null ? config.getLong("data.autosave-seconds", 300L) : 300L;
        this.lifecycle.startAutosave(Math.max(seconds, 30L) * 20L);
        log.info("Data lifecycle enabled (autosave {}s)", seconds);
    }

    @Override
    public void disable(ModuleContext context) {
        if (this.lifecycle == null) {
            return;
        }

        try {
            this.lifecycle.flushAll().orTimeout(20, TimeUnit.SECONDS).join();
        } catch (Exception exception) {
            log.error("Failed to flush profiles on shutdown", exception);
        }
    }
}
