package com.isaacmod;

import com.isaacmod.block.IsaacBlocks;
import com.isaacmod.command.IsaacCommand;
import com.isaacmod.dungeon.DungeonRoomManager;
import com.isaacmod.dungeon.IsaacRunManager;
import com.isaacmod.entity.IsaacEntities;
import com.isaacmod.item.IsaacItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsaacMod implements ModInitializer {
    public static final String MOD_ID = "isaac";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("The Binding of Isaac Roguelike mod initializing!");

        IsaacBlocks.registerModBlocks();
        IsaacItems.registerModItems();
        IsaacEntities.registerModEntities();
        DungeonRoomManager.initialize();
        com.isaacmod.stats.HolyMantleHandler.initialize();
        IsaacCommand.register();

        // Permadeath Roguelike Loop:
        // When a player dies and respawns during an active run, automatically restart in a brand new floor layout!
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (IsaacRunManager.isRunActive()) {
                IsaacRunManager.startNewRun(newPlayer);
            }
        });

        LOGGER.info("The Binding of Isaac Roguelike mod initialized successfully!");
    }
}
