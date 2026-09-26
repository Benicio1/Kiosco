package com.isaacmod.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IsaacKeyItem extends Item {
    public IsaacKeyItem(Settings settings) {
        super(settings.maxCount(64));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.isaac_key").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Golden Key! Used to unlock special chests and doors").formatted(Formatting.GRAY));
    }
}
