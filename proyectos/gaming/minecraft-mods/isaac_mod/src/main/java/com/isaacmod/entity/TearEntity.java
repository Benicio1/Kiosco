package com.isaacmod.entity;

import com.isaacmod.item.IsaacItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class TearEntity extends ThrownItemEntity {
    private float damage = 3.5f;
    private int rangeTicks = 40;
    private int age = 0;
    private boolean explosive = false;
    private boolean lobbed = false;
    private boolean homing = false;
    private boolean piercing = false;
    private boolean giant = false;
    private boolean godheadAura = false;
    private boolean rubberCement = false;
    private int pierceCount = 0;
    private int bounceCount = 0;

    public TearEntity(EntityType<? extends TearEntity> entityType, World world) {
        super(entityType, world);
    }

    public TearEntity(World world, LivingEntity owner) {
        super(IsaacEntities.TEAR, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        if (this.godheadAura) return IsaacItems.GODHEAD;
        if (this.rubberCement) return IsaacItems.RUBBER_CEMENT;
        if (this.giant) return IsaacItems.POLYPHEMUS;
        if (this.explosive) return IsaacItems.IPECAC;
        if (this.piercing) return IsaacItems.BRIMSTONE;
        if (this.homing) return IsaacItems.SPOON_BENDER;
        return IsaacItems.ISAAC_TEAR;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(getDefaultItem());
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
    }

    public void setRangeTicks(int rangeTicks) {
        this.rangeTicks = rangeTicks;
    }

    public int getRangeTicks() {
        return rangeTicks;
    }

    public void setExplosive(boolean explosive) {
        this.explosive = explosive;
    }

    public boolean isExplosive() {
        return explosive;
    }

    public void setLobbed(boolean lobbed) {
        this.lobbed = lobbed;
    }

    public boolean isLobbed() {
        return lobbed;
    }

    public void setHoming(boolean homing) {
        this.homing = homing;
    }

    public boolean isHoming() {
        return homing;
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public void setGiant(boolean giant) {
        this.giant = giant;
    }

    public boolean isGiant() {
        return giant;
    }

    public void setGodheadAura(boolean godheadAura) {
        this.godheadAura = godheadAura;
    }

    public boolean hasGodheadAura() {
        return godheadAura;
    }

    public void setRubberCement(boolean rubberCement) {
        this.rubberCement = rubberCement;
    }

    public boolean hasRubberCement() {
        return rubberCement;
    }

    @Override
    protected float getGravity() {
        return this.lobbed ? 0.05f : 0.006f;
    }

    @Override
    public void tick() {
        super.tick();

        // 1. Homing Steering Logic
        if (this.homing && !this.getWorld().isClient()) {
            Box searchBox = this.getBoundingBox().expand(8.0);
            List<MobEntity> nearby = this.getWorld().getEntitiesByClass(MobEntity.class, searchBox,
                    m -> m.isAlive() && m != this.getOwner());

            MobEntity closest = null;
            double closestDistSq = Double.MAX_VALUE;
            for (MobEntity m : nearby) {
                double d = m.squaredDistanceTo(this);
                if (d < closestDistSq) {
                    closestDistSq = d;
                    closest = m;
                }
            }

            if (closest != null) {
                Vec3d targetDir = closest.getEyePos().subtract(this.getPos()).normalize();
                Vec3d currentVel = this.getVelocity();
                Vec3d newVel = currentVel.multiply(0.85).add(targetDir.multiply(currentVel.length() * 0.15));
                this.setVelocity(newVel);
            }
        }

        // 2. Godhead Divine Halo Aura
        if (this.godheadAura && !this.getWorld().isClient()) {
            Box auraBox = this.getBoundingBox().expand(1.5);
            List<LivingEntity> inAura = this.getWorld().getEntitiesByClass(LivingEntity.class, auraBox,
                    e -> e.isAlive() && e != this.getOwner());
            DamageSource auraDamage = this.getDamageSources().thrown(this, this.getOwner());
            for (LivingEntity target : inAura) {
                target.damage(auraDamage, Math.max(1.0f, this.damage * 0.25f));
            }
        }

        // 3. Particle Trails
        if (this.getWorld().isClient()) {
            ParticleEffect particle;
            if (this.godheadAura) {
                particle = ParticleTypes.END_ROD;
            } else if (this.rubberCement) {
                particle = ParticleTypes.ITEM_SLIME;
            } else if (this.explosive) {
                particle = ParticleTypes.HAPPY_VILLAGER;
            } else if (this.piercing) {
                particle = ParticleTypes.CRIMSON_SPORE;
            } else if (this.homing) {
                particle = ParticleTypes.WITCH;
            } else if (this.giant) {
                particle = ParticleTypes.CRIT;
            } else {
                particle = ParticleTypes.SPLASH;
            }

            this.getWorld().addParticle(particle,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                    0, 0, 0);

            if (this.godheadAura) {
                // Circular radiant halo particles around tear
                double angle = (this.age * 0.4);
                double rx = Math.cos(angle) * 0.6;
                double rz = Math.sin(angle) * 0.6;
                this.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + rx, this.getY(), this.getZ() + rz, 0, 0, 0);
            }
        } else {
            this.age++;
            if (this.age >= this.rangeTicks) {
                pop();
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity target = entityHitResult.getEntity();

        if (!this.getWorld().isClient()) {
            Entity owner = this.getOwner();
            DamageSource damageSource = this.getDamageSources().thrown(this, owner);
            target.damage(damageSource, this.damage);

            if (this.explosive) {
                explode();
                return;
            }

            // Piercing mechanic: tears punch through up to 3 enemies before disappearing
            if (this.piercing && this.pierceCount < 3) {
                this.pierceCount++;
                if (this.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.DAMAGE_INDICATOR,
                            this.getX(), this.getY(), this.getZ(), 4, 0.2, 0.2, 0.2, 0.1);
                    serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.8f, 1.4f);
                }
                return;
            }

            pop();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient()) {
            if (this.explosive) {
                explode();
                return;
            }

            // Rubber Cement Bouncing Tears mechanic:
            if (this.rubberCement && this.bounceCount < 3) {
                this.bounceCount++;
                Direction side = blockHitResult.getSide();
                Vec3d normal = Vec3d.of(side.getVector());
                Vec3d v = this.getVelocity();
                // Reflect velocity: v' = v - 2*(v·n)*n
                Vec3d reflected = v.subtract(normal.multiply(2 * v.dotProduct(normal))).multiply(0.85);
                this.setVelocity(reflected);

                if (this.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.BLOCK_SLIME_BLOCK_FALL, SoundCategory.PLAYERS, 0.8f, 1.3f);
                    serverWorld.spawnParticles(ParticleTypes.ITEM_SLIME,
                            this.getX(), this.getY(), this.getZ(), 6, 0.2, 0.2, 0.2, 0.1);
                }
                return;
            }

            pop();
        }
    }

    private void explode() {
        this.getWorld().createExplosion(
                this,
                null,
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                2.5f,
                false,
                World.ExplosionSourceType.NONE
        );

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
        }
        this.discard();
    }

    private void pop() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 12, 0.2, 0.2, 0.2, 0.1);
            serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.PLAYERS, 0.5f, 1.8f);
        }
        this.discard();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("TearDamage", this.damage);
        nbt.putInt("RangeTicks", this.rangeTicks);
        nbt.putInt("Age", this.age);
        nbt.putBoolean("Explosive", this.explosive);
        nbt.putBoolean("Lobbed", this.lobbed);
        nbt.putBoolean("Homing", this.homing);
        nbt.putBoolean("Piercing", this.piercing);
        nbt.putBoolean("Giant", this.giant);
        nbt.putBoolean("GodheadAura", this.godheadAura);
        nbt.putBoolean("RubberCement", this.rubberCement);
        nbt.putInt("PierceCount", this.pierceCount);
        nbt.putInt("BounceCount", this.bounceCount);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("TearDamage")) this.damage = nbt.getFloat("TearDamage");
        if (nbt.contains("RangeTicks")) this.rangeTicks = nbt.getInt("RangeTicks");
        if (nbt.contains("Age")) this.age = nbt.getInt("Age");
        if (nbt.contains("Explosive")) this.explosive = nbt.getBoolean("Explosive");
        if (nbt.contains("Lobbed")) this.lobbed = nbt.getBoolean("Lobbed");
        if (nbt.contains("Homing")) this.homing = nbt.getBoolean("Homing");
        if (nbt.contains("Piercing")) this.piercing = nbt.getBoolean("Piercing");
        if (nbt.contains("Giant")) this.giant = nbt.getBoolean("Giant");
        if (nbt.contains("GodheadAura")) this.godheadAura = nbt.getBoolean("GodheadAura");
        if (nbt.contains("RubberCement")) this.rubberCement = nbt.getBoolean("RubberCement");
        if (nbt.contains("PierceCount")) this.pierceCount = nbt.getInt("PierceCount");
        if (nbt.contains("BounceCount")) this.bounceCount = nbt.getInt("BounceCount");
    }
}
