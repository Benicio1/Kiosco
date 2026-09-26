package com.escuela.init;

import com.escuela.EscuelaMod;
import com.escuela.item.GeneradorEscuelaItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EscuelaMod.MODID);

    public static final RegistryObject<Item> GENERADOR_ESCUELA = ITEMS.register("generador_escuela",
            GeneradorEscuelaItem::new);
}
