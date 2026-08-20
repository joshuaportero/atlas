package dev.portero.atlas.player;

import dev.portero.atlas.data.profile.Profile;
import dev.portero.atlas.data.session.Session;
import dev.portero.atlas.stat.StatModifier;
import dev.portero.atlas.stat.StatSnapshot;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AtlasPlayer {

    private final Player handle;
    private final Session session;
    private final Map<String, List<StatModifier>> modifiers = new ConcurrentHashMap<>();
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<String, String> questStates = new ConcurrentHashMap<>();
    private final Map<String, Double> resources = new ConcurrentHashMap<>();
    private volatile StatSnapshot stats = StatSnapshot.empty();
    private volatile UUID partyId;
    private volatile double absorption;
    private volatile boolean dirtyResources;

    public AtlasPlayer(Player handle, Session session) {
        this.handle = handle;
        this.session = session;
    }

    public Player handle() {
        return this.handle;
    }

    public Session session() {
        return this.session;
    }

    public Profile profile() {
        return this.session.profile();
    }

    public UUID uniqueId() {
        return this.handle.getUniqueId();
    }

    public String name() {
        return this.handle.getName();
    }

    public StatSnapshot stats() {
        return this.stats;
    }

    public void stats(StatSnapshot stats) {
        this.stats = stats;
    }

    public void addModifier(StatModifier modifier) {
        this.modifiers.computeIfAbsent(modifier.source(), key -> new ArrayList<>()).add(modifier);
    }

    public void removeModifiers(String source) {
        this.modifiers.remove(source);
    }

    public List<StatModifier> modifiers() {
        return this.modifiers.values().stream().flatMap(Collection::stream).toList();
    }

    public double resource(String type) {
        return this.resources.getOrDefault(type, 0.0);
    }

    public void resource(String type, double amount) {
        this.resources.put(type, Math.max(0.0, amount));
        this.dirtyResources = true;
    }

    public Map<String, Double> resources() {
        return Map.copyOf(this.resources);
    }

    public boolean dirtyResources() {
        return this.dirtyResources;
    }

    public void clearDirtyResources() {
        this.dirtyResources = false;
    }

    public boolean onCooldown(String key) {
        Long expiresAt = this.cooldowns.get(key);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt <= System.currentTimeMillis()) {
            this.cooldowns.remove(key);
            return false;
        }
        return true;
    }

    public void cooldown(String key, long durationMillis) {
        this.cooldowns.put(key, System.currentTimeMillis() + durationMillis);
    }

    public long cooldownRemaining(String key) {
        Long expiresAt = this.cooldowns.get(key);
        if (expiresAt == null) {
            return 0L;
        }
        return Math.max(0L, expiresAt - System.currentTimeMillis());
    }

    public Optional<UUID> partyId() {
        return Optional.ofNullable(this.partyId);
    }

    public void partyId(UUID partyId) {
        this.partyId = partyId;
    }

    public double absorption() {
        return this.absorption;
    }

    public void absorption(double absorption) {
        this.absorption = Math.max(0.0, absorption);
    }

    public Optional<String> questState(String key) {
        return Optional.ofNullable(this.questStates.get(key));
    }

    public void questState(String key, String value) {
        this.questStates.put(key, value);
        this.profile().markDirty();
    }

    public Map<String, String> questStates() {
        return Map.copyOf(this.questStates);
    }

    public void questStates(Map<String, String> states) {
        this.questStates.clear();
        this.questStates.putAll(states);
    }
}
