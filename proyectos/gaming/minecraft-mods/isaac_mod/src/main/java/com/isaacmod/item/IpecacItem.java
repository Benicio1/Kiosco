package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IpecacItem extends IsaacPassiveItem {
    public IpecacItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Ipecac: Explosive lobbed tears + massive damage boost, but very slow fire rate
        builder.setExplosive(true);
        builder.setLobbed(true);
        builder.addDamage(8.0f);
        builder.multiplyFireRate(2.0f);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.ipecac").formatted(Formatting.GREEN, Formatting.BOLD));
        tooltip.add(Text.literal("Explosive Shots (Lobbed toxic blast, high damage)").formatted(Formatting.GRAY));
    }
}
