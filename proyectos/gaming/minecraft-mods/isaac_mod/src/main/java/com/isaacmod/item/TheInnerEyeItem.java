package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TheInnerEyeItem extends IsaacPassiveItem {
    public TheInnerEyeItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // The Inner Eye: Triple shot in a spread, tears down (longer cooldown)
        builder.setTearCount(3);
        builder.setSpreadAngle(14.0f);
        builder.multiplyFireRate(1.75f);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.the_inner_eye").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Triple Shot (3-tear spread, slower fire rate)").formatted(Formatting.GRAY));
    }
}
