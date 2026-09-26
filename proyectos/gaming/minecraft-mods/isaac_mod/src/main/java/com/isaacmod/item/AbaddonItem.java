package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AbaddonItem extends IsaacPassiveItem {
    public AbaddonItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Abaddon: +2.5 damage, +shot speed, piercing tears
        builder.addDamage(2.5f);
        builder.addShotSpeed(0.2f);
        builder.setPiercing(true);
    }

    @Override
    public int getPriority() {
        return 110;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.abaddon").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("Demonic Pact! Evil damage boost (+2.5 DMG, +Piercing)").formatted(Formatting.GRAY));
    }
}
