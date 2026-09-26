package com.escuela.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

public class GeneradorEscuelaItem extends Item {
    public GeneradorEscuelaItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            BlockPos clickedPos = context.getClickedPos();
            BlockPos spawnPos = clickedPos.above();

            StructureTemplateManager templateManager = serverLevel.getStructureManager();
            ResourceLocation structureId = new ResourceLocation("escuela", "escuela");
            Optional<StructureTemplate> templateOpt = templateManager.get(structureId);

            if (templateOpt.isPresent()) {
                StructureTemplate template = templateOpt.get();
                StructurePlaceSettings settings = new StructurePlaceSettings()
                        .setRotation(Rotation.NONE)
                        .setMirror(Mirror.NONE)
                        .setIgnoreEntities(false);

                template.placeInWorld(serverLevel, spawnPos, spawnPos, settings, serverLevel.getRandom(), 2);

                serverLevel.playSound(null, spawnPos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        SoundSource.PLAYERS, 1.0F, 1.0F);

                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.translatable("message.escuela.generada"));
                    if (!context.getPlayer().isCreative()) {
                        context.getItemInHand().shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            } else {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(Component.literal("No se pudo cargar la estructura escuela:escuela"));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
