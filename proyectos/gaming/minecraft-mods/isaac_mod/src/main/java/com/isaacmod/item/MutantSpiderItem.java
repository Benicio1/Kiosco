package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MutantSpiderItem extends IsaacPassiveItem {
    public MutantSpiderItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Mutant Spider: Quad Shot! Fires 4 tears in a spread, reduced fire rate
        builder.setTearCount(4);
        builder.setSpreadAngle(18.0f);
        builder.multiplyFireRate(1.9f);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.mutant_spider").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        tooltip.add(Text.literal("Quad Shot (Fires 4 tears in a wide fan)").formatted(Formatting.GRAY));
    }
}
