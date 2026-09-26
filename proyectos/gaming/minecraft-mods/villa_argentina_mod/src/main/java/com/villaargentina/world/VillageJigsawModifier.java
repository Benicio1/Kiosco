package com.villaargentina.world;

import com.mojang.datafixers.util.Pair;
import com.villaargentina.VillaArgentinaMod;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class VillageJigsawModifier {
    private static final ResourceLocation EMPTY_PROCESSOR = new ResourceLocation("minecraft", "empty");

    public static void registerJigsaws(MinecraftServer server) {
        Registry<StructureTemplatePool> poolRegistry = server.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processorRegistry = server.registryAccess().registryOrThrow(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> emptyHolder = processorRegistry.getHolderOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, EMPTY_PROCESSOR));

        // Transformar completamente las aldeas en villas argentinas
        String[] villages = new String[] {
                "plains",
                "savanna",
                "desert",
                "taiga",
                "snowy"
        };

        for (String village : villages) {
            ResourceLocation housesPool = new ResourceLocation("minecraft", "village/" + village + "/houses");
            replacePoolWithVilla(poolRegistry, housesPool, emptyHolder);
        }
        VillaArgentinaMod.LOGGER.info("¡Aldeas transformadas 100% en Villa Argentina!");
    }

    @SuppressWarnings("unchecked")
    private static void replacePoolWithVilla(Registry<StructureTemplatePool> poolRegistry, ResourceLocation poolRL, Holder<StructureProcessorList> processors) {
        StructureTemplatePool pool = poolRegistry.get(poolRL);
        if (pool == null) {
            return;
        }

        try {
            Field rawTemplatesField = null;
            Field templatesField = null;

            for (Field f : StructureTemplatePool.class.getDeclaredFields()) {
                f.setAccessible(true);
                if (List.class.isAssignableFrom(f.getType())) {
                    Object val = f.get(pool);
                    if (val instanceof List<?> list && !list.isEmpty()) {
                        Object first = list.get(0);
                        if (first instanceof Pair) {
                            rawTemplatesField = f;
                        } else if (first instanceof StructurePoolElement) {
                            templatesField = f;
                        }
                    }
                    if (f.getName().equals("rawTemplates")) {
                        rawTemplatesField = f;
                    }
                    if (f.getName().equals("templates")) {
                        templatesField = f;
                    }
                }
            }

            if (rawTemplatesField != null && templatesField != null) {
                // Reemplazamos la lista completa para que la aldea sea 100% Villa Argentina
                List<Pair<StructurePoolElement, Integer>> newRaw = new ArrayList<>();
                Object templatesObj = templatesField.get(pool);
                List<StructurePoolElement> newTemplates = (templatesObj instanceof ObjectArrayList) ? new ObjectArrayList<>() : new ArrayList<>();

                String[][] villaBuildings = new String[][] {
                        {"villaargentina:villa_casilla_chica", "8"},
                        {"villaargentina:villa_casilla_1", "6"},
                        {"villaargentina:villa_casilla_2", "6"},
                        {"villaargentina:villa_casilla_3", "5"},
                        {"villaargentina:villa_kiosco", "4"},
                        {"villaargentina:villa_chori", "5"},
                        {"villaargentina:villa_potrero", "3"},
                        {"villaargentina:villa_feria", "4"}
                };

                for (String[] entry : villaBuildings) {
                    String templateRL = entry[0];
                    int weight = Integer.parseInt(entry[1]);
                    StructurePoolElement piece = StructurePoolElement.legacy(templateRL, processors).apply(StructureTemplatePool.Projection.RIGID);
                    newRaw.add(Pair.of(piece, weight));
                    for (int i = 0; i < weight; i++) {
                        newTemplates.add(piece);
                    }
                }

                rawTemplatesField.set(pool, newRaw);
                templatesField.set(pool, newTemplates);
                VillaArgentinaMod.LOGGER.info("Pool {} reemplazado exitosamente con 100% casillas de villa.", poolRL);
            }
        } catch (Exception e) {
            VillaArgentinaMod.LOGGER.error("Error al reemplazar pool {}", poolRL, e);
        }
    }
}
