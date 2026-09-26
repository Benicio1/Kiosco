package com.isaacmod.item;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ThePoopItem extends IsaacActiveItem {
    public ThePoopItem(Settings settings) {
        super(settings, 1); // 1 room charge
    }

    @Override
    protected boolean onActivate(World world, PlayerEntity user, ItemStack stack) {
        if (!(world instanceof ServerWorld serverWorld)) return false;

        BlockPos pos = user.getBlockPos();
        // Place a poop block (mud block) if air
        if (serverWorld.getBlockState(pos).isAir()) {
            serverWorld.setBlockState(pos, Blocks.MUD.getDefaultState(), 3);
        } else if (serverWorld.getBlockState(pos.offset(user.getHorizontalFacing())).isAir()) {
            serverWorld.setBlockState(pos.offset(user.getHorizontalFacing()), Blocks.MUD.getDefaultState(), 3);
        }

        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_PIG_AMBIENT, SoundCategory.PLAYERS, 1.2f, 0.4f);
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.BLOCKS, 1.0f, 0.5f);

        serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, user.getX(), user.getY() + 0.3, user.getZ(),
                12, 0.3, 0.2, 0.3, 0.02);

        user.sendMessage(Text.literal("§6§l[The Poop] Placed a fresh pile of poop!"), true);
        return true;
    }
}
