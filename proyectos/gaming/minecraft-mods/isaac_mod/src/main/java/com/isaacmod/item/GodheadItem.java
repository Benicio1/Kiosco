package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GodheadItem extends IsaacPassiveItem {
    public GodheadItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Godhead: God tears with radiant halo aura damaging nearby enemies, homing, +0.5 damage
        builder.addDamage(0.5f);
        builder.setHoming(true);
        builder.setGodheadAura(true);
        builder.multiplyFireRate(1.2f);
    }

    @Override
    public int getPriority() {
        return 120;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.godhead").formatted(Formatting.YELLOW, Formatting.BOLD));
        tooltip.add(Text.literal("God Tears! Damaging radiant halo encircles all flying tears").formatted(Formatting.GRAY));
    }
}
