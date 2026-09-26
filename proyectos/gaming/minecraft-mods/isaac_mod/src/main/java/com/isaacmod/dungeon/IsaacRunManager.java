package com.isaacmod.dungeon;

import com.isaacmod.item.IsaacItems;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameRules;

import java.util.List;
import java.util.Random;

public class IsaacRunManager {
    public static final BlockPos DUNGEON_ORIGIN = new BlockPos(0, 120, 0);

    private static DungeonFloor activeFloor = null;
    private static int currentFloorNumber = 1;
    private static boolean runActive = false;
    private static final RunItemPool runItemPool = new RunItemPool();

    public static boolean isRunActive() {
        return runActive;
    }

    public static int getCurrentFloorNumber() {
        return currentFloorNumber;
    }

    public static DungeonFloor getActiveFloor() {
        return activeFloor;
    }

    public static RunItemPool getRunItemPool() {
        return runItemPool;
    }

    /**
     * Starts a brand new roguelike run: cleans previous dungeon, disables mob griefing (so creepers don't break blocks),
     * generates a fresh multi-room floor, resets player equipment to starter tears, and teleports them to the Start Room.
     */
    public static void startNewRun(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        runActive = true;
        currentFloorNumber = 1;
        runItemPool.reset();

        // Ensure Creepers, Ghasts, and mobs CANNOT break blocks in the dungeon
        world.getGameRules().get(GameRules.DO_MOB_GRIEFING).set(false, world.getServer());

        // 1. Clear previous floor blocks and leftover mobs
        cleanupDungeon(world);

        // 2. Generate brand new procedural interconnected floor
        activeFloor = DungeonFloor.generate(world, DUNGEON_ORIGIN, currentFloorNumber, new Random());

        // 3. Register rooms into room manager
        for (DungeonRoom room : activeFloor.getRooms()) {
            DungeonRoomManager.registerRoom(room);
        }

        // 4. Reset player inventory & give starter tear weapon and Isaac pickups
        player.getInventory().clear();
        player.giveItemStack(new ItemStack(IsaacItems.ISAAC_TEAR));
        player.giveItemStack(new ItemStack(IsaacItems.RED_HEART, 3));
        player.giveItemStack(new ItemStack(IsaacItems.SOUL_HEART, 1));
        player.clearStatusEffects();
        player.setHealth(player.getMaxHealth());
        com.isaacmod.stats.HolyMantleHandler.recharge(player);

        // 5. Teleport player to the center of the safe Start Room
        BlockPos spawnPos = activeFloor.getStartRoom().getCenterPos();
        player.teleport(world, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);

        // 6. Sound & Titles
        world.playSound(null, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 0.7f);

        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("THE BASEMENT").formatted(Formatting.GOLD, Formatting.BOLD)));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Floor I - A New Run Begins!").formatted(Formatting.YELLOW)));
        player.sendMessage(Text.literal("§6========================================"), false);
        player.sendMessage(Text.literal("§e§lTHE BINDING OF ISAAC - ROGUELIKE RUN"), false);
        player.sendMessage(Text.literal("§7Explore interconnected rooms, defeat enemies to unlock doors, and find Monstro!"), false);
        player.sendMessage(Text.literal("§aCreepers are safe: they explode on contact without breaking dungeon blocks."), false);
        player.sendMessage(Text.literal("§6========================================"), false);
    }

    /**
     * Advances to the next floor through the Boss trapdoor.
     */
    public static void nextFloor(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        currentFloorNumber++;

        cleanupDungeon(world);

        // Generate next floor with scaled layouts
        activeFloor = DungeonFloor.generate(world, DUNGEON_ORIGIN, currentFloorNumber, new Random());
        for (DungeonRoom room : activeFloor.getRooms()) {
            DungeonRoomManager.registerRoom(room);
        }

        BlockPos spawnPos = activeFloor.getStartRoom().getCenterPos();
        player.teleport(world, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);

        world.playSound(null, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.8f, 1.2f);

        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("THE BASEMENT - FLOOR " + currentFloorNumber).formatted(Formatting.GOLD, Formatting.BOLD)));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Deeper into the basement...").formatted(Formatting.YELLOW)));
        player.sendMessage(Text.literal("§a[Isaac] Advanced to Floor " + currentFloorNumber + "!"), false);
    }

    /**
     * Clears previous floor blocks and leftover mobs.
     */
    private static void cleanupDungeon(ServerWorld world) {
        if (activeFloor != null) {
            activeFloor.clearFloorBlocks(world);
        }
        DungeonRoomManager.clearRooms();

        // Discard any leftover hostile mobs in the dungeon zone
        Box dungeonArea = new Box(-200, 110, -200, 200, 145, 200);
        List<MobEntity> mobs = world.getEntitiesByClass(MobEntity.class, dungeonArea, m -> true);
        for (MobEntity m : mobs) {
            m.discard();
        }
    }

    /**
     * Checks if player stepped onto the boss trapdoor to descend.
     */
    public static void checkTrapdoorDescent(ServerWorld world, ServerPlayerEntity player) {
        if (!runActive || activeFloor == null) return;

        DungeonRoom bossRoom = activeFloor.getBossRoom();
        if (bossRoom != null && bossRoom.isTrapdoorSpawned()) {
            BlockPos center = bossRoom.getCenterPos();
            double dx = player.getX() - (center.getX() + 0.5);
            double dy = Math.abs(player.getY() - center.getY());
            double dz = player.getZ() - (center.getZ() + 0.5);

            if (dx * dx + dz * dz < 1.0 && dy < 1.5) {
                nextFloor(player);
            }
        }
    }

    public static void endRun() {
        runActive = false;
        activeFloor = null;
        DungeonRoomManager.clearRooms();
    }
}
