package dev.portero.atlas.data.lifecycle;

import dev.portero.atlas.data.profile.Profile;
import dev.portero.atlas.data.profile.ProfileRepository;
import dev.portero.atlas.data.session.Session;
import dev.portero.atlas.data.session.SessionService;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.event.ProfileReadyEvent;
import dev.portero.atlas.event.ProfileSaveEvent;
import dev.portero.atlas.event.SessionCloseEvent;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.pipeline.PlayerCloseContext;
import dev.portero.atlas.pipeline.PlayerReadyContext;
import dev.portero.atlas.scheduler.AtlasScheduler;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class DataLifecycleService {

    private static final Component LOAD_FAILURE =
            Component.text("Failed to load your Atlas profile. Please try again.");

    private final ProfileRepository profiles;
    private final SessionService sessions;
    private final EventBus events;
    private final PipelineRegistry pipelines;
    private final AtlasScheduler scheduler;
    private final Map<UUID, Profile> pending = new ConcurrentHashMap<>();
    private final Set<CompletableFuture<?>> writes = ConcurrentHashMap.newKeySet();

    public DataLifecycleService(ProfileRepository profiles, SessionService sessions,
                                EventBus events, PipelineRegistry pipelines,
                                AtlasScheduler scheduler) {
        this.profiles = profiles;
        this.sessions = sessions;
        this.events = events;
        this.pipelines = pipelines;
        this.scheduler = scheduler;
    }

    public void handlePreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        try {
            Profile profile = this.profiles.findOrCreateNow(event.getUniqueId(), event.getName());
            this.pending.put(event.getUniqueId(), profile);
        } catch (Exception exception) {
            log.error("Failed to load profile for {}", event.getName(), exception);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LOAD_FAILURE);
        }
    }

    public void handleJoin(Player player) {
        Profile profile = this.pending.remove(player.getUniqueId());
        if (profile == null) {
            player.kick(LOAD_FAILURE);
            return;
        }

        Session session = this.sessions.open(player, profile);
        this.pipelines.require("player.ready", PlayerReadyContext.class)
                .execute(new PlayerReadyContext(player, profile, session));
        this.events.publish(new ProfileReadyEvent(player, profile, session));
    }

    public void handleQuit(Player player) {
        UUID uniqueId = player.getUniqueId();
        Profile profile = this.sessions.find(uniqueId)
                .map(Session::profile)
                .orElse(this.pending.remove(uniqueId));

        if (profile == null) {
            return;
        }

        this.pipelines.require("player.close", PlayerCloseContext.class)
                .execute(new PlayerCloseContext(uniqueId, profile, player));
        this.sessions.close(uniqueId);
        this.events.publish(new SessionCloseEvent(uniqueId, profile, player));
        this.persist(List.of(profile));
    }

    public void startAutosave(long periodTicks) {
        this.scheduler.syncRepeat(periodTicks, periodTicks, this::autosave);
    }

    public CompletableFuture<Void> flushAll() {
        List<Profile> dirty = this.collectDirty();
        CompletableFuture<Void> persist = dirty.isEmpty()
                ? CompletableFuture.completedFuture(null)
                : this.persist(dirty);
        CompletableFuture<?>[] inflight = this.writes.toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(persist, CompletableFuture.allOf(inflight));
    }

    private void autosave() {
        List<Profile> dirty = this.collectDirty();
        if (!dirty.isEmpty()) {
            this.persist(dirty);
        }
    }

    private List<Profile> collectDirty() {
        List<Profile> dirty = new ArrayList<>();
        this.sessions.sessions().forEach(session -> {
            if (session.profile().dirty()) {
                dirty.add(session.profile());
            }
        });
        this.pending.values().forEach(profile -> {
            if (profile.dirty()) {
                dirty.add(profile);
            }
        });
        return dirty;
    }

    private CompletableFuture<Void> persist(List<Profile> profiles) {
        profiles.forEach(profile -> this.events.publish(new ProfileSaveEvent(profile)));
        CompletableFuture<Void> future = this.profiles.saveAll(profiles)
                .orTimeout(15, TimeUnit.SECONDS)
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        profiles.forEach(Profile::markDirty);
                        log.error("Failed to persist {} profile(s)", profiles.size(), throwable);
                        return;
                    }
                    profiles.forEach(Profile::clearDirty);
                });
        this.writes.add(future);
        future.whenComplete((ignored, throwable) -> this.writes.remove(future));
        return future;
    }
}
