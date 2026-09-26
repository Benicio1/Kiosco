package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GnawedLeafItem extends IsaacPassiveItem {
    public GnawedLeafItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Gnawed Leaf: Standing still for 1.5s petrifies Isaac into an invulnerable stone statue
        builder.setGnawedLeaf(true);
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.gnawed_leaf").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
        tooltip.add(Text.literal("Stand still for 1.5 seconds to turn to stone and become invulnerable").formatted(Formatting.GRAY));
    }
}
