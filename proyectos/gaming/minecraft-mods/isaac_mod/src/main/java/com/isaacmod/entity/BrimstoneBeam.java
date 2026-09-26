package com.isaacmod.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.List;

public class BrimstoneBeam {
    public static final float MAX_DISTANCE = 24.0f;

    public static void fire(World world, PlayerEntity user, float damage) {
        Vec3d eyePos = user.getEyePos();
        Vec3d lookDir = user.getRotationVector();
        Vec3d endPos = eyePos.add(lookDir.multiply(MAX_DISTANCE));

        // Raycast against blocks so laser stops on solid walls
        BlockHitResult blockHit = world.raycast(new RaycastContext(
                eyePos, endPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                user
        ));

        Vec3d actualEnd = (blockHit.getType() != HitResult.Type.MISS) ? blockHit.getPos() : endPos;
        double actualDistance = eyePos.distanceTo(actualEnd);

        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            // Spawn thick blood laser beam particles
            DustParticleEffect bloodDust = new DustParticleEffect(new Vector3f(0.85f, 0.05f, 0.05f), 1.6f);
            int particleCount = (int) (actualDistance * 6);
            for (int i = 0; i <= particleCount; i++) {
                double progress = (double) i / particleCount;
                Vec3d point = eyePos.lerp(actualEnd, progress);
                serverWorld.spawnParticles(bloodDust, point.x, point.y, point.z, 2, 0.05, 0.05, 0.05, 0.0);
                if (i % 3 == 0) {
                    serverWorld.spawnParticles(ParticleTypes.CRIMSON_SPORE, point.x, point.y, point.z, 2, 0.1, 0.1, 0.1, 0.02);
                }
            }

            // Damage all living entities intersected by the laser cylinder
            Box beamBounds = new Box(eyePos, actualEnd).expand(1.2);
            List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, beamBounds,
                    e -> e.isAlive() && e != user);

            DamageSource damageSource = serverWorld.getDamageSources().playerAttack(user);
            int hitCount = 0;
            for (LivingEntity target : targets) {
                // Distance from target to line segment
                Vec3d targetPos = target.getEyePos();
                Vec3d line = actualEnd.subtract(eyePos);
                double lineLenSq = line.dotProduct(line);
                if (lineLenSq > 0.0001) {
                    double t = Math.max(0.0, Math.min(1.0, targetPos.subtract(eyePos).dotProduct(line) / lineLenSq));
                    Vec3d projection = eyePos.add(line.multiply(t));
                    if (targetPos.squaredDistanceTo(projection) <= 2.0) {
                        target.damage(damageSource, damage * 1.5f);
                        serverWorld.spawnParticles(ParticleTypes.DAMAGE_INDICATOR,
                                target.getX(), target.getY() + 1.0, target.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
                        hitCount++;
                    }
                }
            }

            // Laser audio
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.PLAYERS, 1.2f, 0.7f);
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 1.0f, 0.6f);

            user.sendMessage(Text.literal("§4§l[Brimstone] Blood laser pierced " + hitCount + " enemies!"), true);
        }
    }
}
