package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MagicMushroomItem extends IsaacPassiveItem {
    public MagicMushroomItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Magic Mushroom: All Stats Up! +0.3 flat damage, x1.5 damage multiplier, +range, +speed
        builder.addDamage(0.3f);
        builder.multiplyDamage(1.5f);
        builder.addRange(10);
        builder.multiplyShotSpeed(1.15f);
    }

    @Override
    public int getPriority() {
        return 150;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.magic_mushroom").formatted(Formatting.RED, Formatting.BOLD));
        tooltip.add(Text.literal("All Stats Up! (+0.3 Flat, x1.5 Damage Multiplier, +Range, +Speed)").formatted(Formatting.GRAY));
    }
}
