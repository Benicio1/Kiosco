package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TheHaloItem extends IsaacPassiveItem {
    public TheHaloItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // The Halo: +1.0 damage, +range, +speed, slightly faster fire rate
        builder.addDamage(1.0f);
        builder.addRange(8);
        builder.addShotSpeed(0.15f);
        builder.multiplyFireRate(0.9f);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.the_halo").formatted(Formatting.YELLOW, Formatting.BOLD));
        tooltip.add(Text.literal("All Stats Up (+1.0 DMG, +Speed, +Health, +Range)").formatted(Formatting.GRAY));
    }
}
