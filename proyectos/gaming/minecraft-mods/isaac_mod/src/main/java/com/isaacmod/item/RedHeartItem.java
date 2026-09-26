package com.isaacmod.item;

import net.minecraft.client.item.TooltipContext;
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

public class RedHeartItem extends Item {
    public RedHeartItem(Settings settings) {
        super(settings.maxCount(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getHealth() < user.getMaxHealth()) {
            if (!world.isClient()) {
                user.heal(4.0f); // Restores 2 full hearts (4 HP)
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HEART, user.getX(), user.getY() + 1.2, user.getZ(),
                            6, 0.3, 0.3, 0.3, 0.1);
                    serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.2f);
                }
                user.sendMessage(Text.literal("§c+2 Hearts Restored!"), true);
                if (!user.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
            }
            return TypedActionResult.success(stack, world.isClient());
        } else {
            if (!world.isClient()) {
                user.sendMessage(Text.literal("§cHealth already full!"), true);
            }
            return TypedActionResult.fail(stack);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.red_heart").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("Right-click to restore 2 hearts (4 HP)").formatted(Formatting.GRAY));
    }
}
