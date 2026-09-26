package com.isaacmod.item;

import com.isaacmod.entity.TearEntity;
import com.isaacmod.stats.PlayerStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IsaacTearItem extends Item {
    public IsaacTearItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(itemStack);
        }

        PlayerStats stats = PlayerStats.compute(user);

        if (stats.hasBrimstoneLaser()) {
            com.isaacmod.entity.BrimstoneBeam.fire(world, user, stats.getDamage());
            user.getItemCooldownManager().set(this, Math.max(16, Math.round(stats.getFireRate() * 1.5f)));
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(itemStack, world.isClient());
        }

        if (!world.isClient()) {
            int count = stats.getTearCount();
            if (stats.hasTwentyTwenty()) {
                count = Math.max(2, count * 2);
            }
            float spread = Math.max(stats.getSpreadAngle(), stats.hasTwentyTwenty() ? 5.0f : 0.0f);

            for (int i = 0; i < count; i++) {
                TearEntity tear = new TearEntity(world, user);
                tear.setDamage(stats.getDamage());
                tear.setRangeTicks(stats.getRange());
                tear.setExplosive(stats.isExplosive());
                tear.setLobbed(stats.isLobbed());
                tear.setHoming(stats.isHoming());
                tear.setPiercing(stats.isPiercing());
                tear.setGiant(stats.isGiant());
                tear.setGodheadAura(stats.hasGodheadAura());
                tear.setRubberCement(stats.hasRubberCement());

                float yawOffset = 0.0f;
                if (count > 1) {
                    yawOffset = -spread + (2.0f * spread / (count - 1)) * i;
                }

                tear.setVelocity(user, user.getPitch(), user.getYaw() + yawOffset, 0.0f, stats.getShotSpeed(), 1.0f);
                world.spawnEntity(tear);
            }
        }

        user.getItemCooldownManager().set(this, stats.getFireRate());
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS,
                0.7f, 0.5f / (world.getRandom().nextFloat() * 0.4f + 0.8f));

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.isaac.isaac_tear").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("Primary Tear Weapon").formatted(Formatting.DARK_PURPLE));
    }
}
