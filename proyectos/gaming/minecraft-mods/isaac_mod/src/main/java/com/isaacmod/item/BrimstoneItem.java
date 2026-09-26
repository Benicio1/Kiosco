package com.isaacmod.item;

import com.isaacmod.entity.BrimstoneBeam;
import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrimstoneItem extends IsaacPassiveItem {
    public BrimstoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public void modifyStats(PlayerStats.Builder builder) {
        // Brimstone: Blood Laser Beam! High damage, piercing laser, slight fire rate penalty
        builder.setBrimstoneLaser(true);
        builder.setPiercing(true);
        builder.addDamage(4.5f);
        builder.multiplyDamage(1.5f);
        builder.multiplyFireRate(1.6f);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        PlayerStats stats = PlayerStats.compute(user);
        BrimstoneBeam.fire(world, user, stats.getDamage());

        user.getItemCooldownManager().set(this, Math.max(16, Math.round(stats.getFireRate() * 1.5f)));
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public int getPriority() {
        return 150;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.brimstone").formatted(Formatting.DARK_RED, Formatting.BOLD));
        tooltip.add(Text.literal("Blood Laser Beam (Piercing raycast laser; right-click to unleash)").formatted(Formatting.GRAY));
    }
}
