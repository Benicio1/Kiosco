package com.isaacmod.dungeon;

import com.isaacmod.IsaacMod;
import com.isaacmod.block.IsaacBlocks;
import com.isaacmod.item.IsaacItems;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.*;

public class DungeonRoom {
    public static final int ROOM_SIZE = 16;

    private final int gridX;
    private final int gridZ;
    private final int minY;
    private final int maxY;
    private final BlockBox bounds;
    private final Box boundingBox;
    private final RoomType roomType;

    private RoomState state;
    private final List<BlockPos> doorPositions = new ArrayList<>();
    private final Set<UUID> activeMobUuids = new HashSet<>();
    private boolean trapdoorSpawned = false;

    public DungeonRoom(int gridX, int gridZ, int minY, int maxY, RoomType roomType) {
        this.gridX = gridX;
        this.gridZ = gridZ;
        this.minY = minY;
        this.maxY = maxY;
        this.roomType = roomType;

        int minX = gridX * ROOM_SIZE;
        int minZ = gridZ * ROOM_SIZE;
        int maxX = minX + ROOM_SIZE - 1;
        int maxZ = minZ + ROOM_SIZE - 1;

        this.bounds = new BlockBox(minX, minY, minZ, maxX, maxY, maxZ);
        this.boundingBox = new Box(minX, minY, minZ, maxX + 1, maxY, maxZ + 1);

        if (roomType == RoomType.START || roomType == RoomType.TREASURE) {
            this.state = RoomState.CLEARED;
        } else {
            this.state = RoomState.UNVISITED;
        }
    }

    public DungeonRoom(int gridX, int gridZ, int minY, int maxY) {
        this(gridX, gridZ, minY, maxY, RoomType.COMBAT);
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridZ() {
        return gridZ;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public RoomState getState() {
        return state;
    }

    public void setState(RoomState state) {
        this.state = state;
    }

    public BlockBox getBounds() {
        return bounds;
    }

    public Box getBoundingBox() {
        return boundingBox;
    }

    public boolean isTrapdoorSpawned() {
        return trapdoorSpawned;
    }

    public void addDoorPosition(BlockPos pos) {
        if (!doorPositions.contains(pos)) {
            doorPositions.add(pos);
        }
    }

    public List<BlockPos> getDoorPositions() {
        return Collections.unmodifiableList(doorPositions);
    }

    public boolean contains(BlockPos pos) {
        return bounds.contains(pos);
    }

    public boolean contains(double x, double y, double z) {
        return boundingBox.contains(x, y, z);
    }

    public boolean isInsideTriggerZone(double x, double y, double z) {
        int minX = bounds.getMinX();
        int maxX = bounds.getMaxX();
        int minZ = bounds.getMinZ();
        int maxZ = bounds.getMaxZ();
        int bMinY = bounds.getMinY();
        int bMaxY = bounds.getMaxY();

        return x >= (minX + 2.0) && x <= (maxX - 1.0) &&
               z >= (minZ + 2.0) && z <= (maxZ - 1.0) &&
               y >= bMinY && y < bMaxY;
    }

    public BlockPos getCenterPos() {
        int centerX = gridX * ROOM_SIZE + (ROOM_SIZE / 2);
        int centerZ = gridZ * ROOM_SIZE + (ROOM_SIZE / 2);
        return new BlockPos(centerX, minY + 1, centerZ);
    }

    public static net.minecraft.item.Item[] getArtifactPool() {
        return new net.minecraft.item.Item[] {
                IsaacItems.CRICKETS_HEAD,
                IsaacItems.THE_INNER_EYE,
                IsaacItems.IPECAC,
                IsaacItems.BRIMSTONE,
                IsaacItems.SPOON_BENDER,
                IsaacItems.THE_SAD_ONION,
                IsaacItems.MUTANT_SPIDER,
                IsaacItems.POLYPHEMUS,
                IsaacItems.HOLY_MANTLE,
                IsaacItems.SACRED_HEART,
                IsaacItems.GODHEAD,
                IsaacItems.MAGIC_MUSHROOM,
                IsaacItems.TWENTY_TWENTY,
                IsaacItems.SOY_MILK,
                IsaacItems.RUBBER_CEMENT,
                IsaacItems.THE_WAFER,
                IsaacItems.PYROMANIAC,
                IsaacItems.GNAWED_LEAF,
                IsaacItems.CAT_O_NINE_TAILS,
                IsaacItems.THE_HALO,
                IsaacItems.ABADDON,
                IsaacItems.THE_D6,
                IsaacItems.YUM_HEART,
                IsaacItems.BOOK_OF_BELIAL,
                IsaacItems.THE_NECRONOMICON,
                IsaacItems.TAMMYS_HEAD
        };
    }

    public void onPlayerEnter(ServerWorld world, ServerPlayerEntity player) {
        com.isaacmod.stats.HolyMantleHandler.recharge(player);

        if (this.roomType == RoomType.START) {
            player.sendMessage(Text.literal("§6[Isaac] Safe Starting Room. Step through the doors to explore the dungeon!"), false);
            return;
        }

        if (this.roomType == RoomType.TREASURE) {
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("TREASURE ROOM").formatted(Formatting.GOLD, Formatting.BOLD)));
            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Claim your artifact!").formatted(Formatting.YELLOW)));
            return;
        }

        if (this.state != RoomState.UNVISITED) {
            return;
        }

        startCombat(world, player);
    }

