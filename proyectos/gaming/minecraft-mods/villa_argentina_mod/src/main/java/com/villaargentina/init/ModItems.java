package com.villaargentina.init;

import com.villaargentina.VillaArgentinaMod;
import com.villaargentina.item.FernetItem;
import com.villaargentina.item.MateItem;
import com.villaargentina.item.TermoItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, VillaArgentinaMod.MODID);

    public static final RegistryObject<Item> MATE = ITEMS.register("mate",
            () -> new MateItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> TERMO = ITEMS.register("termo",
            () -> new TermoItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CHORIPAN = ITEMS.register("choripan",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8)
                    .saturationMod(0.8F)
                    .meat()
                    .build())));

    public static final RegistryObject<Item> BOTELLA_FERNET = ITEMS.register("botella_fernet",
            () -> new FernetItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> DISCO_CUMBIA = ITEMS.register("disco_cumbia",
            () -> new RecordItem(6, ModSounds.CUMBIA_VILLERA, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 3610));

    public static final RegistryObject<Item> DISCO_TAMO_CHELO = ITEMS.register("disco_tamo_chelo",
            () -> new RecordItem(7, ModSounds.TAMO_CHELO, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 2200));

    public static final RegistryObject<Item> DISCO_LGANTE_RKT = ITEMS.register("disco_lgante_rkt",
            () -> new RecordItem(8, ModSounds.LGANTE_RKT, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 3700));

    public static final RegistryObject<Item> DISCO_PERRITO_MALVADO = ITEMS.register("disco_perrito_malvado",
            () -> new RecordItem(9, ModSounds.PERRITO_MALVADO, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 5600));
}
