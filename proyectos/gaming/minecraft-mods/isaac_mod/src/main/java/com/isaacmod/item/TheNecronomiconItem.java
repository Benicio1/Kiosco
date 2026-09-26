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

public class TheNecronomiconItem extends IsaacActiveItem {
    public TheNecronomiconItem(Settings settings) {
        super(settings, 3); // 3 room charges
    }

    @Override
    protected boolean onActivate(World world, PlayerEntity user, ItemStack stack) {
        if (!(world instanceof ServerWorld serverWorld)) return false;

        Box area = user.getBoundingBox().expand(16.0);
        List<MobEntity> targets = serverWorld.getEntitiesByClass(MobEntity.class, area,
                MobEntity::isAlive);

        if (targets.isEmpty()) {
            user.sendMessage(Text.literal("§c[The Necronomicon] No enemies in range!"), true);
            return false;
        }

        for (MobEntity mob : targets) {
            mob.damage(serverWorld.getDamageSources().magic(), 15.0f);
            serverWorld.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, mob.getX(), mob.getY() + 1.0, mob.getZ(),
                    8, 0.3, 0.3, 0.3, 0.1);
        }

        serverWorld.spawnParticles(ParticleTypes.SQUID_INK, user.getX(), user.getY() + 1.0, user.getZ(),
                50, 2.0, 1.0, 2.0, 0.2);
        serverWorld.spawnParticles(ParticleTypes.SOUL, user.getX(), user.getY() + 1.0, user.getZ(),
                30, 1.5, 0.8, 1.5, 0.1);
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.0f, 0.8f);

        user.sendMessage(Text.literal("§5§l[The Necronomicon] Death curse struck " + targets.size() + " enemies for 15 damage!"), true);
        return true;
    }
}
