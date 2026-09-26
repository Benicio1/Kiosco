package com.isaacmod.item;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.List;

public class LemonMishapItem extends IsaacActiveItem {
    public LemonMishapItem(Settings settings) {
        super(settings, 2); // 2 room charges
    }

    @Override
    protected boolean onActivate(World world, PlayerEntity user, ItemStack stack) {
        if (!(world instanceof ServerWorld serverWorld)) return false;

        // Spawns a small yellow sizzle puddle dealing minor 3 damage
        DustParticleEffect yellowDust = new DustParticleEffect(new Vector3f(1.0f, 1.0f, 0.1f), 1.4f);
        serverWorld.spawnParticles(yellowDust, user.getX(), user.getY() + 0.1, user.getZ(),
                30, 1.5, 0.1, 1.5, 0.0);

        Box puddleBox = user.getBoundingBox().expand(2.5, 0.5, 2.5);
        List<MobEntity> hostiles = serverWorld.getEntitiesByClass(MobEntity.class, puddleBox, MobEntity::isAlive);
        for (MobEntity m : hostiles) {
            m.damage(serverWorld.getDamageSources().magic(), 3.0f);
        }

        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 1.0f, 1.5f);

        user.sendMessage(Text.literal("§e[Lemon Mishap] Sizzling lemon puddle spilled!"), true);
        return true;
    }
}
