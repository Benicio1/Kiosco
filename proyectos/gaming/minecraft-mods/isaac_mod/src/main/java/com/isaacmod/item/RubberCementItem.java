package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RubberCementItem extends IsaacPassiveItem {
    public RubberCementItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Rubber Cement: Bouncing tears that reflect off walls and obstacles up to 3 times
        builder.setRubberCement(true);
        builder.addRange(15);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.rubber_cement").formatted(Formatting.GREEN, Formatting.BOLD));
        tooltip.add(Text.literal("Bouncing Tears! Tears bounce off blocks and walls up to 3 times").formatted(Formatting.GRAY));
    }
}
