package dev.portero.atlas.zombies.arena;

import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Immutable definition of a playable zombies map. Never mutated by gameplay;
 * all per-game state lives in {@code GameSession}.
 */
@Getter
public class Arena {

    private final String id;
    private final String displayName;
    private final String worldName;
    private final Cuboid region;
    private final Location lobbySpawn;
    private final List<Location> playerSpawns;
    private final List<Location> zombieSpawns;
    private final Map<String, WindowDef> windows;
    private final Map<String, DoorDef> doors;
    private final Map<String, WallBuyDef> wallBuys;
    @Nullable
    private final MysteryBoxDef mysteryBox;
    private final Map<String, BlockPos> drinks;
    @Nullable
    private final BlockPos powerSwitch;
    private final ArenaSettings settings;

    public Arena(String id, String displayName, String worldName, Cuboid region, Location lobbySpawn,
            List<Location> playerSpawns, List<Location> zombieSpawns,
            Map<String, WindowDef> windows, Map<String, DoorDef> doors,
            Map<String, WallBuyDef> wallBuys, @Nullable MysteryBoxDef mysteryBox,
            Map<String, BlockPos> drinks, @Nullable BlockPos powerSwitch, ArenaSettings settings) {
        this.id = id;
        this.displayName = displayName;
        this.worldName = worldName;
        this.region = region;
        this.lobbySpawn = lobbySpawn.clone();
        this.playerSpawns = List.copyOf(playerSpawns);
        this.zombieSpawns = List.copyOf(zombieSpawns);
        this.windows = Map.copyOf(windows);
        this.doors = Map.copyOf(doors);
        this.wallBuys = Map.copyOf(wallBuys);
        this.mysteryBox = mysteryBox;
        this.drinks = Map.copyOf(drinks);
        this.powerSwitch = powerSwitch;
        this.settings = settings;
    }
}
