package com.isaacmod.entity;

import com.isaacmod.IsaacMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class IsaacEntities {
    public static final EntityType<TearEntity> TEAR = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(IsaacMod.MOD_ID, "tear"),
            FabricEntityTypeBuilder.<TearEntity>create(SpawnGroup.MISC, TearEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static void registerModEntities() {
        IsaacMod.LOGGER.info("Registering Isaac Mod Entities");
    }
}
