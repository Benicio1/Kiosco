package com.isaacmod.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IsaacBombItem extends Item {
    public IsaacBombItem(Settings settings) {
        super(settings.maxCount(64));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            com.isaacmod.entity.IsaacBombEntity bomb = new com.isaacmod.entity.IsaacBombEntity(world, user.getX(), user.getY(), user.getZ(), user);
            bomb.setFuse(40); // 2 seconds
            world.spawnEntity(bomb);

            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
            user.sendMessage(Text.literal("§6[Isaac] Bomb placed! Detonating in 2 seconds..."), true);

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.isaac_bomb").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Place down to detonate after a 2-second fuse").formatted(Formatting.GRAY));
    }
}
