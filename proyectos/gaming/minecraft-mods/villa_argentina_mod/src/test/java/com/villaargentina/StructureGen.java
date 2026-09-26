package com.villaargentina;

import net.minecraft.nbt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class StructureGen {
    public static void main(String[] args) throws Exception {
        String base = args.length > 0 ? args[0] : "src/main/resources/data/villaargentina/structures";
        new File(base).mkdirs();

        genCasilla1(base + "/villa_casilla_1.nbt");
        genCasilla2(base + "/villa_casilla_2.nbt");
        genCasilla3(base + "/villa_casilla_3.nbt");
        genCasillaChica(base + "/villa_casilla_chica.nbt");
        genKiosco(base + "/villa_kiosco.nbt");
        genPuestoChori(base + "/villa_chori.nbt");
        genPotrero(base + "/villa_potrero.nbt");
        genFeria(base + "/villa_feria.nbt");

        System.out.println("All 8 structure NBT files generated successfully!");
    }

    private static class Builder {
        int sx, sy, sz;
        List<CompoundTag> palette = new ArrayList<>();
        Map<String, Integer> paletteMap = new HashMap<>();
        List<CompoundTag> blocks = new ArrayList<>();

        Builder(int x, int y, int z) {
            this.sx = x;
            this.sy = y;
            this.sz = z;
        }

        int state(String blockName) {
            return state(blockName, null);
        }

        int state(String blockName, Map<String, String> props) {
            String key = blockName + (props != null ? props.toString() : "");
            if (paletteMap.containsKey(key)) return paletteMap.get(key);

            CompoundTag tag = new CompoundTag();
            tag.putString("Name", blockName);
            if (props != null && !props.isEmpty()) {
                CompoundTag ptag = new CompoundTag();
                for (Map.Entry<String, String> e : props.entrySet()) {
                    ptag.putString(e.getKey(), e.getValue());
                }
                tag.put("Properties", ptag);
            }
            int idx = palette.size();
            palette.add(tag);
            paletteMap.put(key, idx);
            return idx;
        }

        void set(int x, int y, int z, int st) {
            set(x, y, z, st, null);
        }

        void set(int x, int y, int z, int st, CompoundTag be) {
            CompoundTag b = new CompoundTag();
            ListTag pos = new ListTag();
            pos.add(IntTag.valueOf(x));
            pos.add(IntTag.valueOf(y));
            pos.add(IntTag.valueOf(z));
            b.put("pos", pos);
            b.putInt("state", st);
            if (be != null) {
                b.put("nbt", be);
            }
            blocks.add(b);
        }

        void setEntranceJigsaw(int x, int y, int z) {
            Map<String, String> props = Map.of("orientation", "north_up");
            int st = state("minecraft:jigsaw", props);

            CompoundTag nbt = new CompoundTag();
            nbt.putString("id", "minecraft:jigsaw");
            nbt.putString("name", "minecraft:building_entrance");
            nbt.putString("target", "minecraft:building_entrance");
            nbt.putString("pool", "minecraft:village/plains/streets");
            nbt.putString("joint", "aligned");
            nbt.putString("final_state", "minecraft:air");

            set(x, y, z, st, nbt);
        }

        void setVillagerSpawn(int x, int y, int z) {
            Map<String, String> props = Map.of("orientation", "up_north");
            int st = state("minecraft:jigsaw", props);

            CompoundTag nbt = new CompoundTag();
            nbt.putString("id", "minecraft:jigsaw");
            nbt.putString("name", "minecraft:bottom");
            nbt.putString("target", "minecraft:bottom");
            nbt.putString("pool", "minecraft:village/plains/villagers");
            nbt.putString("joint", "rollable");
            nbt.putString("final_state", "minecraft:air");

            set(x, y, z, st, nbt);
        }

        // Cama perfectamente alineada con BlockEntity oficial para que nunca falle
        void setBed(int footX, int footY, int footZ, String facing, String color) {
            int headX = footX;
            int headZ = footZ;
            if (facing.equals("north")) headZ = footZ - 1;
            else if (facing.equals("south")) headZ = footZ + 1;
            else if (facing.equals("east")) headX = footX + 1;
            else if (facing.equals("west")) headX = footX - 1;

            Map<String, String> footProps = Map.of("facing", facing, "occupied", "false", "part", "foot");
            Map<String, String> headProps = Map.of("facing", facing, "occupied", "false", "part", "head");

            CompoundTag bedNbt = new CompoundTag();
            bedNbt.putString("id", "minecraft:bed");

            set(footX, footY, footZ, state("minecraft:" + color, footProps), bedNbt.copy());
            set(headX, footY, headZ, state("minecraft:" + color, headProps), bedNbt.copy());
        }

        void save(String path) throws Exception {
            CompoundTag root = new CompoundTag();
            root.putInt("DataVersion", 3465);

            ListTag sizeList = new ListTag();
            sizeList.add(IntTag.valueOf(sx));
            sizeList.add(IntTag.valueOf(sy));
            sizeList.add(IntTag.valueOf(sz));
            root.put("size", sizeList);

            ListTag palList = new ListTag();
            for (CompoundTag p : palette) palList.add(p);
            root.put("palette", palList);

            ListTag blkList = new ListTag();
            for (CompoundTag b : blocks) blkList.add(b);
            root.put("blocks", blkList);

            root.put("entities", new ListTag());

            File out = new File(path);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                NbtIo.writeCompressed(root, fos);
            }
        }
    }

    // 1. Casilla de 2 pisos con chapas, ladrillos huecos y tanque de agua azul
    private static void genCasilla1(String path) throws Exception {
        Builder b = new Builder(7, 8, 7);
        int ladrillo = b.state("villaargentina:ladrillo_hueco");
        int chapa = b.state("villaargentina:chapa_zinc");
        int chapaOx = b.state("villaargentina:chapa_oxidada");
        int tanque = b.state("villaargentina:tanque_agua");
        int cobble = b.state("minecraft:cobblestone");
        int wood = b.state("minecraft:oak_planks");
        int glass = b.state("minecraft:glass_pane");
        int doorLower = b.state("minecraft:oak_door", Map.of("half", "lower", "facing", "south"));
        int doorUpper = b.state("minecraft:oak_door", Map.of("half", "upper", "facing", "south"));
        int torch = b.state("minecraft:torch");
        int craft = b.state("minecraft:crafting_table");
        int ladder = b.state("minecraft:ladder", Map.of("facing", "west"));

        b.setEntranceJigsaw(3, 1, 0);
        b.set(3, 0, 0, cobble);

        // Piso de piedra (y=0)
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                b.set(x, 0, z, cobble);
            }
        }

        // Paredes piso 1 (y=1 a 3)
        for (int y = 1; y <= 3; y++) {
            for (int x = 1; x <= 5; x++) {
                for (int z = 1; z <= 5; z++) {
                    if (x == 1 || x == 5 || z == 1 || z == 5) {
                        if (x == 3 && z == 1 && (y == 1 || y == 2)) {
                            b.set(x, y, z, (y == 1) ? doorLower : doorUpper);
                        } else if ((x == 1 || x == 5) && z == 3 && y == 2) {
                            b.set(x, y, z, glass);
                        } else {
                            b.set(x, y, z, ladrillo);
                        }
                    }
                }
            }
        }

        // Interior espacioso
        b.setBed(2, 1, 4, "north", "red_bed"); // Foot en (2,1,4), Head en (2,1,3)
        b.set(4, 1, 4, craft);
        b.set(3, 2, 4, torch);
        b.setVillagerSpawn(3, 1, 2); // 3 bloques libres de altura, sin asfixia

        // Techo de piso 1 (y=4)
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                b.set(x, 4, z, wood);
            }
        }

        // Escalera exterior
        for (int y = 1; y <= 4; y++) b.set(1, y, 2, ladder);

        // Paredes piso 2 (y=5, 6)
        for (int y = 5; y <= 6; y++) {
            for (int x = 2; x <= 5; x++) {
                for (int z = 2; z <= 5; z++) {
                    if (x == 2 || x == 5 || z == 2 || z == 5) {
                        b.set(x, y, z, ((x + y) % 2 == 0) ? chapa : chapaOx);
                    }
                }
            }
        }

        // Techo con tanque
        for (int x = 2; x <= 5; x++) {
            for (int z = 2; z <= 5; z++) {
                b.set(x, 7, z, chapa);
            }
        }
        b.set(2, 7, 2, cobble);
        b.set(5, 7, 5, cobble);
        b.set(4, 7, 3, tanque);

        b.save(path);
    }

    // 2. Casilla angosta de pasillo con revoque sin terminar
    private static void genCasilla2(String path) throws Exception {
        Builder b = new Builder(7, 7, 7);
        int ladrillo = b.state("villaargentina:ladrillo_hueco");
        int chapa = b.state("villaargentina:chapa_zinc");
        int chapaOx = b.state("villaargentina:chapa_oxidada");
        int tanque = b.state("villaargentina:tanque_agua");
        int cobble = b.state("minecraft:cobblestone");
        int wood = b.state("minecraft:spruce_planks");
        int doorLower = b.state("minecraft:spruce_door", Map.of("half", "lower", "facing", "south"));
        int doorUpper = b.state("minecraft:spruce_door", Map.of("half", "upper", "facing", "south"));
        int ironBars = b.state("minecraft:iron_bars");
        int lantern = b.state("minecraft:lantern", Map.of("hanging", "true"));

        b.setEntranceJigsaw(3, 1, 0);
        b.set(3, 0, 0, cobble);

        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) b.set(x, 0, z, cobble);

        for (int y = 1; y <= 3; y++) {
            for (int x = 1; x <= 5; x++) {
                for (int z = 1; z <= 5; z++) {
                    if (x == 1 || x == 5 || z == 1 || z == 5) {
                        if (x == 3 && z == 1 && (y == 1 || y == 2)) {
                            b.set(x, y, z, (y == 1) ? doorLower : doorUpper);
                        } else if (x == 4 && z == 5 && y == 2) {
                            b.set(x, y, z, ironBars);
                        } else {
                            b.set(x, y, z, (y == 3) ? chapaOx : ladrillo);
                        }
                    }
                }
            }
        }

        b.setBed(2, 1, 4, "north", "yellow_bed");
        b.setVillagerSpawn(4, 1, 2);
        b.set(3, 3, 3, lantern);

        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) b.set(x, 4, z, wood);

        for (int y = 5; y <= 6; y++) {
            for (int x = 1; x <= 5; x++) {
                for (int z = 1; z <= 5; z++) {
                    if (x == 1 || x == 5 || z == 1 || z == 5) {
                        b.set(x, y, z, (x % 2 == 0) ? chapa : chapaOx);
                    }
                }
            }
        }

        b.set(3, 6, 3, tanque);

        b.save(path);
    }

    // 3. Casilla de 3 pisos precaria con andamios
    private static void genCasilla3(String path) throws Exception {
        Builder b = new Builder(7, 12, 7);
        int ladrillo = b.state("villaargentina:ladrillo_hueco");
        int chapa = b.state("villaargentina:chapa_zinc");
        int chapaOx = b.state("villaargentina:chapa_oxidada");
        int tanque = b.state("villaargentina:tanque_agua");
        int cobble = b.state("minecraft:cobblestone");
        int wood = b.state("minecraft:oak_planks");
        int scaffolding = b.state("minecraft:scaffolding");
        int ironBars = b.state("minecraft:iron_bars");
        int doorLower = b.state("minecraft:oak_door", Map.of("half", "lower", "facing", "south"));
        int doorUpper = b.state("minecraft:oak_door", Map.of("half", "upper", "facing", "south"));
        int lantern = b.state("minecraft:lantern", Map.of("hanging", "true"));
        int lever = b.state("minecraft:lever", Map.of("face", "floor", "facing", "north"));

        b.setEntranceJigsaw(3, 1, 0);
        b.set(3, 0, 0, cobble);

        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) b.set(x, 0, z, cobble);

        for (int y = 1; y <= 3; y++) {
            for (int x = 1; x <= 5; x++) {
                for (int z = 1; z <= 5; z++) {
                    if (x == 1 || x == 5 || z == 1 || z == 5) {
                        if (x == 3 && z == 1 && (y == 1 || y == 2)) {
                            b.set(x, y, z, (y == 1) ? doorLower : doorUpper);
                        } else {
                            b.set(x, y, z, ladrillo);
                        }
                    }
                }
            }
        }

        b.setBed(2, 1, 4, "north", "blue_bed");
        b.setVillagerSpawn(4, 1, 2);
        b.set(3, 3, 3, lantern);

        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) b.set(x, 4, z, wood);

        for (int y = 1; y <= 7; y++) b.set(5, y, 0, scaffolding);

        for (int y = 5; y <= 7; y++) {
            for (int x = 1; x <= 4; x++) {
                for (int z = 1; z <= 4; z++) {
                    if (x == 1 || x == 4 || z == 1 || z == 4) {
                        b.set(x, y, z, (y == 6 && x == 2 && z == 1) ? ironBars : chapa);
                    }
                }
            }
        }
        for (int x = 1; x <= 4; x++) for (int z = 1; z <= 4; z++) b.set(x, 7, z, wood);

        for (int y = 8; y <= 9; y++) {
            for (int x = 2; x <= 4; x++) {
                for (int z = 2; z <= 4; z++) {
                    if (x == 2 || x == 4 || z == 2 || z == 4) {
                        b.set(x, y, z, chapaOx);
                    }
                }
            }
        }

        for (int x = 2; x <= 4; x++) for (int z = 2; z <= 4; z++) b.set(x, 10, z, chapa);
        b.set(3, 10, 3, tanque);
        b.set(2, 10, 2, ironBars);
        b.set(2, 11, 2, lever);

        b.save(path);
    }

    // 4. Casilla chica espaciosa (6x6) con altura de 3 bloques libres para evitar asfixia
    private static void genCasillaChica(String path) throws Exception {
        Builder b = new Builder(6, 6, 6);
        int ladrillo = b.state("villaargentina:ladrillo_hueco");
        int chapa = b.state("villaargentina:chapa_zinc");
        int chapaOx = b.state("villaargentina:chapa_oxidada");
        int tanque = b.state("villaargentina:tanque_agua");
        int cobble = b.state("minecraft:cobblestone");
        int doorLower = b.state("minecraft:oak_door", Map.of("half", "lower", "facing", "south"));
        int doorUpper = b.state("minecraft:oak_door", Map.of("half", "upper", "facing", "south"));
        int torch = b.state("minecraft:torch");

        b.setEntranceJigsaw(3, 1, 0);
        b.set(3, 0, 0, cobble);

        for (int x = 1; x <= 4; x++) for (int z = 1; z <= 4; z++) b.set(x, 0, z, cobble);

        for (int y = 1; y <= 3; y++) {
            for (int x = 1; x <= 4; x++) {
                for (int z = 1; z <= 4; z++) {
                    if (x == 1 || x == 4 || z == 1 || z == 4) {
                        if (x == 3 && z == 1 && (y == 1 || y == 2)) {
                            b.set(x, y, z, (y == 1) ? doorLower : doorUpper);
                        } else {
                            b.set(x, y, z, (y == 3) ? chapa : ladrillo);
                        }
                    }
                }
            }
        }

        // Cama colocada correctamente con BlockEntity oficial
        b.setBed(2, 1, 3, "north", "white_bed"); // Foot en (2,1,3), Head en (2,1,2)
        b.setVillagerSpawn(3, 1, 2); // 3 bloques libres hasta el techo
        b.set(3, 3, 3, torch);

        for (int x = 1; x <= 4; x++) for (int z = 1; z <= 4; z++) b.set(x, 4, z, chapaOx);
        b.set(2, 5, 2, tanque);

        b.save(path);
    }

    // 5. Kiosco / Almacén "Doña Rosa"
    private static void genKiosco(String path) throws Exception {
        Builder b = new Builder(7, 5, 7);
        int ladrillo = b.state("villaargentina:ladrillo_hueco");
        int chapa = b.state("villaargentina:chapa_zinc");
        int chapaOx = b.state("villaargentina:chapa_oxidada");
        int cobble = b.state("minecraft:cobblestone");
        int ironBars = b.state("minecraft:iron_bars");
        int barrel = b.state("minecraft:barrel", Map.of("facing", "up"));
        int lantern = b.state("minecraft:lantern", Map.of("hanging", "true"));
        int woodSlab = b.state("minecraft:oak_slab", Map.of("type", "bottom"));

        b.setEntranceJigsaw(3, 1, 0);
        b.set(3, 0, 0, cobble);

        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) b.set(x, 0, z, cobble);

        for (int y = 1; y <= 3; y++) {
            for (int x = 1; x <= 5; x++) {
                for (int z = 1; z <= 5; z++) {
                    if (x == 1 || x == 5 || z == 1 || z == 5) {
                        if (z == 1 && (x == 2 || x == 3 || x == 4) && y == 2) {
                            b.set(x, y, z, ironBars);
                        } else if (z == 1 && (x == 2 || x == 3 || x == 4) && y == 1) {
                            b.set(x, y, z, woodSlab);
                        } else {
                            b.set(x, y, z, ladrillo);
                        }
                    }
                }
            }
        }

        b.set(4, 1, 4, barrel);
        b.set(4, 2, 4, barrel);
        b.set(2, 1, 4, barrel);
        b.set(3, 3, 3, lantern);
        b.setVillagerSpawn(3, 1, 3);

        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) b.set(x, 4, z, (x + z) % 2 == 0 ? chapa : chapaOx);

        b.save(path);
    }

    // 6. Puesto de Choripán y Parrilla Callejera
    private static void genPuestoChori(String path) throws Exception {
        Builder b = new Builder(7, 5, 7);
        int chapa = b.state("villaargentina:chapa_zinc");
        int chapaOx = b.state("villaargentina:chapa_oxidada");
        int parrilla = b.state("villaargentina:parrilla_tambor");
        int fence = b.state("minecraft:oak_fence");
        int slab = b.state("minecraft:oak_slab", Map.of("type", "bottom"));
        int dirtPath = b.state("minecraft:dirt_path");
        int lantern = b.state("minecraft:lantern", Map.of("hanging", "true"));
        int barrel = b.state("minecraft:barrel", Map.of("facing", "up"));

        b.setEntranceJigsaw(3, 1, 0);
        b.set(3, 0, 0, dirtPath);

        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) b.set(x, 0, z, dirtPath);

        for (int y = 1; y <= 3; y++) {
            b.set(1, y, 1, fence);
            b.set(5, y, 1, fence);
            b.set(1, y, 5, fence);
            b.set(5, y, 5, fence);
        }

        b.set(3, 1, 3, parrilla);
        b.set(4, 1, 3, slab);
        b.set(2, 1, 3, barrel);

        b.set(2, 1, 1, slab);
        b.set(4, 1, 1, slab);
        b.set(3, 3, 3, lantern);
        b.setVillagerSpawn(3, 1, 2);

        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) b.set(x, 4, z, (x == 3) ? chapaOx : chapa);

        b.save(path);
    }

    // 7. El Potrero (Canchita de fútbol)
    private static void genPotrero(String path) throws Exception {
        Builder b = new Builder(9, 4, 8);
        int coarse = b.state("minecraft:coarse_dirt");
        int pathBlock = b.state("minecraft:dirt_path");
        int fence = b.state("minecraft:oak_fence");
        int web = b.state("minecraft:cobweb");
        int flagBlue = b.state("minecraft:light_blue_banner", Map.of("rotation", "0"));
        int flagWhite = b.state("minecraft:white_banner", Map.of("rotation", "0"));

        b.setEntranceJigsaw(4, 1, 0);
        b.set(4, 0, 0, pathBlock);

        for (int x = 1; x <= 7; x++) {
            for (int z = 1; z <= 6; z++) {
                b.set(x, 0, z, ((x + z) % 2 == 0) ? coarse : pathBlock);
            }
        }

        // Arco izquierdo (x=2)
        b.set(2, 1, 3, fence); b.set(2, 2, 3, fence);
        b.set(2, 2, 4, fence);
        b.set(2, 2, 5, fence); b.set(2, 1, 5, fence);
        b.set(1, 1, 4, web);

        // Arco derecho (x=6)
        b.set(6, 1, 3, fence); b.set(6, 2, 3, fence);
        b.set(6, 2, 4, fence);
        b.set(6, 2, 5, fence); b.set(6, 1, 5, fence);
        b.set(7, 1, 4, web);

        b.set(1, 1, 1, flagBlue);
        b.set(7, 1, 1, flagWhite);
        b.set(1, 1, 6, flagWhite);
        b.set(7, 1, 6, flagBlue);

        b.save(path);
    }

    // 8. Feria callejera
    private static void genFeria(String path) throws Exception {
        Builder b = new Builder(7, 5, 7);
        int dirtPath = b.state("minecraft:dirt_path");
        int fence = b.state("minecraft:oak_fence");
        int woolRed = b.state("minecraft:red_wool");
        int woolWhite = b.state("minecraft:white_wool");
        int woolBlue = b.state("minecraft:light_blue_wool");
        int carpet = b.state("minecraft:yellow_carpet");
        int barrel = b.state("minecraft:barrel", Map.of("facing", "up"));
        int chest = b.state("minecraft:chest", Map.of("facing", "south"));
        int lantern = b.state("minecraft:lantern", Map.of("hanging", "true"));

        b.setEntranceJigsaw(3, 1, 0);
        b.set(3, 0, 0, dirtPath);

        for (int x = 1; x <= 5; x++) for (int z = 1; z <= 5; z++) b.set(x, 0, z, dirtPath);

        for (int y = 1; y <= 3; y++) {
            b.set(1, y, 1, fence);
            b.set(5, y, 1, fence);
            b.set(1, y, 5, fence);
            b.set(5, y, 5, fence);
        }

        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                int w = (x % 3 == 0) ? woolBlue : ((x % 3 == 1) ? woolWhite : woolRed);
                b.set(x, 4, z, w);
            }
        }

        b.set(2, 1, 3, barrel);
        b.set(3, 1, 3, chest);
        b.set(4, 1, 3, barrel);
        b.set(3, 1, 2, carpet);
        b.set(3, 3, 3, lantern);
        b.setVillagerSpawn(3, 1, 3);

        b.save(path);
    }
}
