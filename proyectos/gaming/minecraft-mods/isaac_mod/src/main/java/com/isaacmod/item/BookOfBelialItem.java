package com.isaacmod.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class BookOfBelialItem extends IsaacActiveItem {
    public BookOfBelialItem(Settings settings) {
        super(settings, 1); // 1 room charge
    }

    @Override
    protected boolean onActivate(World world, PlayerEntity user, ItemStack stack) {
        // Grants demonic combat fury: Strength II and Speed I
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 45, 1, false, true, true));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20 * 45, 0, false, true, true));

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.FLAME, user.getX(), user.getY() + 1.0, user.getZ(),
                    35, 0.4, 0.6, 0.4, 0.1);
            serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, user.getX(), user.getY() + 1.2, user.getZ(),
                    20, 0.3, 0.5, 0.3, 0.05);
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 0.9f, 1.2f);
        }

        user.sendMessage(Text.literal("§4§l[Book of Belial] Demonic Fury unleashed! (+Damage & Speed)"), true);
        return true;
    }
}
