package dev.portero.atlas.zombies.combat.gun;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GunDefinitionTest {

    @Test
    void parsesFullDefinition() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("damage", 14.0);
        yaml.set("material", "CROSSBOW");
        yaml.set("displayName", "&eM1 Garand");
        yaml.set("magazine", 8);
        yaml.set("reserve", 96);
        yaml.set("boxWeight", 10);
        yaml.set("fireSound", "entity.generic.explode");

        GunDefinition gun = GunDefinition.fromYaml("m1_garand", yaml);
        assertEquals("m1_garand", gun.id());
        assertEquals(Material.CROSSBOW, gun.material());
        assertEquals(14.0, gun.damage(), 0.0001);
        assertEquals(8, gun.magazine());
        assertEquals(96, gun.reserve());
        assertEquals(10, gun.boxWeight());
        assertEquals(1, gun.pellets());
        assertTrue(gun.boxWeight() > 0);
    }

    @Test
    void appliesDefaults() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("material", "IRON_HOE");

        GunDefinition gun = GunDefinition.fromYaml("pistol", yaml);
        assertEquals(6.0, gun.damage(), 0.0001);
        assertEquals(8, gun.magazine());
        assertEquals("entity.generic.explode", gun.fireSound());
        assertEquals(false, gun.projectile());
    }

    @Test
    void rejectsUnknownMaterial() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("material", "NOT_A_MATERIAL");
        assertThrows(IllegalArgumentException.class, () -> GunDefinition.fromYaml("broken", yaml));
    }
}
