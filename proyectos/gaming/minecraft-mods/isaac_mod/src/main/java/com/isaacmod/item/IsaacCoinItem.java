package com.isaacmod.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IsaacCoinItem extends Item {
    public IsaacCoinItem(Settings settings) {
        super(settings.maxCount(99));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.isaac_coin").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Penny! Valuable coin currency for Isaac dungeon shops").formatted(Formatting.GRAY));
    }
}
