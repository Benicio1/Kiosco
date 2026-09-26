package com.villaargentina.init;

import com.villaargentina.VillaArgentinaMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, VillaArgentinaMod.MODID);

    public static final RegistryObject<SoundEvent> CUMBIA_VILLERA = SOUND_EVENTS.register("cumbia_villera",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(VillaArgentinaMod.MODID, "cumbia_villera")));

    public static final RegistryObject<SoundEvent> TAMO_CHELO = SOUND_EVENTS.register("tamo_chelo",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(VillaArgentinaMod.MODID, "tamo_chelo")));

    public static final RegistryObject<SoundEvent> LGANTE_RKT = SOUND_EVENTS.register("lgante_rkt",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(VillaArgentinaMod.MODID, "lgante_rkt")));

    public static final RegistryObject<SoundEvent> PERRITO_MALVADO = SOUND_EVENTS.register("perrito_malvado",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(VillaArgentinaMod.MODID, "perrito_malvado")));

    public static final RegistryObject<SoundEvent> SORBO_MATE = SOUND_EVENTS.register("sorbo_mate",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(VillaArgentinaMod.MODID, "sorbo_mate")));
}
