package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TheSadOnionItem extends IsaacPassiveItem {
    public TheSadOnionItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // The Sad Onion: Major fire rate boost (reduces cooldown)
        builder.addFireRate(-3.0f);
        builder.multiplyFireRate(0.65f);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.the_sad_onion").formatted(Formatting.AQUA, Formatting.BOLD));
        tooltip.add(Text.literal("Tears Up! (Fast firing rate)").formatted(Formatting.GRAY));
    }
}
