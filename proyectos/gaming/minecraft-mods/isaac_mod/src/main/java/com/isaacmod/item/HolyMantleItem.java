package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HolyMantleItem extends IsaacPassiveItem {
    public HolyMantleItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        builder.setHolyMantle(true);
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.holy_mantle").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Holy Shield (Blocks the first hit of damage in each room)").formatted(Formatting.GRAY));
    }
}
