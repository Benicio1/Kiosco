package com.isaacmod.dungeon;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonRoomManager {
    private static final Map<Long, DungeonRoom> rooms = new ConcurrentHashMap<>();

    public static void initialize() {
        ServerTickEvents.END_WORLD_TICK.register(DungeonRoomManager::onWorldTick);
    }

    public static void registerRoom(DungeonRoom room) {
        long key = ChunkPos.toLong(room.getGridX(), room.getGridZ());
        rooms.put(key, room);
    }

    public static DungeonRoom getRoom(int gridX, int gridZ) {
        return rooms.get(ChunkPos.toLong(gridX, gridZ));
    }

    public static Collection<DungeonRoom> getAllRooms() {
        return rooms.values();
    }

    public static void clearRooms() {
        rooms.clear();
    }

    private static void onWorldTick(ServerWorld world) {
        // CRITICAL FIX: Only execute dungeon tick logic on OVERWORLD!
        // Prevents Nether and End from desyncing entity states or modifying blocks in wrong dimensions!
        if (world.getRegistryKey() != World.OVERWORLD) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            IsaacRunManager.checkTrapdoorDescent(world, player);
        }

        for (DungeonRoom room : rooms.values()) {
            if (room.getState() == RoomState.UNVISITED) {
                // Only trigger when the player is fully inside the interior, past the doorway threshold
                for (ServerPlayerEntity player : world.getPlayers()) {
                    if (room.isInsideTriggerZone(player.getX(), player.getY(), player.getZ())) {
                        room.onPlayerEnter(world, player);
                        break;
                    }
                }
            } else if (room.getState() == RoomState.ACTIVE_COMBAT) {
                room.tick(world);
            }
        }
    }

    /**
     * Generates a single test room centered at or aligned to the grid.
     */
    public static DungeonRoom generateTestRoom(ServerWorld world, BlockPos origin) {
        int gridX = Math.floorDiv(origin.getX(), DungeonRoom.ROOM_SIZE);
        int gridZ = Math.floorDiv(origin.getZ(), DungeonRoom.ROOM_SIZE);
        int minY = origin.getY();
        int height = 5;
        int maxY = minY + height;

        int minX = gridX * DungeonRoom.ROOM_SIZE;
        int minZ = gridZ * DungeonRoom.ROOM_SIZE;
        int maxX = minX + DungeonRoom.ROOM_SIZE - 1;
        int maxZ = minZ + DungeonRoom.ROOM_SIZE - 1;

        DungeonRoom room = new DungeonRoom(gridX, gridZ, minY, maxY, RoomType.COMBAT);

        // 1. Floor & Ceiling & Walls
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.setBlockState(new BlockPos(x, minY, z), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 3);
                world.setBlockState(new BlockPos(x, maxY, z), Blocks.DEEPSLATE_TILES.getDefaultState(), 3);
                // Roof spawn-proofing: Bottom slabs
                world.setBlockState(new BlockPos(x, maxY + 1, z), Blocks.DEEPSLATE_TILE_SLAB.getDefaultState(), 3);

                for (int y = minY + 1; y < maxY; y++) {
                    boolean isWall = (x == minX || x == maxX || z == minZ || z == maxZ);
                    BlockPos p = new BlockPos(x, y, z);
                    if (isWall) {
                        world.setBlockState(p, Blocks.DEEPSLATE_BRICKS.getDefaultState(), 3);
                    } else {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
        }

        // 2. Ceiling & Roof Illumination
        int midX = minX + (DungeonRoom.ROOM_SIZE / 2);
        int midZ = minZ + (DungeonRoom.ROOM_SIZE / 2);

        BlockPos[] centerCeiling = {
                new BlockPos(midX, maxY, midZ),
                new BlockPos(midX - 1, maxY, midZ),
                new BlockPos(midX, maxY, midZ - 1),
                new BlockPos(midX - 1, maxY, midZ - 1)
        };
        for (BlockPos cp : centerCeiling) {
            world.setBlockState(cp, Blocks.SEA_LANTERN.getDefaultState(), 3);
            world.setBlockState(cp.up(), Blocks.LANTERN.getDefaultState(), 3);
        }

        BlockPos[] corners = {
                new BlockPos(minX + 2, maxY, minZ + 2),
                new BlockPos(maxX - 2, maxY, minZ + 2),
                new BlockPos(minX + 2, maxY, maxZ - 2),
                new BlockPos(maxX - 2, maxY, maxZ - 2)
        };
        for (BlockPos cp : corners) {
            world.setBlockState(cp, Blocks.SEA_LANTERN.getDefaultState(), 3);
            world.setBlockState(cp.up(), Blocks.LANTERN.getDefaultState(), 3);
        }

        // 3. Doorways (North, South, East, West)

        addDoorway(world, room, new BlockPos(midX - 1, minY + 1, minZ));
        addDoorway(world, room, new BlockPos(midX, minY + 1, minZ));

        addDoorway(world, room, new BlockPos(midX - 1, minY + 1, maxZ));
        addDoorway(world, room, new BlockPos(midX, minY + 1, maxZ));

        addDoorway(world, room, new BlockPos(minX, minY + 1, midZ - 1));
        addDoorway(world, room, new BlockPos(minX, minY + 1, midZ));

        addDoorway(world, room, new BlockPos(maxX, minY + 1, midZ - 1));
        addDoorway(world, room, new BlockPos(maxX, minY + 1, midZ));

        // 3. Interior lighting
        world.setBlockState(new BlockPos(minX + 2, minY + 2, minZ + 2), Blocks.WALL_TORCH.getDefaultState(), 3);
        world.setBlockState(new BlockPos(maxX - 2, minY + 2, minZ + 2), Blocks.WALL_TORCH.getDefaultState(), 3);
        world.setBlockState(new BlockPos(minX + 2, minY + 2, maxZ - 2), Blocks.WALL_TORCH.getDefaultState(), 3);
        world.setBlockState(new BlockPos(maxX - 2, minY + 2, maxZ - 2), Blocks.WALL_TORCH.getDefaultState(), 3);

        registerRoom(room);
        return room;
    }

    private static void addDoorway(ServerWorld world, DungeonRoom room, BlockPos bottomPos) {
        for (int dy = 0; dy < 3; dy++) {
            BlockPos p = bottomPos.up(dy);
            world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
            room.addDoorPosition(p);
        }
    }
}
