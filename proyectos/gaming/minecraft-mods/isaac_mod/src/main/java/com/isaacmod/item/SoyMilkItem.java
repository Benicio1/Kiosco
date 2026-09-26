package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoyMilkItem extends IsaacPassiveItem {
    public SoyMilkItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Soy Milk: Ludicrous fire rate (reduced cooldown to 2 ticks!), lower damage
        builder.multiplyDamage(0.25f);
        builder.multiplyFireRate(0.20f);
        builder.addFireRate(-6.0f);
    }

    @Override
    public int getPriority() {
        return 170; // Apply damage reduction multiplier last
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.soy_milk").formatted(Formatting.AQUA, Formatting.BOLD));
        tooltip.add(Text.literal("Ludicrous Fire Rate! Ultra-rapid tears with reduced damage").formatted(Formatting.GRAY));
    }
}
