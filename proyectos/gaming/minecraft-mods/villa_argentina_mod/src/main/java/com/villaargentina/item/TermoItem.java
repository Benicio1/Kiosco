package com.villaargentina.item;

import com.villaargentina.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.List;

public class TermoItem extends Item {
    public static final int MAX_CHARGES = 8;

    public TermoItem(Properties properties) {
        super(properties);
    }

    public static int getCharges(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains("AguaCaliente")) ? tag.getInt("AguaCaliente") : 0;
    }

    public static void setCharges(ItemStack stack, int charges) {
        stack.getOrCreateTag().putInt("AguaCaliente", Math.max(0, Math.min(charges, MAX_CHARGES)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.WATER_CAULDRON) || state.getFluidState().is(Fluids.WATER) || state.is(Blocks.WATER)) {
            setCharges(stack, MAX_CHARGES);
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.villaargentina.termo_lleno").withStyle(ChatFormatting.AQUA), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionHand otherHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        // Raycast al agua para recargar
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hit.getBlockPos();
            if (level.getFluidState(hitPos).is(Fluids.WATER)) {
                setCharges(stack, MAX_CHARGES);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.villaargentina.termo_lleno").withStyle(ChatFormatting.AQUA), true);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }

        int charges = getCharges(stack);

        // Si tenemos el mate en la otra mano, cebamos el mate!
        if (otherStack.is(ModItems.MATE.get())) {
            if (charges > 0) {
                setCharges(stack, charges - 1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.3F);
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.villaargentina.mate_cebado", (charges - 1)).withStyle(ChatFormatting.GREEN), true);
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
                    player.setTicksFrozen(0);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            } else {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.villaargentina.termo_vacio").withStyle(ChatFormatting.RED), true);
                }
                return InteractionResultHolder.fail(stack);
            }
        }

        if (charges > 0) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        } else {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.villaargentina.termo_vacio").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        int charges = getCharges(stack);
        if (charges > 0) {
            setCharges(stack, charges - 1);
            if (!level.isClientSide) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
                entity.setTicksFrozen(0);
                entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.SATURATION, 100, 0));
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int charges = getCharges(stack);
        if (charges > 0) {
            tooltip.add(Component.translatable("tooltip.villaargentina.termo_cargas", charges, MAX_CHARGES).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.villaargentina.termo_vacio_info").withStyle(ChatFormatting.GRAY));
        }
    }
}
