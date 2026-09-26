package com.isaacmod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class YumHeartItem extends IsaacActiveItem {
    public YumHeartItem(Settings settings) {
        super(settings, 1); // 1 room charge
    }

    @Override
    protected boolean onActivate(World world, PlayerEntity user, ItemStack stack) {
        if (user.getHealth() >= user.getMaxHealth()) {
            user.sendMessage(Text.literal("§c[Yum Heart] Health is already full!"), true);
            return false;
        }

        user.heal(2.0f); // Restores 1 full heart (2 HP)
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.HEART, user.getX(), user.getY() + 1.2, user.getZ(),
                    8, 0.4, 0.4, 0.4, 0.1);
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.2f);
        }
        user.sendMessage(Text.literal("§c§l[Yum Heart] Restored 1 Heart!"), true);
        return true;
    }
}
