package com.isaacmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IsaacBombEntity extends TntEntity {
    private LivingEntity owner;

    public IsaacBombEntity(EntityType<? extends TntEntity> entityType, World world) {
        super(entityType, world);
    }

    public IsaacBombEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        super(world, x, y, z, igniter);
        this.owner = igniter;
    }

    @Override
    public void tick() {
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
        }

        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.98));
        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
        }

        int fuse = this.getFuse() - 1;
        this.setFuse(fuse);
        if (fuse <= 0) {
            this.discard();
            if (!this.getWorld().isClient()) {
                this.explodeWithoutGriefing();
            }
        } else {
            this.updateWaterState();
            if (this.getWorld().isClient()) {
                this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void explodeWithoutGriefing() {
        // Safe explosion: ExplosionSourceType.NONE guarantees ZERO block destruction
        this.getWorld().createExplosion(
                this,
                null,
                null,
                this.getX(),
                this.getBodyY(0.0625),
                this.getZ(),
                4.0f,
                false,
                World.ExplosionSourceType.NONE
        );

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getBodyY(0.5), this.getZ(), 1, 0, 0, 0, 0);

            // Deal heavy blast damage (25 damage = 12.5 hearts) to all nearby hostile mobs
            Box blastBox = new Box(this.getX() - 4.5, this.getY() - 2.0, this.getZ() - 4.5,
                    this.getX() + 4.5, this.getY() + 3.0, this.getZ() + 4.5);
            List<LivingEntity> targets = serverWorld.getEntitiesByClass(
                    LivingEntity.class,
                    blastBox,
                    e -> e.isAlive() && !(e instanceof PlayerEntity)
            );
            for (LivingEntity target : targets) {
                target.damage(serverWorld.getDamageSources().explosion(this, this.owner), 25.0f);
            }
        }
    }
}
