package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WoodenSpoonItem extends IsaacPassiveItem {
    public WoodenSpoonItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Wooden Spoon: Mediocre "Speed Up" (+0.08 shot speed, no damage or fire rate)
        builder.addShotSpeed(0.08f);
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.wooden_spoon").formatted(Formatting.GOLD));
        tooltip.add(Text.literal("Speed Up (Minor projectile speed boost)").formatted(Formatting.GRAY));
    }
}
