package com.isaacmod.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlackHeartItem extends Item {
    public BlackHeartItem(Settings settings) {
        super(settings.maxCount(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            int currentAmp = 0;
            if (user.hasStatusEffect(StatusEffects.ABSORPTION)) {
                var current = user.getStatusEffect(StatusEffects.ABSORPTION);
                if (current != null && current.getAmplifier() < 3) {
                    currentAmp = current.getAmplifier() + 1;
                }
            }
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 20 * 600, currentAmp, false, true, true));

            // Black Heart Necronomicon detonation: also hits any nearby hostiles for 8 damage!
            if (world instanceof ServerWorld serverWorld) {
                Box area = user.getBoundingBox().expand(8.0);
                List<MobEntity> hostiles = serverWorld.getEntitiesByClass(MobEntity.class, area,
                        MobEntity::isAlive);
                for (MobEntity m : hostiles) {
                    m.damage(serverWorld.getDamageSources().magic(), 8.0f);
                }

                serverWorld.spawnParticles(ParticleTypes.SOUL, user.getX(), user.getY() + 1.0, user.getZ(),
                        25, 0.5, 0.5, 0.5, 0.1);
                serverWorld.spawnParticles(ParticleTypes.SQUID_INK, user.getX(), user.getY() + 1.0, user.getZ(),
                        20, 0.4, 0.4, 0.4, 0.05);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.ENTITY_WITHER_HURT, SoundCategory.PLAYERS, 1.0f, 1.4f);
            }

            user.sendMessage(Text.literal("§8§l+Black Heart Shield & Dark Pulse Activated!"), true);
            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.black_heart").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("Right-click to gain Shield Hearts + unleash a dark room pulse").formatted(Formatting.GRAY));
    }
}
