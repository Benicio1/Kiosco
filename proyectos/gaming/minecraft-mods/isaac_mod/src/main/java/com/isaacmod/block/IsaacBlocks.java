package com.isaacmod.block;

import com.isaacmod.IsaacMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class IsaacBlocks {
    public static final Block DOOR_BARRIER = registerBlock("door_barrier",
            new DoorBarrierBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK)
                    .strength(-1.0f, 3600000.0f)
                    .dropsNothing()
                    .nonOpaque()));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(IsaacMod.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(IsaacMod.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        IsaacMod.LOGGER.info("Registering Isaac Mod Blocks");
    }
}