    public void startCombat(ServerWorld world, ServerPlayerEntity player) {
        this.state = RoomState.ACTIVE_COMBAT;
        IsaacMod.LOGGER.info("Room at ({}, {}) started combat! Locking doors.", gridX, gridZ);

        // 1. Lock Exits: Replace all door positions with unbreakable door barriers
        lockExits(world);

        // 2. Play warning audio and notifications
        BlockPos center = getCenterPos();
        world.playSound(null, center.getX(), center.getY(), center.getZ(),
                SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, 0.5f);
        world.playSound(null, center.getX(), center.getY(), center.getZ(),
                SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0f, 0.8f);

        if (roomType == RoomType.BOSS) {
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("BOSS ROOM!").formatted(Formatting.DARK_PURPLE, Formatting.BOLD)));
            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Defeat Monstro to unlock the trapdoor!").formatted(Formatting.RED)));
            spawnBossWave(world);
        } else {
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("ROOM LOCKED!").formatted(Formatting.DARK_RED, Formatting.BOLD)));
            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Defeat all hostile mobs to open the doors!").formatted(Formatting.RED)));
            spawnHostileWave(world);
        }

        player.sendMessage(Text.translatable("dungeon.isaac.room_locked").formatted(Formatting.RED), false);
    }

    private void lockExits(ServerWorld world) {
        for (BlockPos doorPos : doorPositions) {
            world.setBlockState(doorPos, IsaacBlocks.DOOR_BARRIER.getDefaultState(), 3);
        }
    }

    public void unlockExits(ServerWorld world) {
        IsaacMod.LOGGER.info("Room at ({}, {}) unlocking! Restoring exits to AIR.", gridX, gridZ);

        // 1. Automatically restore all registered doorway blocks to AIR
        for (BlockPos doorPos : doorPositions) {
            world.setBlockState(doorPos, Blocks.AIR.getDefaultState(), 3);
        }

        // 2. Comprehensive cleanup: scan the entire room perimeter walls for any leftover DOOR_BARRIER blocks and clear them to AIR
        BlockBox b = getBounds();
        for (int x = b.getMinX(); x <= b.getMaxX(); x++) {
            for (int z = b.getMinZ(); z <= b.getMaxZ(); z++) {
                boolean isPerimeter = (x == b.getMinX() || x == b.getMaxX() || z == b.getMinZ() || z == b.getMaxZ());
                if (isPerimeter) {
                    for (int y = b.getMinY() + 1; y < b.getMaxY(); y++) {
                        BlockPos p = new BlockPos(x, y, z);
                        if (world.getBlockState(p).isOf(IsaacBlocks.DOOR_BARRIER)) {
                            world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                        }
                    }
                }
            }
        }

        BlockPos center = getCenterPos();
        world.playSound(null, center.getX(), center.getY(), center.getZ(),
                SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.2f, 1.2f);
    }

    private void spawnHostileWave(ServerWorld world) {
        activeMobUuids.clear();
        BlockPos center = getCenterPos();
        int encounterType = world.getRandom().nextInt(5);

        switch (encounterType) {
            case 0 -> {
                // Small bouncy Slimes
                int count = 3;
                for (int i = 0; i < count; i++) {
                    SlimeEntity slime = EntityType.SLIME.create(world);
                    if (slime != null) {
                        slime.setSize(1, true);
                        spawnMobAt(world, slime, center.add((i - 1) * 3, 0, (i % 2 == 0 ? -3 : 3)));
                    }
                }
            }
            case 1 -> {
                // 2 Skeleton Archers
                for (int i = 0; i < 2; i++) {
                    SkeletonEntity skeleton = EntityType.SKELETON.create(world);
                    if (skeleton != null) {
                        spawnMobAt(world, skeleton, center.add(i == 0 ? -3 : 3, 0, -3));
                    }
                }
            }
            case 2 -> {
                // 1 Creeper & 1 Zombie
                CreeperEntity creeper = EntityType.CREEPER.create(world);
                if (creeper != null) {
                    spawnMobAt(world, creeper, center.add(0, 0, -3));
                }
                ZombieEntity zombie = EntityType.ZOMBIE.create(world);
                if (zombie != null) {
                    spawnMobAt(world, zombie, center.add(3, 0, 3));
                }
            }
            case 3 -> {
                // 2 Normal Spiders
                for (int i = 0; i < 2; i++) {
                    SpiderEntity spider = EntityType.SPIDER.create(world);
                    if (spider != null) {
                        spawnMobAt(world, spider, center.add(i == 0 ? -3 : 3, 0, 3));
                    }
                }
            }
            default -> {
                // Mixed Skirmish: 1 Zombie + 1 Cave Spider
                ZombieEntity zombie = EntityType.ZOMBIE.create(world);
                if (zombie != null) {
                    spawnMobAt(world, zombie, center.add(-3, 0, 0));
                }
                CaveSpiderEntity spider = EntityType.CAVE_SPIDER.create(world);
                if (spider != null) {
                    spawnMobAt(world, spider, center.add(3, 0, 0));
                }
            }
        }
    }

    private void spawnBossWave(ServerWorld world) {
        activeMobUuids.clear();
        BlockPos center = getCenterPos();

        // Boss: "MONSTRO"
        ZombieEntity boss = EntityType.ZOMBIE.create(world);
        if (boss != null) {
            boss.refreshPositionAndAngles(center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0, 0);
            boss.initialize(world, world.getLocalDifficulty(center), SpawnReason.EVENT, null, null);
            boss.setCustomName(Text.literal("§4§lMONSTRO").formatted(Formatting.BOLD));
            boss.setCustomNameVisible(true);

            var maxHealthAttr = boss.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(40.0);
                boss.setHealth(40.0f);
            }
            boss.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 99999, 0));
            boss.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 99999, 0));

            world.spawnEntity(boss);
            activeMobUuids.add(boss.getUuid());
        }

        // 1 Spider minion
        SpiderEntity spider = EntityType.SPIDER.create(world);
        if (spider != null) {
            spawnMobAt(world, spider, center.add(-4, 0, -3));
        }
    }

    private void spawnMobAt(ServerWorld world, MobEntity mob, BlockPos pos) {
        mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null, null);
        world.spawnEntity(mob);
        activeMobUuids.add(mob.getUuid());
    }

    public void tick(ServerWorld world) {
        if (world.getRegistryKey() != World.OVERWORLD) {
            return;
        }

        if (state != RoomState.ACTIVE_COMBAT) {
            return;
        }

        // 1. Remove dead/despawned/discarded UUIDs
        activeMobUuids.removeIf(uuid -> {
            Entity entity = world.getEntity(uuid);
            return entity == null || !entity.isAlive() || entity.isRemoved();
        });

        // 2. Spatial check: Check if there are any remaining living hostile mobs inside the room
        List<MobEntity> livingHostilesInRoom = world.getEntitiesByClass(
                MobEntity.class,
                boundingBox,
                mob -> mob.isAlive() && (mob instanceof HostileEntity || mob instanceof SlimeEntity)
        );

        // Room is cleared if either all tracked UUIDs are dead OR there are zero living hostiles inside the room
        if (activeMobUuids.isEmpty() || livingHostilesInRoom.isEmpty()) {
            finishCombat(world);
        }
    }

    private void finishCombat(ServerWorld world) {
        this.state = RoomState.CLEARED;
        IsaacMod.LOGGER.info("Room at ({}, {}) combat completed! Unlocking all doors.", gridX, gridZ);

        // Automatically open ALL exits
        unlockExits(world);

        BlockPos center = getCenterPos();

        if (roomType == RoomType.BOSS) {
            spawnBossRewardChest(world);
            spawnTrapdoor(world);

            world.playSound(null, center.getX(), center.getY(), center.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.2f, 0.8f);
            world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                    80, 2.0, 1.5, 2.0, 0.2);

            for (ServerPlayerEntity p : world.getPlayers()) {
                com.isaacmod.stats.HolyMantleHandler.recharge(p);
                for (ItemStack s : p.getInventory().main) {
                    if (!s.isEmpty() && s.getItem() instanceof com.isaacmod.item.IsaacActiveItem activeItem) {
                        activeItem.addCharge(s, p);
                    }
                }
                p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("BOSS DEFEATED!").formatted(Formatting.GOLD, Formatting.BOLD)));
                p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Step into the Trapdoor to descend!").formatted(Formatting.YELLOW)));
                p.sendMessage(Text.literal("§a[Isaac] Boss defeated! All doors opened, and the trapdoor to the next floor has appeared at the center!"), false);
            }
        } else {
            spawnRewardChest(world);

            world.playSound(null, center.getX(), center.getY(), center.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                    40, 1.0, 1.0, 1.0, 0.2);

            for (ServerPlayerEntity p : world.getPlayers()) {
                com.isaacmod.stats.HolyMantleHandler.recharge(p);
                for (ItemStack s : p.getInventory().main) {
                    if (!s.isEmpty() && s.getItem() instanceof com.isaacmod.item.IsaacActiveItem activeItem) {
                        activeItem.addCharge(s, p);
                    }
                }
                p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("ROOM CLEARED!").formatted(Formatting.GREEN, Formatting.BOLD)));
                p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Doors unlocked! Proceed to the next room.").formatted(Formatting.YELLOW)));
                p.sendMessage(Text.literal("§a[Isaac] Room cleared! All doors are now open. Proceed through any doorway to continue!"), false);
            }
        }
    }

    private void spawnRewardChest(ServerWorld world) {
        BlockPos center = getCenterPos();
        world.setBlockState(center, Blocks.CHEST.getDefaultState(), 3);

        BlockEntity be = world.getBlockEntity(center);
        if (be instanceof ChestBlockEntity chest) {
            // Rebalanced: 15% chance for an Isaac artifact (passive/active/trash)
            if (world.getRandom().nextFloat() < 0.15f) {
                var item = IsaacRunManager.getRunItemPool().drawItem(world.getRandom());
                chest.setStack(13, new ItemStack(item));
            }

            // Authentic Isaac pickups: Red Hearts, Soul Hearts, Black Hearts, Bombs, Keys, Coins
            chest.setStack(11, new ItemStack(IsaacItems.RED_HEART, 1 + world.getRandom().nextInt(2)));

            float r = world.getRandom().nextFloat();
            if (r < 0.35f) {
                chest.setStack(15, new ItemStack(IsaacItems.SOUL_HEART, 1));
            } else if (r < 0.60f) {
                chest.setStack(15, new ItemStack(IsaacItems.BLACK_HEART, 1));
            } else if (r < 0.80f) {
                chest.setStack(15, new ItemStack(IsaacItems.ISAAC_BOMB, 1 + world.getRandom().nextInt(2)));
            } else {
                chest.setStack(15, new ItemStack(IsaacItems.ISAAC_KEY, 1));
            }

            if (world.getRandom().nextBoolean()) {
                chest.setStack(9, new ItemStack(IsaacItems.ISAAC_COIN, 1 + world.getRandom().nextInt(3)));
            }
        }
    }

    private void spawnBossRewardChest(ServerWorld world) {
        BlockPos chestPos = getCenterPos().add(2, 0, 0);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 3);

        BlockEntity be = world.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            // Guaranteed Boss Artifact from non-repeating Isaac pool
            var item = IsaacRunManager.getRunItemPool().drawItem(world.getRandom());
            chest.setStack(13, new ItemStack(item));

            // Generous health pickups & supplies for boss victory
            chest.setStack(11, new ItemStack(IsaacItems.RED_HEART, 2 + world.getRandom().nextInt(2)));
            chest.setStack(15, new ItemStack(IsaacItems.SOUL_HEART, 1 + world.getRandom().nextInt(2)));
            chest.setStack(9, new ItemStack(IsaacItems.BLACK_HEART, 1));
            chest.setStack(17, new ItemStack(IsaacItems.ISAAC_BOMB, 2));
            chest.setStack(7, new ItemStack(IsaacItems.ISAAC_KEY, 1));
            chest.setStack(21, new ItemStack(IsaacItems.ISAAC_COIN, 3 + world.getRandom().nextInt(4)));
        }
    }

    private void spawnTrapdoor(ServerWorld world) {
        BlockPos center = getCenterPos();
        world.setBlockState(center.down(), Blocks.END_PORTAL_FRAME.getDefaultState(), 3);
        world.setBlockState(center, Blocks.IRON_TRAPDOOR.getDefaultState(), 3);
        this.trapdoorSpawned = true;
    }

    public void setupTreasureRoomPedestal(ServerWorld world) {
        BlockPos center = getCenterPos();
        world.setBlockState(center.down(), Blocks.GOLD_BLOCK.getDefaultState(), 3);
        world.setBlockState(center, Blocks.CHEST.getDefaultState(), 3);

        BlockEntity be = world.getBlockEntity(center);
        if (be instanceof ChestBlockEntity chest) {
            // Guaranteed Treasure Room Artifact from non-repeating Isaac pool
            var item = IsaacRunManager.getRunItemPool().drawItem(world.getRandom());
            chest.setStack(13, new ItemStack(item));
            chest.setStack(11, new ItemStack(IsaacItems.SOUL_HEART, 1));
        }
    }
}
