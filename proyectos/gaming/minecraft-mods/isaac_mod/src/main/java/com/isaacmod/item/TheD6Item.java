package com.isaacmod.item;

import com.isaacmod.dungeon.DungeonRoom;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TheD6Item extends IsaacActiveItem {
    public TheD6Item(Settings settings) {
        super(settings, 2); // 2 room charges
    }

    @Override
    protected boolean onActivate(World world, PlayerEntity user, ItemStack stack) {
        if (!(world instanceof ServerWorld serverWorld)) return false;

        BlockPos playerPos = user.getBlockPos();
        boolean rerolled = false;

        // Search for reward or pedestal chests within 10 blocks
        for (BlockPos pos : BlockPos.iterate(playerPos.add(-10, -3, -10), playerPos.add(10, 3, 10))) {
            BlockEntity be = serverWorld.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity chest) {
                // Find non-empty item to re-roll
                for (int slot = 0; slot < chest.size(); slot++) {
                    ItemStack slotStack = chest.getStack(slot);
                    if (!slotStack.isEmpty()) {
                        var newItem = com.isaacmod.dungeon.IsaacRunManager.getRunItemPool().drawItem(serverWorld.getRandom());
                        chest.setStack(slot, new ItemStack(newItem, slotStack.getCount()));
                        rerolled = true;

                        serverWorld.spawnParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                                30, 0.4, 0.4, 0.4, 0.2);
                        serverWorld.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.2f, 1.1f);
                    }
                }
            }
        }

        if (rerolled) {
            user.sendMessage(Text.literal("§6§l[The D6] Pedestal items re-rolled into new artifacts!"), true);
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1.0f, 1.6f);
            return true;
        } else {
            user.sendMessage(Text.literal("§c[The D6] No pedestal chest found nearby to re-roll!"), true);
            return false;
        }
    }
}
