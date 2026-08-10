package dev.portero.atlas.zombies.combat.gun;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Loads gun definitions from {@code zombies/guns.yml} and provides weighted random
 * picks for the mystery box.
 */
@Slf4j
public class GunRegistry {

    private static final String FILE_NAME = "zombies" + File.separator + "guns.yml";

    private final Plugin plugin;
    private Map<String, GunDefinition> guns = Map.of();

    public GunRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * (Re)loads all gun definitions, writing the default file first when missing.
     */
    public void load() {
        File file = new File(this.plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            this.plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        Map<String, GunDefinition> loaded = new LinkedHashMap<>();
        for (String id : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            try {
                loaded.put(id, GunDefinition.fromYaml(id, section));
            } catch (IllegalArgumentException error) {
                log.error("Skipping invalid gun '{}': {}", id, error.getMessage());
            }
        }
        this.guns = Collections.unmodifiableMap(loaded);
        log.info("Loaded {} gun(s): {}", this.guns.size(), this.guns.keySet());
    }

    public Optional<GunDefinition> find(String id) {
        return Optional.ofNullable(this.guns.get(id));
    }

    /**
     * The gun given to every player when round 1 starts.
     *
     * @return the starting gun, or empty when none is flagged
     */
    public Optional<GunDefinition> starting() {
        return this.guns.values().stream().filter(GunDefinition::starting).findFirst();
    }

    /**
     * Weighted random pick from all guns with {@code boxWeight > 0}.
     *
     * @param random randomness source
     * @return the rolled gun, or empty when the box pool is empty
     */
    public Optional<GunDefinition> randomBoxGun(Random random) {
        int totalWeight = this.guns.values().stream().mapToInt(GunDefinition::boxWeight).sum();
        if (totalWeight <= 0) {
            return Optional.empty();
        }
        int roll = random.nextInt(totalWeight);
        for (GunDefinition gun : this.guns.values()) {
            roll -= gun.boxWeight();
            if (roll < 0) {
                return Optional.of(gun);
            }
        }
        return Optional.empty();
    }

    public Collection<GunDefinition> all() {
        return this.guns.values();
    }
}
