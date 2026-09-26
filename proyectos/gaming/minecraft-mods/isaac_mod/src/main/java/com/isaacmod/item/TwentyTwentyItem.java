package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TwentyTwentyItem extends IsaacPassiveItem {
    public TwentyTwentyItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // 20/20: Double Shot with NO fire rate penalty!
        builder.setTwentyTwenty(true);
        builder.setTearCount(2);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.twenty_twenty").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        tooltip.add(Text.literal("Double Shot! Fires 2 tears side-by-side with no fire rate penalty").formatted(Formatting.GRAY));
    }
}
