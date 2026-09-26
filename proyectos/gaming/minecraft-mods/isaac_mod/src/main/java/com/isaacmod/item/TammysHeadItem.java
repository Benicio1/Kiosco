package com.isaacmod.item;

import com.isaacmod.entity.TearEntity;
import com.isaacmod.stats.PlayerStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class TammysHeadItem extends IsaacActiveItem {
    public TammysHeadItem(Settings settings) {
        super(settings, 1); // 1 room charge
    }

    @Override
    protected boolean onActivate(World world, PlayerEntity user, ItemStack stack) {
        if (!(world instanceof ServerWorld serverWorld)) return false;

        PlayerStats stats = PlayerStats.compute(user);
        int tearCount = 10;
        double angleStep = 360.0 / tearCount;

        for (int i = 0; i < tearCount; i++) {
            double angle = Math.toRadians(i * angleStep);
            TearEntity tear = new TearEntity(serverWorld, user);
            tear.setDamage(stats.getDamage());
            tear.setRangeTicks(stats.getRange());
            tear.setExplosive(stats.isExplosive());
            tear.setLobbed(stats.isLobbed());
            tear.setHoming(stats.isHoming());
            tear.setPiercing(stats.isPiercing());
            tear.setGiant(stats.isGiant());
            tear.setGodheadAura(stats.hasGodheadAura());
            tear.setRubberCement(stats.hasRubberCement());

            float yaw = (float) (i * angleStep);
            tear.setVelocity(user, 0.0f, yaw, 0.0f, stats.getShotSpeed() * 1.1f, 1.0f);
            serverWorld.spawnEntity(tear);
        }

        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_CAT_HISS, SoundCategory.PLAYERS, 1.0f, 1.4f);
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 1.2f, 0.5f);

        user.sendMessage(Text.literal("§d§l[Tammy's Head] 360° Radial Tear Burst fired!"), true);
        return true;
    }
}
