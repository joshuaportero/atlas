package dev.portero.atlas.zombies.arena.setup;

import dev.portero.atlas.zombies.arena.Arena;
import dev.portero.atlas.zombies.arena.ArenaSettings;
import dev.portero.atlas.zombies.arena.BlockPos;
import dev.portero.atlas.zombies.arena.Cuboid;
import dev.portero.atlas.zombies.arena.DoorDef;
import dev.portero.atlas.zombies.arena.MysteryBoxDef;
import dev.portero.atlas.zombies.arena.WallBuyDef;
import dev.portero.atlas.zombies.arena.WindowDef;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Incremental arena builder held while an admin runs the setup wizard.
 * Values can be set in any order; {@link #missing()} reports what is left.
 */
@Getter
public class SetupSession {

    private static final int DEFAULT_BOX_PRICE = 950;
    private static final double BOX_MOVE_CHANCE = 0.10;

    private final String arenaId;
    private final List<Location> playerSpawns = new ArrayList<>();
    private final List<Location> zombieSpawns = new ArrayList<>();
    private final Map<String, WindowDef> windows = new LinkedHashMap<>();
    private final Map<String, DoorDef> doors = new LinkedHashMap<>();
    private final Map<String, WallBuyDef> wallBuys = new LinkedHashMap<>();
    private final List<Location> boxLocations = new ArrayList<>();
    private final Map<String, BlockPos> drinks = new LinkedHashMap<>();
    @Setter
    private BlockPos powerSwitch;
    @Setter
    private int boxPrice = DEFAULT_BOX_PRICE;
    @Setter
    private String displayName;
    @Setter
    private Cuboid region;
    @Setter
    private Location lobbySpawn;
    private ArenaSettings settings = ArenaSettings.defaults();

    private SetupSession(String arenaId) {
        this.arenaId = arenaId;
        this.displayName = arenaId;
    }

    /**
     * Starts a fresh setup session for a new arena.
     *
     * @param arenaId id of the arena to create
     * @return empty session
     */
    public static SetupSession create(String arenaId) {
        return new SetupSession(arenaId.toLowerCase());
    }

    /**
     * Starts a setup session pre-populated with an existing arena for editing.
     *
     * @param arena arena to edit
     * @return session holding copies of the arena's data
     */
    public static SetupSession edit(Arena arena) {
        SetupSession session = new SetupSession(arena.getId());
        session.displayName = arena.getDisplayName();
        session.region = arena.getRegion();
        session.lobbySpawn = arena.getLobbySpawn().clone();
        session.playerSpawns.addAll(arena.getPlayerSpawns());
        session.zombieSpawns.addAll(arena.getZombieSpawns());
        session.windows.putAll(arena.getWindows());
        session.doors.putAll(arena.getDoors());
        session.wallBuys.putAll(arena.getWallBuys());
        if (arena.getMysteryBox() != null) {
            session.boxPrice = arena.getMysteryBox().price();
            session.boxLocations.addAll(arena.getMysteryBox().locations());
        }
        session.drinks.putAll(arena.getDrinks());
        session.powerSwitch = arena.getPowerSwitch();
        session.settings = arena.getSettings();
        return session;
    }

    /**
     * Lists human-readable descriptions of everything still missing before the arena
     * can be saved and activated.
     *
     * @return missing items, empty when the arena is complete
     */
    public List<String> missing() {
        List<String> missing = new ArrayList<>();
        if (this.region == null) {
            missing.add("region (/za set region with a WorldEdit selection)");
        }
        if (this.lobbySpawn == null) {
            missing.add("lobby spawn (/za set lobbyspawn)");
        }
        if (this.playerSpawns.isEmpty()) {
            missing.add("at least one player spawn (/za set addplayerspawn)");
        }
        if (this.zombieSpawns.isEmpty()) {
            missing.add("at least one zombie spawn (/za set addzombiespawn)");
        }
        return missing;
    }

    public boolean isComplete() {
        return this.missing().isEmpty();
    }

    /**
     * Builds the immutable arena from the collected values.
     *
     * @param worldName world all locations live in
     * @return the arena definition
     */
    public Arena build(String worldName) {
        MysteryBoxDef mysteryBox = this.boxLocations.isEmpty()
                ? null
                : new MysteryBoxDef(this.boxPrice, BOX_MOVE_CHANCE, this.boxLocations);
        return new Arena(this.arenaId, this.displayName, worldName, this.region, this.lobbySpawn,
                this.playerSpawns, this.zombieSpawns, this.windows, this.doors,
                this.wallBuys, mysteryBox, this.drinks, this.powerSwitch, this.settings);
    }
}
