package com.villaargentina.init;

import com.villaargentina.VillaArgentinaMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, VillaArgentinaMod.MODID);

    public static final RegistryObject<Block> LADRILLO_HUECO = registerBlock("ladrillo_hueco",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .strength(1.8F, 4.0F)
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> CHAPA_ZINC = registerBlock("chapa_zinc",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Block> CHAPA_OXIDADA = registerBlock("chapa_oxidada",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(1.8F, 5.0F)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Block> TANQUE_AGUA = registerBlock("tanque_agua",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(1.5F, 3.0F)
                    .sound(SoundType.COPPER)));

    public static final RegistryObject<Block> PARRILLA_TAMBOR = registerBlock("parrilla_tambor",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.5F, 6.0F)
                    .sound(SoundType.LANTERN)
                    .lightLevel(state -> 13)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
