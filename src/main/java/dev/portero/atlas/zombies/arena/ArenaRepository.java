package dev.portero.atlas.zombies.arena;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads and saves arena definitions under {@code <dataFolder>/zombies/arenas/}.
 * Each arena is a {@code <id>.yml} file; its WorldEdit snapshot lives next to it
 * as {@code <id>.schem}.
 */
@Slf4j
public class ArenaRepository {

    private static final String SCHEMATIC_EXTENSION = ".schem";

    private final Plugin plugin;
    private final File folder;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    public ArenaRepository(Plugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "zombies" + File.separator + "arenas");
    }

    /**
     * (Re)loads every arena file from disk. Broken files are skipped with an error log.
     */
    public void loadAll() {
        this.arenas.clear();
        if (!this.folder.exists() && !this.folder.mkdirs()) {
            log.error("Could not create arena folder {}", this.folder.getAbsolutePath());
            return;
        }

        File[] files = this.folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                Arena arena = this.loadArena(file);
                this.arenas.put(arena.getId(), arena);
            } catch (IllegalArgumentException error) {
                log.error("Skipping broken arena file {}: {}", file.getName(), error.getMessage());
            }
        }
        log.info("Loaded {} zombie arena(s): {}", this.arenas.size(), this.arenas.keySet());
    }

    /**
     * Persists an arena to {@code <id>.yml} (overwrites existing).
     *
     * @param arena arena to save
     */
    public void save(Arena arena) {
        if (!this.folder.exists() && !this.folder.mkdirs()) {
            log.error("Could not create arena folder {}", this.folder.getAbsolutePath());
            return;
        }

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("id", arena.getId());
        yaml.set("displayName", arena.getDisplayName());
        yaml.set("world", arena.getWorldName());
        yaml.set("region.min", List.of(arena.getRegion().minX(), arena.getRegion().minY(), arena.getRegion().minZ()));
        yaml.set("region.max", List.of(arena.getRegion().maxX(), arena.getRegion().maxY(), arena.getRegion().maxZ()));
        yaml.set("lobbySpawn", LocationCodec.serialize(arena.getLobbySpawn()));
        yaml.set("playerSpawns", arena.getPlayerSpawns().stream().map(LocationCodec::serialize).toList());
        yaml.set("zombieSpawns", arena.getZombieSpawns().stream().map(LocationCodec::serialize).toList());
        for (WindowDef window : arena.getWindows().values()) {
            String path = "windows." + window.id();
            yaml.set(path + ".min", List.of(window.planks().minX(), window.planks().minY(), window.planks().minZ()));
            yaml.set(path + ".max", List.of(window.planks().maxX(), window.planks().maxY(), window.planks().maxZ()));
            yaml.set(path + ".spawn", LocationCodec.serialize(window.spawn()));
            yaml.set(path + ".material", window.material().name());
        }
        for (DoorDef door : arena.getDoors().values()) {
            String path = "doors." + door.id();
            yaml.set(path + ".min", List.of(door.region().minX(), door.region().minY(), door.region().minZ()));
            yaml.set(path + ".max", List.of(door.region().maxX(), door.region().maxY(), door.region().maxZ()));
            yaml.set(path + ".price", door.price());
        }
        for (WallBuyDef wallBuy : arena.getWallBuys().values()) {
            String path = "wallBuys." + wallBuy.id();
            yaml.set(path + ".gun", wallBuy.gunId());
            yaml.set(path + ".pos", List.of(wallBuy.pos().x(), wallBuy.pos().y(), wallBuy.pos().z()));
            yaml.set(path + ".facing", wallBuy.facing().name());
            yaml.set(path + ".price", wallBuy.price());
            yaml.set(path + ".ammoPrice", wallBuy.ammoPrice());
        }
        MysteryBoxDef box = arena.getMysteryBox();
        if (box != null) {
            yaml.set("mysteryBox.price", box.price());
            yaml.set("mysteryBox.moveChance", box.moveChance());
            yaml.set("mysteryBox.locations", box.locations().stream().map(LocationCodec::serialize).toList());
        }
        for (Map.Entry<String, BlockPos> drink : arena.getDrinks().entrySet()) {
            BlockPos pos = drink.getValue();
            yaml.set("drinks." + drink.getKey(), List.of(pos.x(), pos.y(), pos.z()));
        }
        BlockPos powerSwitch = arena.getPowerSwitch();
        if (powerSwitch != null) {
            yaml.set("powerSwitch", List.of(powerSwitch.x(), powerSwitch.y(), powerSwitch.z()));
        }
        yaml.set("settings.minPlayers", arena.getSettings().minPlayers());
        yaml.set("settings.maxPlayers", arena.getSettings().maxPlayers());
        yaml.set("settings.startingPoints", arena.getSettings().startingPoints());

        try {
            yaml.save(this.arenaFile(arena.getId()));
            this.arenas.put(arena.getId(), arena);
        } catch (IOException error) {
            log.error("Could not save arena {}: {}", arena.getId(), error.getMessage());
        }
    }

    /**
     * Finds an arena by id (case-insensitive).
     *
     * @param id arena id
     * @return the arena when present
     */
    public Optional<Arena> find(String id) {
        return Optional.ofNullable(this.arenas.get(id.toLowerCase()));
    }

    public Collection<Arena> arenas() {
        return Collections.unmodifiableCollection(this.arenas.values());
    }

    public File arenaFile(String id) {
        return new File(this.folder, id + ".yml");
    }

    public File schematicFile(String id) {
        return new File(this.folder, id + SCHEMATIC_EXTENSION);
    }

    private Arena loadArena(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        final String id = yaml.getString("id", file.getName().replace(".yml", "")).toLowerCase();
        String worldName = yaml.getString("world");
        List<Integer> min = yaml.getIntegerList("region.min");
        List<Integer> max = yaml.getIntegerList("region.max");
        if (worldName == null || min.size() != 3 || max.size() != 3) {
            throw new IllegalArgumentException("missing world or region");
        }
        Cuboid region = new Cuboid(worldName,
                min.get(0), min.get(1), min.get(2),
                max.get(0), max.get(1), max.get(2));

        Location lobbySpawn = this.readLocation(worldName, yaml.get("lobbySpawn"));
        List<Location> playerSpawns = this.readLocationList(worldName, yaml.getMapList("playerSpawns"));
        List<Location> zombieSpawns = this.readLocationList(worldName, yaml.getMapList("zombieSpawns"));
        if (lobbySpawn == null || playerSpawns.isEmpty() || zombieSpawns.isEmpty()) {
            throw new IllegalArgumentException("missing lobby spawn, player spawns or zombie spawns");
        }

        ConfigurationSection settingsSection = yaml.getConfigurationSection("settings");
        ArenaSettings settings = ArenaSettings.defaults();
        if (settingsSection != null) {
            settings = new ArenaSettings(
                    settingsSection.getInt("minPlayers", settings.minPlayers()),
                    settingsSection.getInt("maxPlayers", settings.maxPlayers()),
                    settingsSection.getInt("startingPoints", settings.startingPoints()));
        }

        Map<String, WindowDef> windows = this.readWindows(worldName, yaml.getConfigurationSection("windows"));
        Map<String, DoorDef> doors = this.readDoors(worldName, yaml.getConfigurationSection("doors"));
        Map<String, WallBuyDef> wallBuys = this.readWallBuys(worldName, yaml.getConfigurationSection("wallBuys"));
        MysteryBoxDef mysteryBox = this.readMysteryBox(worldName, yaml.getConfigurationSection("mysteryBox"));
        Map<String, BlockPos> drinks = this.readDrinks(yaml.getConfigurationSection("drinks"));
        BlockPos powerSwitch = this.readBlockPos(yaml.getIntegerList("powerSwitch"));

        String displayName = yaml.getString("displayName", id);
        return new Arena(id, displayName, worldName, region, lobbySpawn, playerSpawns, zombieSpawns,
                windows, doors, wallBuys, mysteryBox, drinks, powerSwitch, settings);
    }

    private Map<String, BlockPos> readDrinks(@Nullable ConfigurationSection section) {
        Map<String, BlockPos> drinks = new LinkedHashMap<>();
        if (section == null) {
            return drinks;
        }
        for (String key : section.getKeys(false)) {
            BlockPos pos = this.readBlockPos(section.getIntegerList(key));
            if (pos != null) {
                drinks.put(key, pos);
            }
        }
        return drinks;
    }

    @Nullable
    private BlockPos readBlockPos(List<Integer> raw) {
        return raw.size() == 3 ? new BlockPos(raw.get(0), raw.get(1), raw.get(2)) : null;
    }

    private Map<String, WallBuyDef> readWallBuys(String worldName, @Nullable ConfigurationSection section) {
        Map<String, WallBuyDef> wallBuys = new LinkedHashMap<>();
        if (section == null) {
            return wallBuys;
        }
        for (String key : section.getKeys(false)) {
            List<Integer> pos = section.getIntegerList(key + ".pos");
            String gunId = section.getString(key + ".gun");
            BlockFace facing = this.parseFace(section.getString(key + ".facing", "NORTH"));
            if (pos.size() != 3 || gunId == null || facing == null) {
                log.warn("Skipping invalid wall-buy '{}' in arena world {}", key, worldName);
                continue;
            }
            wallBuys.put(key, new WallBuyDef(key, gunId, new BlockPos(pos.get(0), pos.get(1), pos.get(2)),
                    facing, section.getInt(key + ".price", 600), section.getInt(key + ".ammoPrice", 300)));
        }
        return wallBuys;
    }

    @Nullable
    private MysteryBoxDef readMysteryBox(String worldName, @Nullable ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        List<Location> locations = this.readLocationList(worldName, section.getMapList("locations"));
        if (locations.isEmpty()) {
            return null;
        }
        return new MysteryBoxDef(section.getInt("price", 950), section.getDouble("moveChance", 0.10), locations);
    }

    @Nullable
    private BlockFace parseFace(String name) {
        try {
            return BlockFace.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

    private Map<String, WindowDef> readWindows(String worldName, @Nullable ConfigurationSection section) {
        Map<String, WindowDef> windows = new LinkedHashMap<>();
        if (section == null) {
            return windows;
        }
        for (String key : section.getKeys(false)) {
            List<Integer> min = section.getIntegerList(key + ".min");
            List<Integer> max = section.getIntegerList(key + ".max");
            Location spawn = this.readLocation(worldName, section.get(key + ".spawn"));
            Material material = Material.matchMaterial(section.getString(key + ".material", "OAK_PLANKS"));
            if (min.size() != 3 || max.size() != 3 || spawn == null || material == null) {
                log.warn("Skipping invalid window '{}' in arena world {}", key, worldName);
                continue;
            }
            Cuboid planks = new Cuboid(worldName,
                    min.get(0), min.get(1), min.get(2), max.get(0), max.get(1), max.get(2));
            windows.put(key, new WindowDef(key, planks, spawn, material));
        }
        return windows;
    }

    private Map<String, DoorDef> readDoors(String worldName, @Nullable ConfigurationSection section) {
        Map<String, DoorDef> doors = new LinkedHashMap<>();
        if (section == null) {
            return doors;
        }
        for (String key : section.getKeys(false)) {
            List<Integer> min = section.getIntegerList(key + ".min");
            List<Integer> max = section.getIntegerList(key + ".max");
            if (min.size() != 3 || max.size() != 3) {
                log.warn("Skipping invalid door '{}' in arena world {}", key, worldName);
                continue;
            }
            Cuboid region = new Cuboid(worldName,
                    min.get(0), min.get(1), min.get(2), max.get(0), max.get(1), max.get(2));
            doors.put(key, new DoorDef(key, region, section.getInt(key + ".price", 750)));
        }
        return doors;
    }

    @Nullable
    private Location readLocation(String worldName, @Nullable Object raw) {
        if (raw instanceof ConfigurationSection section) {
            return LocationCodec.deserialize(worldName, section.getValues(false));
        }
        if (raw instanceof Map<?, ?> map) {
            return LocationCodec.deserialize(worldName, map);
        }
        return null;
    }

    private List<Location> readLocationList(String worldName, List<Map<?, ?>> rawList) {
        List<Location> locations = new ArrayList<>();
        for (Map<?, ?> map : rawList) {
            Location location = LocationCodec.deserialize(worldName, map);
            if (location != null) {
                locations.add(location);
            }
        }
        return locations;
    }
}
