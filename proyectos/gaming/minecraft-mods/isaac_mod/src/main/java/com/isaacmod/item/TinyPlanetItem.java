package com.isaacmod.item;

import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TinyPlanetItem extends IsaacPassiveItem {
    public TinyPlanetItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Tiny Planet: Orbiting tears / massive erratic spread and lower speed
        builder.setSpreadAngle(35.0f);
        builder.multiplyShotSpeed(0.6f);
        builder.addRange(25);
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.tiny_planet").formatted(Formatting.BLUE, Formatting.BOLD));
        tooltip.add(Text.literal("Orbiting Tears (Wide erratic spread; hard to aim!)").formatted(Formatting.GRAY));
    }
}
