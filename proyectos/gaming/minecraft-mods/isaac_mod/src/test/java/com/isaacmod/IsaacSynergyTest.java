package com.isaacmod;

import com.isaacmod.dungeon.DungeonRoom;
import com.isaacmod.stats.PlayerStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IsaacSynergyTest {

    @Test
    @DisplayName("Verify Baseline Player Stats")
    void testBaselineStats() {
        PlayerStats stats = new PlayerStats.Builder().build();

        assertEquals(3.5f, stats.getDamage(), 0.001f);
        assertEquals(12, stats.getFireRate());
        assertEquals(1.2f, stats.getShotSpeed(), 0.001f);
        assertEquals(40, stats.getRange());
        assertEquals(1, stats.getTearCount());
        assertEquals(0.0f, stats.getSpreadAngle(), 0.001f);
        assertFalse(stats.isExplosive());
        assertFalse(stats.isLobbed());
    }

    @Test
    @DisplayName("Verify Cricket's Head Stat Scaling (+0.5 flat, 1.5x multiplier)")
    void testCricketsHeadScaling() {
        PlayerStats.Builder builder = new PlayerStats.Builder();
        // Cricket's Head effect
        builder.addDamage(0.5f);
        builder.multiplyDamage(1.5f);

        PlayerStats stats = builder.build();

        // (3.5 + 0.5) * 1.5 = 6.0
        assertEquals(6.0f, stats.getDamage(), 0.001f);
        assertEquals(12, stats.getFireRate());
        assertEquals(1, stats.getTearCount());
    }

    @Test
    @DisplayName("Verify The Inner Eye Triple Shot Mechanics")
    void testTheInnerEyeMechanics() {
        PlayerStats.Builder builder = new PlayerStats.Builder();
        // The Inner Eye effect
        builder.setTearCount(3);
        builder.setSpreadAngle(14.0f);
        builder.multiplyFireRate(1.75f);

        PlayerStats stats = builder.build();

        assertEquals(3, stats.getTearCount());
        assertEquals(14.0f, stats.getSpreadAngle(), 0.001f);
        assertEquals(Math.round(12 * 1.75f), stats.getFireRate()); // 21 ticks
        assertEquals(3.5f, stats.getDamage(), 0.001f);
    }

    @Test
    @DisplayName("Verify Ipecac Explosive Lobbed Shot Mechanics")
    void testIpecacMechanics() {
        PlayerStats.Builder builder = new PlayerStats.Builder();
        // Ipecac effect
        builder.setExplosive(true);
        builder.setLobbed(true);
        builder.addDamage(8.0f);
        builder.multiplyFireRate(2.0f);

        PlayerStats stats = builder.build();

        assertTrue(stats.isExplosive());
        assertTrue(stats.isLobbed());
        assertEquals(11.5f, stats.getDamage(), 0.001f); // 3.5 + 8.0 = 11.5
        assertEquals(24, stats.getFireRate()); // 12 * 2.0 = 24 ticks
    }

    @Test
    @DisplayName("Verify Full Artifact Synergy (Cricket's Head + Inner Eye + Ipecac)")
    void testCombinedSynergy() {
        PlayerStats.Builder builder = new PlayerStats.Builder();

        // Flat additions first
        builder.addDamage(0.5f); // Cricket's Head flat
        builder.addDamage(8.0f); // Ipecac flat

        // Multipliers
        builder.multiplyDamage(1.5f); // Cricket's Head 1.5x multiplier
        builder.multiplyFireRate(1.75f); // Inner Eye cooldown multiplier
        builder.multiplyFireRate(2.0f);  // Ipecac cooldown multiplier

        // Special attributes
        builder.setTearCount(3);
        builder.setSpreadAngle(14.0f);
        builder.setExplosive(true);
        builder.setLobbed(true);

        PlayerStats stats = builder.build();

        // Damage: (3.5 base + 0.5 + 8.0) * 1.5 = 12.0 * 1.5 = 18.0
        assertEquals(18.0f, stats.getDamage(), 0.001f);
        // Triple shot spread of explosive lobbed tears!
        assertEquals(3, stats.getTearCount());
        assertEquals(14.0f, stats.getSpreadAngle(), 0.001f);
        assertTrue(stats.isExplosive());
        assertTrue(stats.isLobbed());
        // Fire Rate: 12 * 1.75 * 2.0 = 42 ticks
        assertEquals(Math.round(12 * 1.75f * 2.0f), stats.getFireRate());
    }

    @Test
    @DisplayName("Verify Discrete 16x16 Dungeon Room Bounds & Grid Coordinates")
    void testDungeonRoomGridCoordinates() {
        int gridX = 2;
        int gridZ = 3;
        int minY = 64;
        int maxY = 70;

        DungeonRoom room = new DungeonRoom(gridX, gridZ, minY, maxY);

        assertEquals(2, room.getGridX());
        assertEquals(3, room.getGridZ());

        // Bounds: [32, 64, 48] to [47, 70, 63]
        assertEquals(32, room.getBounds().getMinX());
        assertEquals(47, room.getBounds().getMaxX());
        assertEquals(48, room.getBounds().getMinZ());
        assertEquals(63, room.getBounds().getMaxZ());

        // Center: 32 + 8 = 40, 48 + 8 = 56
        assertEquals(40, room.getCenterPos().getX());
        assertEquals(65, room.getCenterPos().getY());
        assertEquals(56, room.getCenterPos().getZ());

        // Inside checks
        assertTrue(room.contains(32.5, 65.0, 48.5));
        assertTrue(room.contains(40.0, 66.0, 56.0));
        assertTrue(room.contains(47.5, 65.0, 63.5));

        // Outside checks
        assertFalse(room.contains(31.9, 65.0, 48.5)); // West outside
        assertFalse(room.contains(48.1, 65.0, 56.0)); // East outside
        assertFalse(room.contains(40.0, 65.0, 64.1)); // South outside
    }

    @Test
    @DisplayName("Verify RoomType Behavior & Default States")
    void testRoomTypes() {
        DungeonRoom start = new DungeonRoom(0, 0, 64, 70, com.isaacmod.dungeon.RoomType.START);
        assertEquals(com.isaacmod.dungeon.RoomState.CLEARED, start.getState());
        assertEquals(com.isaacmod.dungeon.RoomType.START, start.getRoomType());

        DungeonRoom treasure = new DungeonRoom(1, 0, 64, 70, com.isaacmod.dungeon.RoomType.TREASURE);
        assertEquals(com.isaacmod.dungeon.RoomState.CLEARED, treasure.getState());
        assertEquals(com.isaacmod.dungeon.RoomType.TREASURE, treasure.getRoomType());

        DungeonRoom combat = new DungeonRoom(0, 1, 64, 70, com.isaacmod.dungeon.RoomType.COMBAT);
        assertEquals(com.isaacmod.dungeon.RoomState.UNVISITED, combat.getState());

        DungeonRoom boss = new DungeonRoom(0, 2, 64, 70, com.isaacmod.dungeon.RoomType.BOSS);
        assertEquals(com.isaacmod.dungeon.RoomState.UNVISITED, boss.getState());
    }

    @Test
    @DisplayName("Verify Sacred Heart & Godhead Divine Synergies")
    void testSacredHeartAndGodheadSynergy() {
        PlayerStats.Builder builder = new PlayerStats.Builder();
        // Sacred Heart (+3 flat, x2.3 mult, homing)
        builder.addDamage(3.0f);
        builder.multiplyDamage(2.3f);
        builder.setHoming(true);

        // Godhead (+0.5 flat, aura)
        builder.addDamage(0.5f);
        builder.setGodheadAura(true);

        PlayerStats stats = builder.build();

        // (3.5 + 3.0 + 0.5) * 2.3 = 7.0 * 2.3 = 16.1
        assertEquals(16.1f, stats.getDamage(), 0.01f);
        assertTrue(stats.isHoming());
        assertTrue(stats.hasGodheadAura());
    }

    @Test
    @DisplayName("Verify Soy Milk High Fire Rate & Damage Reduction")
    void testSoyMilkMechanics() {
        PlayerStats.Builder builder = new PlayerStats.Builder();
        builder.multiplyDamage(0.25f);
        builder.multiplyFireRate(0.20f);
        builder.addFireRate(-6.0f);

        PlayerStats stats = builder.build();

        assertEquals(3.5f * 0.25f, stats.getDamage(), 0.01f);
        assertEquals(2, stats.getFireRate()); // Capped at minimum 2 ticks
    }

    @Test
    @DisplayName("Verify The Wafer, Pyromaniac & Rubber Cement flags")
    void testDefensiveAndUtilityFlags() {
        PlayerStats stats = new PlayerStats.Builder()
                .setTheWafer(true)
                .setPyromaniac(true)
                .setRubberCement(true)
                .setTwentyTwenty(true)
                .setBrimstoneLaser(true)
                .build();

        assertTrue(stats.hasTheWafer());
        assertTrue(stats.hasPyromaniac());
        assertTrue(stats.hasRubberCement());
        assertTrue(stats.hasTwentyTwenty());
        assertTrue(stats.hasBrimstoneLaser());
    }
}
