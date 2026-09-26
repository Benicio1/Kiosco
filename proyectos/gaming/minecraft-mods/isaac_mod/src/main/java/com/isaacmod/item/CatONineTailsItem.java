package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CatONineTailsItem extends IsaacPassiveItem {
    public CatONineTailsItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Cat O' Nine Tails: +1.5 flat damage, +0.3 shot speed
        builder.addDamage(1.5f);
        builder.addShotSpeed(0.3f);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.cat_o_nine_tails").formatted(Formatting.DARK_RED, Formatting.BOLD));
        tooltip.add(Text.literal("Shot Speed & Damage Up (+1.5 DMG, +0.3 Shot Speed)").formatted(Formatting.GRAY));
    }
}
