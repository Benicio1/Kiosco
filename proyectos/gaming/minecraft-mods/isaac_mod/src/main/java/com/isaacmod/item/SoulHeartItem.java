package com.isaacmod.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoulHeartItem extends Item {
    public SoulHeartItem(Settings settings) {
        super(settings.maxCount(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            // Grant Absorption hearts (level 0 is 2 extra hearts, level 1 is 4 hearts)
            int currentAmp = 0;
            if (user.hasStatusEffect(StatusEffects.ABSORPTION)) {
                var current = user.getStatusEffect(StatusEffects.ABSORPTION);
                if (current != null && current.getAmplifier() < 3) {
                    currentAmp = current.getAmplifier() + 1;
                }
            }
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 20 * 600, currentAmp, false, true, true));

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, user.getX(), user.getY() + 1.2, user.getZ(),
                        12, 0.4, 0.4, 0.4, 0.05);
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0f, 1.5f);
            }
            user.sendMessage(Text.literal("§b+Soul Heart Shield Activated!"), true);
            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.soul_heart").formatted(Formatting.AQUA, Formatting.BOLD));
        tooltip.add(Text.literal("Right-click to gain Soul Heart shield protection").formatted(Formatting.GRAY));
    }
}
