package com.villaargentina.init;

import com.google.common.collect.ImmutableSet;
import com.villaargentina.VillaArgentinaMod;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, VillaArgentinaMod.MODID);

    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, VillaArgentinaMod.MODID);

    public static final RegistryObject<PoiType> CHORIPANERO_POI = POI_TYPES.register("choripanero_poi",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.PARRILLA_TAMBOR.get().getStateDefinition().getPossibleStates()),
                    1, 1));

    public static final RegistryObject<VillagerProfession> CHORIPANERO = VILLAGER_PROFESSIONS.register("choripanero",
            () -> new VillagerProfession(
                    "choripanero",
                    holder -> holder.get() == CHORIPANERO_POI.get(),
                    holder -> holder.get() == CHORIPANERO_POI.get(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_BUTCHER
            ));
}
