package dev.portero.atlas.zombies.perk;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerkDefinitionTest {

    @Test
    void parsesDefinition() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("displayName", "&4Juggernog");
        yaml.set("price", 2500);

        PerkDefinition perk = PerkDefinition.fromYaml("juggernog", yaml);
        assertEquals("juggernog", perk.id());
        assertEquals("&4Juggernog", perk.displayName());
        assertEquals(2500, perk.price());
        assertEquals(2500, perk.soloPrice());
    }

    @Test
    void soloPriceOverridesPriceWhenAlone() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("price", 1500);
        yaml.set("soloPrice", 500);

        PerkDefinition perk = PerkDefinition.fromYaml("quick_revive", yaml);
        assertEquals(500, perk.priceFor(1));
        assertEquals(1500, perk.priceFor(2));
        assertEquals(1500, perk.priceFor(4));
    }
}
