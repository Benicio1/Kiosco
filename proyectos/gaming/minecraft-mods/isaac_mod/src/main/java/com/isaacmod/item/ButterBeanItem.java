package com.isaacmod.item;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class ButterBeanItem extends IsaacActiveItem {
    public ButterBeanItem(Settings settings) {
        super(settings, 1); // 1 room charge
    }

    @Override
    protected boolean onActivate(World world, PlayerEntity user, ItemStack stack) {
        if (!(world instanceof ServerWorld serverWorld)) return false;

        Box area = user.getBoundingBox().expand(4.0);
        List<MobEntity> hostiles = serverWorld.getEntitiesByClass(MobEntity.class, area, MobEntity::isAlive);

        for (MobEntity m : hostiles) {
            double dx = m.getX() - user.getX();
            double dz = m.getZ() - user.getZ();
            m.takeKnockback(1.2, -dx, -dz);
            m.damage(serverWorld.getDamageSources().generic(), 1.0f);
        }

        serverWorld.spawnParticles(ParticleTypes.POOF, user.getX(), user.getY() + 0.5, user.getZ(),
                20, 0.5, 0.3, 0.5, 0.05);
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_LLAMA_SPIT, SoundCategory.PLAYERS, 1.2f, 0.5f);

        user.sendMessage(Text.literal("§e[Butter Bean] *Fart!* Enemies blown back slightly."), true);
        return true;
    }
}
