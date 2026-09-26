package com.isaacmod.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class IsaacActiveItem extends Item {
    private final int maxCharges;

    public IsaacActiveItem(Settings settings, int maxCharges) {
        super(settings.maxCount(1));
        this.maxCharges = maxCharges;
    }

    public int getMaxCharges() {
        return maxCharges;
    }

    public static int getCharges(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("IsaacCharges")) {
            return nbt.getInt("IsaacCharges");
        }
        return 0;
    }

    public static void setCharges(ItemStack stack, int charges) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt("IsaacCharges", charges);
    }

    public void addCharge(ItemStack stack, ServerPlayerEntity player) {
        int current = getCharges(stack);
        if (current < maxCharges) {
            int updated = current + 1;
            setCharges(stack, updated);
            if (updated == maxCharges) {
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 1.2f, 1.8f);
                player.sendMessage(Text.literal("§6§l[" + this.getName().getString() + "] FULLY CHARGED! (Ready to use)"), true);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        int currentCharges = getCharges(stack);
        if (currentCharges < maxCharges && !user.getAbilities().creativeMode) {
            if (!world.isClient()) {
                user.sendMessage(Text.literal("§cItem not fully charged! (" + currentCharges + "/" + maxCharges + " rooms cleared)"), true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.7f, 1.2f);
            }
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient()) {
            boolean success = onActivate(world, user, stack);
            if (success) {
                if (!user.getAbilities().creativeMode) {
                    setCharges(stack, 0);
                }
                user.getItemCooldownManager().set(this, 20); // 1-second cooldown prevention
                return TypedActionResult.success(stack, false);
            } else {
                return TypedActionResult.fail(stack);
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    /**
     * Executes the active item's special effect.
     * @return true if the activation succeeded and charges should be consumed.
     */
    protected abstract boolean onActivate(World world, PlayerEntity user, ItemStack stack);

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int charges = getCharges(stack);
        if (charges >= maxCharges) {
            tooltip.add(Text.literal("Charge: " + charges + "/" + maxCharges + " [READY]").formatted(Formatting.GREEN, Formatting.BOLD));
        } else {
            tooltip.add(Text.literal("Charge: " + charges + "/" + maxCharges + " (Defeat rooms to recharge)").formatted(Formatting.YELLOW));
        }
    }
}
