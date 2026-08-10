package dev.portero.atlas.zombies.game;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Per-player state of one game run: points, kills, survival status, owned perks and the
 * data needed to return the player to the exact spot (and game mode) they joined from.
 */
@Getter
public class PlayerSession {

    private final UUID playerId;
    private final Location returnLocation;
    private final GameMode returnGameMode;
    private final Set<String> perks = new HashSet<>();
    private int points;
    private int kills;
    private boolean alive = true;
    private boolean downed;

    public PlayerSession(UUID playerId, Location returnLocation, GameMode returnGameMode, int startingPoints) {
        this.playerId = playerId;
        this.returnLocation = returnLocation.clone();
        this.returnGameMode = returnGameMode;
        this.points = startingPoints;
    }

    public void addPoints(int amount) {
        this.points += amount;
    }

    public void addKill() {
        this.kills++;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setDowned(boolean downed) {
        this.downed = downed;
    }

    public void addPerk(String perkId) {
        this.perks.add(perkId);
    }

    public boolean removePerk(String perkId) {
        return this.perks.remove(perkId);
    }

    public boolean hasPerk(String perkId) {
        return this.perks.contains(perkId);
    }

    public Set<String> perks() {
        return new HashSet<>(this.perks);
    }

    public void clearPerks() {
        this.perks.clear();
    }
}
