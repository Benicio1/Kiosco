package com.isaacmod.stats;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HolyMantleHandler {
    private static final Map<UUID, Boolean> shieldActive = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3d> lastPositions = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> stationaryTicks = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> isApplyingWaferDamage = ThreadLocal.withInitial(() -> false);

    public static void initialize() {
        // Track stationary ticks for Gnawed Leaf
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() != World.OVERWORLD) return;
            for (ServerPlayerEntity player : world.getPlayers()) {
                UUID id = player.getUuid();
                Vec3d current = player.getPos();
                Vec3d prev = lastPositions.get(id);
                if (prev != null && current.squaredDistanceTo(prev) < 0.001) {
                    stationaryTicks.put(id, stationaryTicks.getOrDefault(id, 0) + 1);
                } else {
                    stationaryTicks.put(id, 0);
                }
                lastPositions.put(id, current);

                // Gnawed Leaf visual effect when stone form triggers
                if (stationaryTicks.getOrDefault(id, 0) == 30) {
                    PlayerStats stats = PlayerStats.compute(player);
                    if (stats.hasGnawedLeaf()) {
                        player.sendMessage(Text.literal("§7§l[Gnawed Leaf] Stone statue form active! Invulnerable until you move."), true);
                        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.BLOCK_STONE_PLACE, SoundCategory.PLAYERS, 1.0f, 0.8f);
                        world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(),
                                15, 0.3, 0.5, 0.3, 0.02);
                    }
                }
            }
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                if (isApplyingWaferDamage.get()) {
                    return true;
                }

                PlayerStats stats = PlayerStats.compute(player);

                // 1. Pyromaniac: Explosions heal the player!
                if (stats.hasPyromaniac() && (source.isOf(DamageTypes.EXPLOSION) || source.isOf(DamageTypes.PLAYER_EXPLOSION))) {
                    player.heal(Math.max(2.0f, amount * 0.5f));
                    player.getServerWorld().spawnParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(),
                            8, 0.4, 0.4, 0.4, 0.1);
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.4f);
                    player.sendMessage(Text.literal("§6§l[Pyromaniac] Explosion absorbed and healed health!"), true);
                    return false;
                }

                // 2. Gnawed Leaf: Complete invulnerability when standing still for >= 1.5 seconds (30 ticks)
                if (stats.hasGnawedLeaf() && stationaryTicks.getOrDefault(player.getUuid(), 0) >= 30) {
                    player.getServerWorld().spawnParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(),
                            10, 0.4, 0.4, 0.4, 0.1);
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_STONE_HIT, SoundCategory.PLAYERS, 1.2f, 1.0f);
                    player.sendMessage(Text.literal("§7[Gnawed Leaf] Stone statue deflected the attack!"), true);
                    return false;
                }

                // 3. Holy Mantle: Absorbs the first damage hit in each room
                if (stats.hasHolyMantle()) {
                    if (shieldActive.getOrDefault(player.getUuid(), true)) {
                        shieldActive.put(player.getUuid(), false);
                        player.getServerWorld().spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
                                30, 0.5, 0.5, 0.5, 0.1);
                        player.getServerWorld().spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(),
                                20, 0.4, 0.4, 0.4, 0.1);
                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.2f, 1.2f);
                        player.sendMessage(Text.literal("§b§l[Holy Mantle] Shield absorbed the hit!"), true);
                        return false;
                    }
                }

                // 4. The Wafer: Caps all damage taken to only 1 HP (half a heart)
                if (stats.hasTheWafer() && amount > 1.0f) {
                    try {
                        isApplyingWaferDamage.set(true);
                        player.damage(source, 1.0f);
                    } finally {
                        isApplyingWaferDamage.set(false);
                    }
                    player.getServerWorld().spawnParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0, player.getZ(),
                            12, 0.3, 0.3, 0.3, 0.1);
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundCategory.PLAYERS, 1.0f, 1.3f);
                    player.sendMessage(Text.literal("§e§l[The Wafer] Heavy damage reduced to 1 HP!"), true);
                    return false;
                }
            }
            return true;
        });
    }

    public static void recharge(ServerPlayerEntity player) {
        shieldActive.put(player.getUuid(), true);
    }

    public static void rechargeAll() {
        shieldActive.clear();
    }
}
