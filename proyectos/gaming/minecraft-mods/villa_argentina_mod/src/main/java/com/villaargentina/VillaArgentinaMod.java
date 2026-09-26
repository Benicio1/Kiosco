package com.villaargentina;

import com.mojang.logging.LogUtils;
import com.villaargentina.init.ModBlocks;
import com.villaargentina.init.ModItems;
import com.villaargentina.init.ModSounds;
import com.villaargentina.init.ModVillagers;
import com.villaargentina.world.VillageJigsawModifier;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.List;

@Mod(VillaArgentinaMod.MODID)
public class VillaArgentinaMod {
    public static final String MODID = "villaargentina";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> VILLA_TAB = CREATIVE_MODE_TABS.register("villa_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.villaargentina.villa_tab"))
                    .icon(() -> new ItemStack(ModItems.MATE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.LADRILLO_HUECO.get());
                        output.accept(ModBlocks.CHAPA_ZINC.get());
                        output.accept(ModBlocks.CHAPA_OXIDADA.get());
                        output.accept(ModBlocks.TANQUE_AGUA.get());
                        output.accept(ModBlocks.PARRILLA_TAMBOR.get());
                        output.accept(ModItems.CHORIPAN.get());
                        output.accept(ModItems.MATE.get());
                        output.accept(ModItems.TERMO.get());
                        output.accept(ModItems.BOTELLA_FERNET.get());
                        output.accept(ModItems.DISCO_CUMBIA.get());
                        output.accept(ModItems.DISCO_TAMO_CHELO.get());
                        output.accept(ModItems.DISCO_LGANTE_RKT.get());
                        output.accept(ModItems.DISCO_PERRITO_MALVADO.get());
                    }).build());

    public VillaArgentinaMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModVillagers.POI_TYPES.register(modEventBus);
        ModVillagers.VILLAGER_PROFESSIONS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::onVillagerTrades);
    }

    private void onServerAboutToStart(ServerAboutToStartEvent event) {
        VillageJigsawModifier.registerJigsaws(event.getServer());
    }

    private void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == ModVillagers.CHORIPANERO.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            // Nivel 1: Materia prima por esmeraldas y choripán al paso
            trades.get(1).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.PORKCHOP, 4),
                    new ItemStack(Items.EMERALD, 1),
                    16, 2, 0.05F
            ));
            trades.get(1).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(ModItems.CHORIPAN.get(), 2),
                    16, 2, 0.05F
            ));

            // Nivel 2: Carbón y Choripanes extra
            trades.get(2).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.COAL, 8),
                    new ItemStack(Items.EMERALD, 1),
                    12, 5, 0.05F
            ));
            trades.get(2).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(Items.BREAD, 2),
                    new ItemStack(ModItems.CHORIPAN.get(), 3),
                    12, 5, 0.05F
            ));

            // Nivel 3: Mate y Termo
            trades.get(3).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 2),
                    new ItemStack(ModItems.MATE.get(), 1),
                    8, 10, 0.05F
            ));
            trades.get(3).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 4),
                    new ItemStack(ModItems.TERMO.get(), 1),
                    6, 10, 0.05F
            ));

            // Nivel 4: Botella de Fernet
            trades.get(4).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 3),
                    new ItemStack(ModItems.BOTELLA_FERNET.get(), 1),
                    8, 15, 0.05F
            ));

            // Nivel 5: Colección de Discos de Cumbia y RKT
            trades.get(5).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 6),
                    new ItemStack(ModItems.DISCO_CUMBIA.get(), 1),
                    4, 20, 0.05F
            ));
            trades.get(5).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 6),
                    new ItemStack(ModItems.DISCO_TAMO_CHELO.get(), 1),
                    4, 20, 0.05F
            ));
            trades.get(5).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 6),
                    new ItemStack(ModItems.DISCO_LGANTE_RKT.get(), 1),
                    4, 20, 0.05F
            ));
            trades.get(5).add((trader, rand) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 6),
                    new ItemStack(ModItems.DISCO_PERRITO_MALVADO.get(), 1),
                    4, 20, 0.05F
            ));
        }
    }
}
