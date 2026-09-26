package com.isaacmod.dungeon;

import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class DungeonFloor {
    private final int floorNumber;
    private final BlockPos origin;
    private final Map<Long, DungeonRoom> rooms = new LinkedHashMap<>();
    private DungeonRoom startRoom;
    private DungeonRoom bossRoom;
    private DungeonRoom treasureRoom;

    public DungeonFloor(int floorNumber, BlockPos origin) {
        this.floorNumber = floorNumber;
        this.origin = origin;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public Collection<DungeonRoom> getRooms() {
        return rooms.values();
    }

    public DungeonRoom getStartRoom() {
        return startRoom;
    }

    public DungeonRoom getBossRoom() {
        return bossRoom;
    }

    public DungeonRoom getTreasureRoom() {
        return treasureRoom;
    }

    public DungeonRoom getRoom(int gridX, int gridZ) {
        return rooms.get(ChunkPos.toLong(gridX, gridZ));
    }

    public DungeonRoom getRoomAt(double x, double y, double z) {
        for (DungeonRoom room : rooms.values()) {
            if (room.contains(x, y, z)) {
                return room;
            }
        }
        return null;
    }

    /**
     * Generates a procedurally interconnected floor with Start, Combat, Treasure, and Boss rooms.
     */
    public static DungeonFloor generate(ServerWorld world, BlockPos origin, int floorNumber, Random random) {
        DungeonFloor floor = new DungeonFloor(floorNumber, origin);

        int totalRooms = Math.min(8, 5 + floorNumber);
        int minY = origin.getY();
        int height = 5;
        int maxY = minY + height;

        // 1. Generate 2D Grid Room Layout
        Set<GridCoord> placedCoords = new LinkedHashSet<>();
        GridCoord startCoord = new GridCoord(0, 0);
        placedCoords.add(startCoord);

        List<GridCoord> candidates = new ArrayList<>();
        candidates.add(startCoord);

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        while (placedCoords.size() < totalRooms && !candidates.isEmpty()) {
            GridCoord current = candidates.get(random.nextInt(candidates.size()));
            List<GridCoord> validNeighbors = new ArrayList<>();

            for (int[] dir : directions) {
                GridCoord neighbor = new GridCoord(current.x + dir[0], current.z + dir[1]);
                if (!placedCoords.contains(neighbor)) {
                    validNeighbors.add(neighbor);
                }
            }

            if (validNeighbors.isEmpty()) {
                candidates.remove(current);
            } else {
                GridCoord next = validNeighbors.get(random.nextInt(validNeighbors.size()));
                placedCoords.add(next);
                candidates.add(next);
            }
        }

        // 2. Assign Room Types
        GridCoord bossCoord = null;
        int maxDist = -1;
        for (GridCoord coord : placedCoords) {
            int dist = Math.abs(coord.x) + Math.abs(coord.z);
            if (dist > maxDist) {
                maxDist = dist;
                bossCoord = coord;
            }
        }

        GridCoord treasureCoord = null;
        for (GridCoord coord : placedCoords) {
            if (!coord.equals(startCoord) && !coord.equals(bossCoord)) {
                int neighborCount = countNeighbors(coord, placedCoords, directions);
                if (neighborCount == 1) {
                    treasureCoord = coord;
                    break;
                }
            }
        }
        if (treasureCoord == null) {
            for (GridCoord coord : placedCoords) {
                if (!coord.equals(startCoord) && !coord.equals(bossCoord)) {
                    treasureCoord = coord;
                    break;
                }
            }
        }

        // 3. Instantiate DungeonRoom objects
        for (GridCoord coord : placedCoords) {
            RoomType type = RoomType.COMBAT;
            if (coord.equals(startCoord)) {
                type = RoomType.START;
            } else if (coord.equals(bossCoord)) {
                type = RoomType.BOSS;
            } else if (coord.equals(treasureCoord)) {
                type = RoomType.TREASURE;
            }

            DungeonRoom room = new DungeonRoom(coord.x, coord.z, minY, maxY, type);
            long key = ChunkPos.toLong(coord.x, coord.z);
            floor.rooms.put(key, room);

            if (type == RoomType.START) floor.startRoom = room;
            if (type == RoomType.BOSS) floor.bossRoom = room;
            if (type == RoomType.TREASURE) floor.treasureRoom = room;
        }

        // 4. Physical World Construction
        for (DungeonRoom room : floor.rooms.values()) {
            buildRoomShell(world, room);
        }

        // 5. Connect adjacent rooms with open, seamless doorways through BOTH shared walls
        for (GridCoord coord : placedCoords) {
            DungeonRoom currentRoom = floor.getRoom(coord.x, coord.z);

            // Connect North (z - 1)
            if (placedCoords.contains(new GridCoord(coord.x, coord.z - 1))) {
                connectRoomsNorthSouth(world, floor.getRoom(coord.x, coord.z - 1), currentRoom);
            }
            // Connect West (x - 1)
            if (placedCoords.contains(new GridCoord(coord.x - 1, coord.z))) {
                connectRoomsEastWest(world, floor.getRoom(coord.x - 1, coord.z), currentRoom);
            }
        }

        // 6. Special room setups
        if (floor.treasureRoom != null) {
            floor.treasureRoom.setupTreasureRoomPedestal(world);
        }

        return floor;
    }

    private static int countNeighbors(GridCoord coord, Set<GridCoord> all, int[][] dirs) {
        int count = 0;
        for (int[] d : dirs) {
            if (all.contains(new GridCoord(coord.x + d[0], coord.z + d[1]))) {
                count++;
            }
        }
        return count;
    }

    private static void buildRoomShell(ServerWorld world, DungeonRoom room) {
        BlockBox bounds = room.getBounds();
        int minX = bounds.getMinX();
        int maxX = bounds.getMaxX();
        int minY = bounds.getMinY();
        int maxY = bounds.getMaxY();
        int minZ = bounds.getMinZ();
        int maxZ = bounds.getMaxZ();

        int midX = minX + (DungeonRoom.ROOM_SIZE / 2);
        int midZ = minZ + (DungeonRoom.ROOM_SIZE / 2);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                // Floor
                world.setBlockState(new BlockPos(x, minY, z), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 3);
                // Ceiling
                world.setBlockState(new BlockPos(x, maxY, z), Blocks.DEEPSLATE_TILES.getDefaultState(), 3);
                // Roof spawn-proofing: Bottom slabs across entire roof prevents hostile mob spawns
                world.setBlockState(new BlockPos(x, maxY + 1, z), Blocks.DEEPSLATE_TILE_SLAB.getDefaultState(), 3);

                // Walls & Air Interior
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

        // Ceiling & Roof Illumination: Sea Lanterns embedded in ceiling + Lanterns on roof
        // 1. Center 2x2 cluster
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

        // 2. Four Corners
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

        // Lighting: corner wall torches
        world.setBlockState(new BlockPos(minX + 2, minY + 2, minZ + 2), Blocks.WALL_TORCH.getDefaultState(), 3);
        world.setBlockState(new BlockPos(maxX - 2, minY + 2, minZ + 2), Blocks.WALL_TORCH.getDefaultState(), 3);
        world.setBlockState(new BlockPos(minX + 2, minY + 2, maxZ - 2), Blocks.WALL_TORCH.getDefaultState(), 3);
        world.setBlockState(new BlockPos(maxX - 2, minY + 2, maxZ - 2), Blocks.WALL_TORCH.getDefaultState(), 3);
    }

    private static void connectRoomsNorthSouth(ServerWorld world, DungeonRoom northRoom, DungeonRoom southRoom) {
        int minX = southRoom.getBounds().getMinX();
        int midX = minX + (DungeonRoom.ROOM_SIZE / 2);
        int wallZ_south = southRoom.getBounds().getMinZ();
        int wallZ_north = northRoom.getBounds().getMaxZ();
        int minY = southRoom.getBounds().getMinY();

        // Carve 2-block wide, 3-block high opening completely through BOTH adjacent walls
        for (int dx = -1; dx <= 0; dx++) {
            // Floor under doorway
            world.setBlockState(new BlockPos(midX + dx, minY, wallZ_south), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 3);
            world.setBlockState(new BlockPos(midX + dx, minY, wallZ_north), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 3);

            for (int dy = 1; dy <= 3; dy++) {
                BlockPos pSouth = new BlockPos(midX + dx, minY + dy, wallZ_south);
                BlockPos pNorth = new BlockPos(midX + dx, minY + dy, wallZ_north);

                world.setBlockState(pSouth, Blocks.AIR.getDefaultState(), 3);
                world.setBlockState(pNorth, Blocks.AIR.getDefaultState(), 3);

                // Both rooms manage both doorway boundary blocks so they are always in sync
                southRoom.addDoorPosition(pSouth);
                southRoom.addDoorPosition(pNorth);
                northRoom.addDoorPosition(pSouth);
                northRoom.addDoorPosition(pNorth);
            }
        }
    }

    private static void connectRoomsEastWest(ServerWorld world, DungeonRoom westRoom, DungeonRoom eastRoom) {
        int minZ = eastRoom.getBounds().getMinZ();
        int midZ = minZ + (DungeonRoom.ROOM_SIZE / 2);
        int wallX_east = eastRoom.getBounds().getMinX();
        int wallX_west = westRoom.getBounds().getMaxX();
        int minY = eastRoom.getBounds().getMinY();

        // Carve 2-block wide, 3-block high opening completely through BOTH adjacent walls
        for (int dz = -1; dz <= 0; dz++) {
            // Floor under doorway
            world.setBlockState(new BlockPos(wallX_east, minY, midZ + dz), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 3);
            world.setBlockState(new BlockPos(wallX_west, minY, midZ + dz), Blocks.POLISHED_DEEPSLATE.getDefaultState(), 3);

            for (int dy = 1; dy <= 3; dy++) {
                BlockPos pEast = new BlockPos(wallX_east, minY + dy, midZ + dz);
                BlockPos pWest = new BlockPos(wallX_west, minY + dy, midZ + dz);

                world.setBlockState(pEast, Blocks.AIR.getDefaultState(), 3);
                world.setBlockState(pWest, Blocks.AIR.getDefaultState(), 3);

                eastRoom.addDoorPosition(pEast);
                eastRoom.addDoorPosition(pWest);
                westRoom.addDoorPosition(pEast);
                westRoom.addDoorPosition(pWest);
            }
        }
    }

    public void clearFloorBlocks(ServerWorld world) {
        for (DungeonRoom room : rooms.values()) {
            BlockBox b = room.getBounds();
            for (int x = b.getMinX(); x <= b.getMaxX(); x++) {
                for (int y = b.getMinY(); y <= b.getMaxY() + 2; y++) {
                    for (int z = b.getMinZ(); z <= b.getMaxZ(); z++) {
                        world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    public record GridCoord(int x, int z) {}
}
