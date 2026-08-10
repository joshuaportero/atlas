package dev.portero.atlas.zombies.arena;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuboidTest {

    @Test
    void constructorNormalizesBounds() {
        Cuboid cuboid = new Cuboid("world", 10, 70, 30, 0, 60, 20);
        assertEquals(0, cuboid.minX());
        assertEquals(10, cuboid.maxX());
        assertEquals(60, cuboid.minY());
        assertEquals(70, cuboid.maxY());
        assertEquals(20, cuboid.minZ());
        assertEquals(30, cuboid.maxZ());
    }

    @Test
    void chunkColumnsCoverWholeRegion() {
        Cuboid cuboid = new Cuboid("world", 0, 0, 0, 33, 10, 16);
        List<int[]> columns = cuboid.chunkColumns();
        assertEquals(6, columns.size());
        assertTrue(columns.stream().anyMatch(column -> column[0] == 0 && column[1] == 0));
        assertTrue(columns.stream().anyMatch(column -> column[0] == 2 && column[1] == 1));
    }

    @Test
    void chunkColumnsForSingleChunkRegion() {
        Cuboid cuboid = new Cuboid("world", 5, 0, 5, 10, 10, 10);
        assertEquals(1, cuboid.chunkColumns().size());
    }
}
