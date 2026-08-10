package dev.portero.atlas.zombies.perk;

import lombok.Getter;
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

/**
 * Loads perk definitions and the global perk slot limit from {@code zombies/perks.yml}.
 */
@Slf4j
public class PerkRegistry {

    private static final String FILE_NAME = "zombies" + File.separator + "perks.yml";

    private final Plugin plugin;
    private Map<String, PerkDefinition> perks = Map.of();
    @Getter
    private int maxPerks = 4;

    public PerkRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * (Re)loads all perk definitions, writing the default file first when missing.
     */
    public void load() {
        File file = new File(this.plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            this.plugin.saveResource(FILE_NAME, false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        this.maxPerks = yaml.getInt("maxPerks", 4);

        Map<String, PerkDefinition> loaded = new LinkedHashMap<>();
        ConfigurationSection section = yaml.getConfigurationSection("perks");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection perkSection = section.getConfigurationSection(id);
                if (perkSection != null) {
                    loaded.put(id, PerkDefinition.fromYaml(id, perkSection));
                }
            }
        }
        this.perks = Collections.unmodifiableMap(loaded);
        log.info("Loaded {} perk(s): {}", this.perks.size(), this.perks.keySet());
    }

    public Optional<PerkDefinition> find(String id) {
        return Optional.ofNullable(this.perks.get(id));
    }

    public Collection<PerkDefinition> all() {
        return this.perks.values();
    }
}
