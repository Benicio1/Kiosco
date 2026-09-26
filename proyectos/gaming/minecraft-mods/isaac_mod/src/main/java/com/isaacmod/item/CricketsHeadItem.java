package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CricketsHeadItem extends IsaacPassiveItem {
    public CricketsHeadItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Cricket's Head: +0.5 flat damage and 1.5x damage multiplier (Huge DMG UP!)
        builder.addDamage(0.5f);
        builder.multiplyDamage(1.5f);
    }

    @Override
    public int getPriority() {
        return 200; // Multipliers evaluate after flat adds
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.crickets_head").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("DMG Up (+0.5 + 50% Multiplier)").formatted(Formatting.GRAY));
    }
}
