package com.villaargentina.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "villaargentina", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SuffocationProtector {

    @SubscribeEvent
    public static void onVillagerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        // Si el aldeano está sufriendo asfixia por bloques (paredes / techos de la generación)
        if (event.getSource().is(DamageTypes.IN_WALL)) {
            Level level = villager.level();
            BlockPos currentPos = villager.blockPosition();

            // Buscar un bloque de aire seguro adyacente o arriba
            BlockPos safePos = null;
            for (int dy = 0; dy <= 2; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos check = currentPos.offset(dx, dy, dz);
                        if (level.isEmptyBlock(check) && level.isEmptyBlock(check.above())) {
                            safePos = check;
                            break;
                        }
                    }
                    if (safePos != null) break;
                }
                if (safePos != null) break;
            }

            if (safePos != null) {
                villager.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
                event.setCanceled(true);
            }
        }
    }
}
