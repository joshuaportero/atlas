package dev.portero.atlas.zombies.game;

import dev.portero.atlas.zombies.ZombiesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoundDirectorTest {

    private RoundDirector director;

    @BeforeEach
    void setUp() {
        ZombiesConfig config = mock(ZombiesConfig.class);
        when(config.getBaseZombies()).thenReturn(6);
        when(config.getZombiesPerRound()).thenReturn(2);
        when(config.getZombiesPerExtraPlayer()).thenReturn(3);
        when(config.getSpawnIntervalTicks()).thenReturn(20);
        when(config.getHealthMultiplierPerRound()).thenReturn(0.15);
        when(config.getBaseSpeed()).thenReturn(0.23);
        when(config.getSprintSpeed()).thenReturn(0.30);
        when(config.getSprinterFromRound()).thenReturn(5);
        when(config.getSprinterChanceStep()).thenReturn(0.05);
        when(config.getSprinterChanceMax()).thenReturn(0.6);
        this.director = new RoundDirector(config);
    }

    @Test
    void zombieCountScalesWithRoundAndPlayers() {
        assertEquals(6, this.director.zombiesForRound(1, 1));
        assertEquals(8, this.director.zombiesForRound(2, 1));
        assertEquals(24, this.director.zombiesForRound(10, 1));
        assertEquals(15, this.director.zombiesForRound(1, 4));
        assertEquals(27, this.director.zombiesForRound(7, 4));
    }

    @Test
    void healthScalesLinearlyPerRound() {
        assertEquals(20.0, this.director.healthForRound(1), 0.0001);
        assertEquals(23.0, this.director.healthForRound(2), 0.0001);
        assertEquals(32.0, this.director.healthForRound(5), 0.0001);
    }

    @Test
    void spawnIntervalShrinksButNeverBelowFloor() {
        assertEquals(20, this.director.spawnIntervalTicks(1));
        assertEquals(16, this.director.spawnIntervalTicks(5));
        assertEquals(5, this.director.spawnIntervalTicks(16));
        assertEquals(5, this.director.spawnIntervalTicks(100));
    }

    @Test
    void noSprintersBeforeConfiguredRound() {
        for (int round = 1; round < 5; round++) {
            assertEquals(0.23, this.director.speedForRound(round), 0.0001);
        }
    }

    @Test
    void speedNeverLeavesConfiguredBand() {
        for (int round = 5; round <= 60; round++) {
            double speed = this.director.speedForRound(round);
            assertEquals(true, Math.abs(speed - 0.23) < 0.0001 || Math.abs(speed - 0.30) < 0.0001);
        }
    }
}
