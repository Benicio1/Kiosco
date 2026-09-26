package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PyromaniacItem extends IsaacPassiveItem {
    public PyromaniacItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Pyromaniac: Complete explosion immunity, explosions heal player!
        builder.setPyromaniac(true);
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.pyromaniac").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("Explosion Immunity! Explosions heal your health instead of harming").formatted(Formatting.GRAY));
    }
}
