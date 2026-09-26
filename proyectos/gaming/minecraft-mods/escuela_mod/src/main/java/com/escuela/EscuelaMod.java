package com.escuela;

import com.mojang.logging.LogUtils;
import com.escuela.init.ModBlocks;
import com.escuela.init.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(EscuelaMod.MODID)
public class EscuelaMod {
    public static final String MODID = "escuela";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> ESCUELA_TAB = CREATIVE_MODE_TABS.register("escuela_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.escuela.escuela_tab"))
                    .icon(() -> new ItemStack(ModBlocks.PIZARRON.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.GENERADOR_ESCUELA.get());
                        output.accept(ModBlocks.PIZARRON.get());
                        output.accept(ModBlocks.PUPITRE.get());
                        output.accept(ModBlocks.BALDOSA_ESCOLAR.get());
                        output.accept(ModBlocks.PARED_ESCOLAR.get());
                        output.accept(ModBlocks.LINEA_CANCHA.get());
                    }).build());

    public EscuelaMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        LOGGER.info("Escuela Mod inicializado correctamente!");
    }
}
