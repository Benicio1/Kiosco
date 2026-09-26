package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SacredHeartItem extends IsaacPassiveItem {
    public SacredHeartItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Sacred Heart: Massive damage boost, homing tears, full heart heal
        builder.addDamage(3.0f);
        builder.multiplyDamage(2.3f);
        builder.setHoming(true);
        builder.multiplyShotSpeed(0.85f);
        builder.addFireRate(2.0f);
    }

    @Override
    public int getPriority() {
        return 160; // Applies multiplier after flat damage
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.sacred_heart").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Homing Tears + Huge Damage Multiplier (x2.3 DMG, +3 Flat)").formatted(Formatting.GRAY));
    }
}
