package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TheWaferItem extends IsaacPassiveItem {
    public TheWaferItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // The Wafer: Damage resistance, caps all incoming damage to only 1 HP (half a heart)
        builder.setTheWafer(true);
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.the_wafer").formatted(Formatting.YELLOW, Formatting.BOLD));
        tooltip.add(Text.literal("Damage Resistance! Caps all damage taken to only 1 HP (half heart)").formatted(Formatting.GRAY));
    }
}
