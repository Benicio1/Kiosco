package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PolyphemusItem extends IsaacPassiveItem {
    public PolyphemusItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Polyphemus: Mega tears! Massive damage, slower fire rate, giant tears
        builder.addDamage(8.0f);
        builder.multiplyDamage(1.5f);
        builder.multiplyFireRate(1.8f);
        builder.setGiant(true);
    }

    @Override
    public int getPriority() {
        return 150;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.polyphemus").formatted(Formatting.DARK_RED, Formatting.BOLD));
        tooltip.add(Text.literal("Mega Tears! (+Huge Damage, Giant Tears, Slower Tears)").formatted(Formatting.GRAY));
    }
}
