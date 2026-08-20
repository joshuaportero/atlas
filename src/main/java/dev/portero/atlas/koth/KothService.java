package dev.portero.atlas.koth;

import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.event.WorldEventStartedEvent;
import dev.portero.atlas.event.WorldEventStoppedEvent;
import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.level.LevelService;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.scheduler.AtlasScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class KothService {

    private final ProfileManager profiles;
    private final LevelService levels;
    private final AtlasScheduler scheduler;
    private final double radius;
    private final long xpReward;
    private final Map<UUID, Integer> scores = new ConcurrentHashMap<>();
    private volatile Location hill;
    private volatile boolean running;
    private volatile UUID king;
    private volatile boolean contested;
    private ScheduledTask ticker;

    public KothService(ProfileManager profiles, LevelService levels, AtlasScheduler scheduler,
                       EventBus events, double radius, long xpReward) {
        this.profiles = profiles;
        this.levels = levels;
        this.scheduler = scheduler;
        this.radius = radius;
        this.xpReward = xpReward;
        events.subscribe(WorldEventStartedEvent.class, event -> {
            if ("koth".equals(event.definition().id())) {
                this.start();
            }
        });
        events.subscribe(WorldEventStoppedEvent.class, event -> {
            if ("koth".equals(event.definition().id())) {
                this.stop();
            }
        });
    }

    public void setHill(Location location) {
        this.hill = location.clone();
    }

    public Location hill() {
        return this.hill == null ? null : this.hill.clone();
    }

    public boolean running() {
        return this.running;
    }

    public boolean start() {
        if (this.running) {
            return false;
        }
        if (this.hill == null) {
            Messages.Koth.NO_HILL.broadcast();
            return false;
        }
        this.running = true;
        this.scores.clear();
        this.king = null;
        Messages.Koth.STARTED.broadcast();
        this.ticker = this.scheduler.syncRepeat(20L, 20L, this::tick);
        return true;
    }

    public boolean stop() {
        if (!this.running) {
            return false;
        }
        this.running = false;
        if (this.ticker != null) {
            this.ticker.cancel();
            this.ticker = null;
        }
        UUID winner = this.leader();
        if (winner != null) {
            int score = this.scores.getOrDefault(winner, 0);
            Player player = Bukkit.getPlayer(winner);
            String name = player != null ? player.getName() : winner.toString().substring(0, 8);
            Messages.Koth.WINNER.broadcast(name, score);
            if (player != null) {
                this.profiles.find(player).ifPresent(atlas -> this.levels.addXp(atlas, this.xpReward));
            }
        } else {
            Messages.Koth.STOPPED.broadcast();
        }
        this.scores.clear();
        this.king = null;
        return true;
    }

    private void tick() {
        if (!this.running || this.hill == null || this.hill.getWorld() == null) {
            return;
        }
        java.util.List<Player> inside = this.hill.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distanceSquared(this.hill)
                        <= this.radius * this.radius)
                .toList();
        if (inside.isEmpty()) {
            this.king = null;
            return;
        }
        if (inside.size() > 1 && !this.sameParty(inside)) {
            if (!this.contested) {
                this.contested = true;
                Messages.Koth.CONTESTED.broadcast(Integer.toString(inside.size()));
            }
            this.king = null;
            return;
        }
        this.contested = false;
        Player holder = inside.get(0);
        this.scores.merge(holder.getUniqueId(), 1, Integer::sum);
        if (!holder.getUniqueId().equals(this.king)) {
            this.king = holder.getUniqueId();
            Messages.Koth.KING.broadcast(holder.getName());
        }
    }

    private boolean sameParty(java.util.List<Player> players) {
        UUID party = this.profiles.find(players.getFirst())
                .flatMap(AtlasPlayer::partyId)
                .orElse(null);
        if (party == null) {
            return false;
        }
        return players.stream().allMatch(player -> this.profiles.find(player)
                .flatMap(AtlasPlayer::partyId)
                .filter(party::equals)
                .isPresent());
    }

    private UUID leader() {
        UUID winner = null;
        int best = -1;
        for (Map.Entry<UUID, Integer> entry : this.scores.entrySet()) {
            if (entry.getValue() > best) {
                best = entry.getValue();
                winner = entry.getKey();
            }
        }
        return winner;
    }
}
