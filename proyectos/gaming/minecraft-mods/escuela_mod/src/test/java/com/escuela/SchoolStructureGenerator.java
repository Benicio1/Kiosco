package com.escuela;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class SchoolStructureGenerator {

    static class BlockInfo {
        int x, y, z;
        int state;
        CompoundTag nbt;

        BlockInfo(int x, int y, int z, int state, CompoundTag nbt) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.state = state;
            this.nbt = nbt;
        }
    }

    static class Tag {
        byte type;
        String name;

        Tag(byte type, String name) {
            this.type = type;
            this.name = name;
        }

        void write(DataOutputStream out) throws IOException {
            out.writeByte(type);
            if (name != null) {
                out.writeUTF(name);
            }
            writePayload(out);
        }

        void writePayload(DataOutputStream out) throws IOException {}
    }

    static class ByteTag extends Tag {
        byte val;
        ByteTag(String name, byte val) { super((byte)1, name); this.val = val; }
        void writePayload(DataOutputStream out) throws IOException { out.writeByte(val); }
    }

    static class ShortTag extends Tag {
        short val;
        ShortTag(String name, short val) { super((byte)2, name); this.val = val; }
        void writePayload(DataOutputStream out) throws IOException { out.writeShort(val); }
    }

    static class IntTag extends Tag {
        int val;
        IntTag(String name, int val) { super((byte)3, name); this.val = val; }
        void writePayload(DataOutputStream out) throws IOException { out.writeInt(val); }
    }

    static class LongTag extends Tag {
        long val;
        LongTag(String name, long val) { super((byte)4, name); this.val = val; }
        void writePayload(DataOutputStream out) throws IOException { out.writeLong(val); }
    }

    static class FloatTag extends Tag {
        float val;
        FloatTag(String name, float val) { super((byte)5, name); this.val = val; }
        void writePayload(DataOutputStream out) throws IOException { out.writeFloat(val); }
    }

    static class DoubleTag extends Tag {
        double val;
        DoubleTag(String name, double val) { super((byte)6, name); this.val = val; }
        void writePayload(DataOutputStream out) throws IOException { out.writeDouble(val); }
    }

    static class StringTag extends Tag {
        String val;
        StringTag(String name, String val) { super((byte)8, name); this.val = val; }
        void writePayload(DataOutputStream out) throws IOException { out.writeUTF(val); }
    }

    static class ListTag extends Tag {
        byte elemType;
        List<Tag> list = new ArrayList<>();

        ListTag(String name, byte elemType) {
            super((byte)9, name);
            this.elemType = elemType;
        }

        void add(Tag tag) {
            list.add(tag);
        }

        void writePayload(DataOutputStream out) throws IOException {
            out.writeByte(elemType);
            out.writeInt(list.size());
            for (Tag t : list) {
                // List elements do NOT write tag type or name, only payload
                t.writePayload(out);
            }
        }
    }

    static class CompoundTag extends Tag {
        List<Tag> tags = new ArrayList<>();

        CompoundTag(String name) { super((byte)10, name); }

        void put(Tag t) { tags.add(t); }
        void putByte(String k, byte v) { tags.add(new ByteTag(k, v)); }
        void putShort(String k, short v) { tags.add(new ShortTag(k, v)); }
        void putInt(String k, int v) { tags.add(new IntTag(k, v)); }
        void putLong(String k, long v) { tags.add(new LongTag(k, v)); }
        void putString(String k, String v) { tags.add(new StringTag(k, v)); }

        void writePayload(DataOutputStream out) throws IOException {
            for (Tag t : tags) {
                t.write(out);
            }
            out.writeByte(0); // TAG_End
        }
    }

    static class NbtStructure {
        int sx, sy, sz;
        List<CompoundTag> palette = new ArrayList<>();
        Map<String, Integer> paletteMap = new HashMap<>();
        Map<Long, BlockInfo> blockGrid = new HashMap<>();

        NbtStructure(int x, int y, int z) {
            this.sx = x;
            this.sy = y;
            this.sz = z;
        }

        int state(String name) {
            return state(name, Collections.emptyMap());
        }

        int state(String name, Map<String, String> props) {
            String key = name + props.toString();
            if (paletteMap.containsKey(key)) return paletteMap.get(key);

            CompoundTag p = new CompoundTag(null);
            p.putString("Name", name);
            if (!props.isEmpty()) {
                CompoundTag pprops = new CompoundTag("Properties");
                for (Map.Entry<String, String> e : props.entrySet()) {
                    pprops.putString(e.getKey(), e.getValue());
                }
                p.put(pprops);
            }
            int idx = palette.size();
            palette.add(p);
            paletteMap.put(key, idx);
            return idx;
        }

        void set(int x, int y, int z, int st) {
            set(x, y, z, st, null);
        }

        void set(int x, int y, int z, int st, CompoundTag nbt) {
            if (x < 0 || x >= sx || y < 0 || y >= sy || z < 0 || z >= sz) return;
            long key = ((long)x << 32) | ((long)y << 16) | (long)z;
            blockGrid.put(key, new BlockInfo(x, y, z, st, nbt));
        }

        void fill(int x0, int y0, int z0, int x1, int y1, int z1, int st) {
            for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
                for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                    for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                        set(x, y, z, st);
                    }
                }
            }
        }

        void save(File file) throws Exception {
            file.getParentFile().mkdirs();
            CompoundTag root = new CompoundTag("");
            root.putInt("DataVersion", 3465);

            ListTag sizeList = new ListTag("size", (byte)3);
            sizeList.add(new IntTag(null, sx));
            sizeList.add(new IntTag(null, sy));
            sizeList.add(new IntTag(null, sz));
            root.put(sizeList);

            ListTag palList = new ListTag("palette", (byte)10);
            for (CompoundTag p : palette) palList.add(p);
            root.put(palList);

            ListTag blkList = new ListTag("blocks", (byte)10);
            for (BlockInfo bi : blockGrid.values()) {
                CompoundTag b = new CompoundTag(null);
                ListTag pos = new ListTag("pos", (byte)3);
                pos.add(new IntTag(null, bi.x));
                pos.add(new IntTag(null, bi.y));
                pos.add(new IntTag(null, bi.z));
                b.put(pos);
                b.putInt("state", bi.state);
                if (bi.nbt != null) {
                    b.put(bi.nbt);
                }
                blkList.add(b);
            }
            root.put(blkList);
            root.put(new ListTag("entities", (byte)10));

            try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
                root.write(out);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int width = 73;  // ~72 meters (X: 0 to 72)
        int height = 7;  // Y: 0 to 6 (interior height 4, 5 ceiling, 6 roof parapet)
        int depth = 54;  // Z: 0 to 53

        NbtStructure s = new NbtStructure(width, height, depth);

        // 1. PALETTE DEFINITIONS
        // Custom blocks
        int baldosa = s.state("escuela:baldosa_escolar");
        int pared = s.state("escuela:pared_escolar");
        int lineaCancha = s.state("escuela:linea_cancha");
        int pizarronNorth = s.state("escuela:pizarron", Map.of("facing", "north"));
        int pizarronSouth = s.state("escuela:pizarron", Map.of("facing", "south"));
        int pizarronEast = s.state("escuela:pizarron", Map.of("facing", "east"));
        int pizarronWest = s.state("escuela:pizarron", Map.of("facing", "west"));
        int pupitreNorth = s.state("escuela:pupitre", Map.of("facing", "north"));
        int pupitreSouth = s.state("escuela:pupitre", Map.of("facing", "south"));

        // Vanilla materials
        int air = s.state("minecraft:air");
        int grass = s.state("minecraft:grass_block");
        int dirtPath = s.state("minecraft:dirt_path");
        int smoothStone = s.state("minecraft:smooth_stone");
        int stoneBricks = s.state("minecraft:stone_bricks");
        int stoneBrickWall = s.state("minecraft:stone_brick_wall");
        int ironBars = s.state("minecraft:iron_bars");
        int glassPane = s.state("minecraft:glass_pane");
        int tintedGlass = s.state("minecraft:tinted_glass");
        int quartzPillar = s.state("minecraft:quartz_pillar", Map.of("axis", "y"));
        int quartzBlock = s.state("minecraft:smooth_quartz");
        int quartzSlab = s.state("minecraft:smooth_quartz_slab", Map.of("type", "bottom"));
        int oakPlanks = s.state("minecraft:oak_planks");
        int birchPlanks = s.state("minecraft:birch_planks");
        int darkOakPlanks = s.state("minecraft:dark_oak_planks");
        int oakDoorLowerNorth = s.state("minecraft:oak_door", Map.of("half", "lower", "facing", "north"));
        int oakDoorUpperNorth = s.state("minecraft:oak_door", Map.of("half", "upper", "facing", "north"));
        int oakDoorLowerSouth = s.state("minecraft:oak_door", Map.of("half", "lower", "facing", "south"));
        int oakDoorUpperSouth = s.state("minecraft:oak_door", Map.of("half", "upper", "facing", "south"));
        int oakDoorLowerEast = s.state("minecraft:oak_door", Map.of("half", "lower", "facing", "east"));
        int oakDoorUpperEast = s.state("minecraft:oak_door", Map.of("half", "upper", "facing", "east"));
        int oakDoorLowerWest = s.state("minecraft:oak_door", Map.of("half", "lower", "facing", "west"));
        int oakDoorUpperWest = s.state("minecraft:oak_door", Map.of("half", "upper", "facing", "west"));
        int birchDoorLower = s.state("minecraft:birch_door", Map.of("half", "lower", "facing", "north"));
        int birchDoorUpper = s.state("minecraft:birch_door", Map.of("half", "upper", "facing", "north"));
        int ironDoorLower = s.state("minecraft:iron_door", Map.of("half", "lower", "facing", "north"));
        int ironDoorUpper = s.state("minecraft:iron_door", Map.of("half", "upper", "facing", "north"));
        int bookshelf = s.state("minecraft:bookshelf");
        int lecternNorth = s.state("minecraft:lectern", Map.of("facing", "north"));
        int cauldron = s.state("minecraft:cauldron");
        int waterCauldron = s.state("minecraft:water_cauldron", Map.of("level", "3"));
        int tripwireHook = s.state("minecraft:tripwire_hook", Map.of("facing", "south"));
        int lantern = s.state("minecraft:lantern", Map.of("hanging", "false"));
        int hangingLantern = s.state("minecraft:lantern", Map.of("hanging", "true"));
        int oakLeaves = s.state("minecraft:oak_leaves", Map.of("persistent", "true"));
        int oakLog = s.state("minecraft:oak_log", Map.of("axis", "y"));
        int greenConcrete = s.state("minecraft:green_concrete");
        int blueConcrete = s.state("minecraft:blue_concrete");
        int cobweb = s.state("minecraft:cobweb");
        int whiteWool = s.state("minecraft:white_wool");
        int redCarpet = s.state("minecraft:red_carpet");
        int polishedAndesite = s.state("minecraft:polished_andesite");
        int smoothStoneSlab = s.state("minecraft:smooth_stone_slab", Map.of("type", "bottom"));
        int oakStairsNorth = s.state("minecraft:oak_stairs", Map.of("facing", "north"));
        int oakStairsSouth = s.state("minecraft:oak_stairs", Map.of("facing", "south"));
        int oakStairsWest = s.state("minecraft:oak_stairs", Map.of("facing", "west"));
        int oakStairsEast = s.state("minecraft:oak_stairs", Map.of("facing", "east"));
        int barrel = s.state("minecraft:barrel", Map.of("facing", "up"));
        int chestNorth = s.state("minecraft:chest", Map.of("facing", "north"));
        int craftTable = s.state("minecraft:crafting_table");
        int flowerPot = s.state("minecraft:potted_poppy");
        int bannerArg = s.state("minecraft:light_blue_banner", Map.of("rotation", "0"));

        // ==========================================
        // 2. FOUNDATION & GROUND LEVEL (Y = 0)
        // ==========================================

        // Default lot ground: grass
        s.fill(0, 0, 0, width - 1, 0, depth - 1, grass);

        // North street curb / sidewalk (Z = 0)
        s.fill(0, 0, 0, width - 1, 0, 0, smoothStone);

        // North setback path / garden walk (Z = 1 to 5)
        for (int x = 2; x <= 70; x += 4) {
            s.set(x, 0, 1, dirtPath);
        }

        // Rotunda entrance plaza (X = 45 to 54, Z = 0 to 6)
        s.fill(45, 0, 1, 54, 0, 6, polishedAndesite);

        // Building indoor flooring:
        // Main School Classrooms & Hallway (X = 2 to 71, Z = 6 to 19)
        s.fill(2, 0, 6, 71, 0, 19, baldosa);

        // SUM Floor (Salón de Uso Múltiple, X = 54 to 71, Z = 14 to 38)
        s.fill(54, 0, 14, 71, 0, 18, baldosa); // Annex / Buffet
        s.fill(54, 0, 19, 71, 0, 38, birchPlanks); // Main Multipurpose parquet floor

        // Sports Court (Cancha Polideportiva, X = 17 to 53, Z = 21 to 37)
        s.fill(17, 0, 21, 53, 0, 37, greenConcrete);
        // Apron walkway around sports court
        s.fill(16, 0, 20, 54, 0, 20, smoothStone);
        s.fill(16, 0, 38, 54, 0, 38, smoothStone);
        s.fill(16, 0, 21, 16, 0, 37, smoothStone);
        s.fill(54, 0, 21, 54, 0, 37, smoothStone);

        // Sports Court Markings:
        // Outer boundary lines
        for (int x = 18; x <= 52; x++) {
            s.set(x, 0, 22, lineaCancha);
            s.set(x, 0, 36, lineaCancha);
        }
        for (int z = 22; z <= 36; z++) {
            s.set(18, 0, z, lineaCancha);
            s.set(52, 0, z, lineaCancha);
        }
        // Center line (half-court line at x = 35)
        for (int z = 22; z <= 36; z++) {
            s.set(35, 0, z, lineaCancha);
        }
        // Center circle (around x = 35, z = 29)
        s.set(34, 0, 27, lineaCancha); s.set(35, 0, 27, lineaCancha); s.set(36, 0, 27, lineaCancha);
        s.set(33, 0, 28, lineaCancha); s.set(37, 0, 28, lineaCancha);
        s.set(33, 0, 29, lineaCancha); s.set(37, 0, 29, lineaCancha);
        s.set(33, 0, 30, lineaCancha); s.set(37, 0, 30, lineaCancha);
        s.set(34, 0, 31, lineaCancha); s.set(35, 0, 31, lineaCancha); s.set(36, 0, 31, lineaCancha);

        // Penalty / Goal D-arcs
        // West goal arc (centered at x=18, z=29)
        for (int z = 26; z <= 32; z++) s.set(22, 0, z, lineaCancha);
        s.set(21, 0, 25, lineaCancha); s.set(21, 0, 33, lineaCancha);
        s.set(20, 0, 24, lineaCancha); s.set(20, 0, 34, lineaCancha);
        s.set(19, 0, 23, lineaCancha); s.set(19, 0, 35, lineaCancha);

        // East goal arc (centered at x=52, z=29)
        for (int z = 26; z <= 32; z++) s.set(48, 0, z, lineaCancha);
        s.set(49, 0, 25, lineaCancha); s.set(49, 0, 33, lineaCancha);
        s.set(50, 0, 24, lineaCancha); s.set(50, 0, 34, lineaCancha);
        s.set(51, 0, 23, lineaCancha); s.set(51, 0, 35, lineaCancha);

        // West Patio (X = 2 to 15, Z = 20 to 38)
        s.fill(2, 0, 20, 15, 0, 38, polishedAndesite);

        // Flagpole Bases in West Patio (Mástiles de la Bandera)
        s.set(8, 0, 27, quartzBlock);
        s.set(8, 0, 31, quartzBlock);

        // South Courtyard Walkways (Z = 39 to 53)
        for (int z = 39; z <= 52; z++) {
            s.set(55, 0, z, smoothStone);
            s.set(56, 0, z, smoothStone);
            s.set(20, 0, z, dirtPath);
        }
        for (int x = 16; x <= 56; x++) {
            s.set(x, 0, 42, dirtPath);
        }

        // Security Booth Floor (Portería escolar, X = 59 to 66, Z = 48 to 52)
        s.fill(59, 0, 48, 66, 0, 52, baldosa);

        // East Sidewalk (X = 72, Z = 0 to 53)
        s.fill(72, 0, 0, 72, 0, depth - 1, smoothStone);


        // ==========================================
        // 3. WALLS & VERTICAL STRUCTURE (Y = 1 to 4)
        // ==========================================

        // North Exterior Wall of School (Z = 6)
        // From X = 2 to 44 and X = 54 to 71
        s.fill(2, 1, 6, 44, 4, 6, pared);
        s.fill(54, 1, 6, 71, 4, 6, pared);

        // Windows on North Classrooms (Y = 2 to 3)
        // Classrooms: 1 (2..8), 2 (9..15), 3 (16..22), 4 (23..29), 5 (30..36), 6 (37..44)
        int[] winNorth = {4, 5, 6,  11, 12, 13,  18, 19, 20,  25, 26, 27,  32, 33, 34,  39, 40, 41,  57, 58, 59,  65, 66, 67};
        for (int wx : winNorth) {
            s.set(wx, 2, 6, glassPane);
            s.set(wx, 3, 6, glassPane);
        }

        // West Exterior Wall of School (X = 2, Z = 6 to 19)
        s.fill(2, 1, 6, 2, 4, 19, pared);
        s.set(2, 2, 9, glassPane); s.set(2, 3, 9, glassPane);
        s.set(2, 2, 16, glassPane); s.set(2, 3, 16, glassPane);

        // East Exterior Wall of School & SUM (X = 71, Z = 6 to 38)
        s.fill(71, 1, 6, 71, 4, 38, pared);
        for (int z = 8; z <= 36; z += 4) {
            s.set(71, 2, z, glassPane);
            s.set(71, 3, z, glassPane);
        }

        // Semicircular Entrance Rotunda (X = 45 to 54, Z = 0 to 6)
        // Six decorative entrance pillars (Y = 1 to 4)
        int[][] rotundaPillars = {{46, 2}, {47, 1}, {49, 0}, {50, 0}, {52, 1}, {53, 2}};
        for (int[] p : rotundaPillars) {
            for (int y = 1; y <= 4; y++) {
                s.set(p[0], y, p[1], quartzPillar);
            }
        }
        // Rotunda side curved walls (Y = 1 to 4)
        for (int y = 1; y <= 4; y++) {
            s.set(45, y, 3, pared);
            s.set(45, y, 4, (y == 2 || y == 3) ? glassPane : pared);
            s.set(45, y, 5, pared);

            s.set(54, y, 3, pared);
            s.set(54, y, 4, (y == 2 || y == 3) ? glassPane : pared);
            s.set(54, y, 5, pared);
        }
        // Main School Entrance Doors at Z = 6 (X = 49 and 50)
        s.set(49, 1, 6, oakDoorLowerSouth);
        s.set(49, 2, 6, oakDoorUpperSouth);
        s.set(50, 1, 6, oakDoorLowerSouth);
        s.set(50, 2, 6, oakDoorUpperSouth);
        s.set(48, 1, 6, pared); s.set(48, 2, 6, glassPane); s.set(48, 3, 6, glassPane); s.set(48, 4, 6, pared);
        s.set(51, 1, 6, pared); s.set(51, 2, 6, glassPane); s.set(51, 3, 6, glassPane); s.set(51, 4, 6, pared);

        // Classroom Dividing Walls (North Wing, Z = 7 to 11, Y = 1 to 4)
        int[] classDividers = {8, 15, 22, 29, 36, 44, 62};
        for (int cx : classDividers) {
            s.fill(cx, 1, 7, cx, 4, 11, pared);
        }

        // Classroom Corridor Wall (Z = 11, X = 2 to 44 and X = 54 to 71)
        s.fill(2, 1, 11, 44, 4, 11, pared);
        s.fill(54, 1, 11, 71, 4, 11, pared);

        // Classroom Entrance Doors (Z = 11)
        int[] classDoors = {3, 10, 17, 24, 31, 38, 56, 64};
        for (int dx : classDoors) {
            s.set(dx, 1, 11, oakDoorLowerNorth);
            s.set(dx, 2, 11, oakDoorUpperNorth);
            s.set(dx, 3, 11, glassPane); // Transom light
        }

        // South Wall of Central Hallway (Z = 15, X = 2 to 45)
        s.fill(2, 1, 15, 45, 4, 15, pared);

        // Doors from Hallway to South Rooms (Z = 15)
        s.set(5, 1, 15, oakDoorLowerSouth);
        s.set(5, 2, 15, oakDoorUpperSouth); // Dirección door
        s.set(12, 1, 15, oakDoorLowerSouth);
        s.set(12, 2, 15, oakDoorUpperSouth); // Sala de Profesores
        s.set(17, 1, 15, oakDoorLowerSouth);
        s.set(17, 2, 15, oakDoorUpperSouth); // Baño Mujeres
        s.set(20, 1, 15, oakDoorLowerSouth);
        s.set(20, 2, 15, oakDoorUpperSouth); // Baño Varones
        s.set(28, 1, 15, oakDoorLowerSouth);
        s.set(28, 2, 15, oakDoorUpperSouth); // Preceptoría / Enfermería
        s.set(38, 1, 15, oakDoorLowerSouth);
        s.set(38, 2, 15, oakDoorUpperSouth); // Depósito

        // South Rooms Dividers (Z = 15 to 19, Y = 1 to 4)
        s.fill(9, 1, 16, 9, 4, 19, pared);   // Between Dirección and Sala de Profesores
        s.fill(15, 1, 16, 15, 4, 19, pared); // Between Sala de Profesores and Baños
        s.fill(18, 1, 16, 18, 4, 19, quartzBlock); // Stall partition in Baños
        s.fill(23, 1, 16, 23, 4, 19, pared); // End of west administrative block
        s.fill(33, 1, 16, 33, 4, 19, pared); // Between Preceptoría and Depósito

        // South Exterior Wall of Administrative wing (Z = 19, X = 2 to 23 and X = 24 to 45)
        s.fill(2, 1, 19, 23, 4, 19, pared);
        s.fill(24, 1, 19, 45, 4, 19, pared);
        // Windows overlooking courtyard
        for (int wx : new int[]{4, 7, 11, 13, 26, 30, 36, 40}) {
            s.set(wx, 2, 19, glassPane);
            s.set(wx, 3, 19, glassPane);
        }

        // Direct Double Door exit from Main Hallway to Patio / Sports Court (Z = 15, X = 48..49)
        s.fill(46, 1, 15, 53, 4, 15, pared);
        s.set(49, 1, 15, oakDoorLowerSouth);
        s.set(49, 2, 15, oakDoorUpperSouth);
        s.set(50, 1, 15, oakDoorLowerSouth);
        s.set(50, 2, 15, oakDoorUpperSouth);

        // ==========================================
        // 4. THE MULTIPURPOSE HALL (SUM - Right Wing)
        // ==========================================
        // "el salón que se encuentra a la derecha del plano es de uso multiple"
        // Location: X = 54 to 71, Z = 14 to 38

        // West Wall of SUM facing Courtyard / Court (X = 54, Z = 14 to 38, Y = 1 to 4)
        s.fill(54, 1, 14, 54, 4, 38, pared);
        // Double doors opening to sports patio from SUM
        s.set(54, 1, 24, oakDoorLowerWest);
        s.set(54, 2, 24, oakDoorUpperWest);
        s.set(54, 1, 25, oakDoorLowerWest);
        s.set(54, 2, 25, oakDoorUpperWest);

        s.set(54, 1, 31, oakDoorLowerWest);
        s.set(54, 2, 31, oakDoorUpperWest);
        s.set(54, 1, 32, oakDoorLowerWest);
        s.set(54, 2, 32, oakDoorUpperWest);

        // Large viewing windows on SUM west wall overlooking the sports court
        for (int z : new int[]{21, 22, 27, 28, 29, 34, 35}) {
            s.set(54, 2, z, glassPane);
            s.set(54, 3, z, glassPane);
        }

        // Interior Divider between SUM Annex/Buffet and Main Hall (Z = 18, X = 55 to 70)
        s.fill(55, 1, 18, 70, 4, 18, pared);
        // Double doors into main SUM hall
        s.set(62, 1, 18, oakDoorLowerSouth);
        s.set(62, 2, 18, oakDoorUpperSouth);
        s.set(63, 1, 18, oakDoorLowerSouth);
        s.set(63, 2, 18, oakDoorUpperSouth);

        // South Exterior Wall of SUM (Z = 38, X = 54 to 71, Y = 1 to 4)
        s.fill(54, 1, 38, 71, 4, 38, pared);
        // Emergency double doors at south of SUM
        s.set(62, 1, 38, oakDoorLowerSouth);
        s.set(62, 2, 38, oakDoorUpperSouth);
        s.set(63, 1, 38, oakDoorLowerSouth);
        s.set(63, 2, 38, oakDoorUpperSouth);
        s.set(57, 2, 38, glassPane); s.set(57, 3, 38, glassPane);
        s.set(68, 2, 38, glassPane); s.set(68, 3, 38, glassPane);

        // Elevated Stage in the SUM (Escenario escolar, Z = 33 to 37, X = 56 to 69)
        s.fill(56, 1, 33, 69, 1, 37, darkOakPlanks);
        // Stage steps at front
        for (int x = 58; x <= 67; x++) {
            s.set(x, 1, 32, oakStairsSouth);
        }
        // Red carpet along stage center
        for (int z = 33; z <= 36; z++) {
            s.set(62, 2, z, redCarpet);
            s.set(63, 2, z, redCarpet);
        }
        // Lectern / Podium on stage
        s.set(62, 2, 34, lecternNorth);
        // Theatrical backdrop curtains / decoration on stage back wall (Z = 37)
        for (int x = 57; x <= 68; x++) {
            if (x == 57 || x == 58 || x == 67 || x == 68) {
                s.set(x, 2, 37, whiteWool);
                s.set(x, 3, 37, whiteWool);
            }
        }

        // Multipurpose tables and seating inside the SUM (Z = 20 to 30, X = 56 to 69)
        for (int z = 21; z <= 29; z += 3) {
            // Table row
            for (int x = 57; x <= 67; x++) {
                s.set(x, 1, z, smoothStoneSlab);
                s.set(x, 1, z - 1, oakStairsNorth); // chairs facing north
                s.set(x, 1, z + 1, oakStairsSouth); // chairs facing south
            }
        }

        // Buffet / Cantina Escolar inside SUM Annex (Z = 14 to 17, X = 55 to 70)
        s.fill(66, 1, 15, 70, 1, 15, smoothStoneSlab); // Serving counter
        s.set(69, 1, 17, craftTable);
        s.set(68, 1, 17, barrel);
        s.set(67, 1, 17, chestNorth);

        // ==========================================
        // 5. CLASSROOM INTERIOR FURNISHINGS
        // ==========================================
        // 6 Classrooms along North wing:
        // Classroom 1: X = 3..7, Z = 7..10
        // Classroom 2: X = 10..14, Z = 7..10
        // Classroom 3: X = 17..21, Z = 7..10
        // Classroom 4: X = 24..28, Z = 7..10
        // Classroom 5: X = 31..35, Z = 7..10
        // Classroom 6: X = 38..43, Z = 7..10
        int[][] classRooms = {
            {3, 7}, {10, 14}, {17, 21}, {24, 28}, {31, 35}, {38, 43}
        };

        for (int[] cr : classRooms) {
            int xMin = cr[0];
            int xMax = cr[1];

            // Teacher blackboard (Pizarrón) on East dividing wall
            s.set(xMax, 2, 8, pizarronWest);
            s.set(xMax, 2, 9, pizarronWest);

            // Teacher's Desk & Chair
            s.set(xMax - 1, 1, 8, oakPlanks);
            s.set(xMax - 1, 2, 8, flowerPot);
            s.set(xMax - 1, 1, 9, oakStairsWest);

            // Pupil Desks (Pupitres Escolares) in neat rows facing the chalkboard (East)
            for (int x = xMin; x <= xMax - 2; x += 2) {
                for (int z = 7; z <= 10; z += 2) {
                    s.set(x, 1, z, pupitreSouth);
                }
            }

            // Bookshelf at back corner
            s.set(xMin, 1, 7, bookshelf);
            s.set(xMin, 2, 7, bookshelf);

            // Ceiling light
            s.set((xMin + xMax) / 2, 4, 9, hangingLantern);
        }

        // East Classrooms / Library (X = 55 to 70, Z = 7 to 10)
        // Library (X = 55 to 61):
        s.fill(55, 1, 7, 55, 3, 10, bookshelf);
        s.fill(61, 1, 7, 61, 3, 9, bookshelf);
        // Reading table
        s.fill(57, 1, 8, 59, 1, 9, smoothStoneSlab);
        s.set(56, 1, 8, oakStairsEast); s.set(56, 1, 9, oakStairsEast);
        s.set(60, 1, 8, oakStairsWest); s.set(60, 1, 9, oakStairsWest);
        s.set(58, 2, 8, flowerPot);
        s.set(58, 4, 8, hangingLantern);

        // Science / Computer Lab (X = 63 to 70):
        s.fill(64, 1, 7, 69, 1, 7, smoothStoneSlab);
        s.fill(64, 1, 9, 69, 1, 9, smoothStoneSlab);
        for (int x = 64; x <= 69; x += 2) {
            s.set(x, 1, 8, oakStairsNorth);
            s.set(x, 2, 7, flowerPot);
        }
        s.set(66, 4, 8, hangingLantern);

        // ==========================================
        // 6. ADMINISTRATION & SERVICE ROOMS
        // ==========================================
        // Dirección (Principal's Office, X = 3 to 8, Z = 16 to 18)
        s.set(7, 1, 17, bookshelf);
        s.set(7, 2, 17, bookshelf);
        s.set(5, 1, 17, darkOakPlanks); // Principal's desk
        s.set(5, 2, 17, flowerPot);
        s.set(5, 1, 18, oakStairsNorth); // chair
        s.set(5, 4, 17, hangingLantern);

        // Sala de Profesores (Teachers' Lounge, X = 10 to 14, Z = 16 to 18)
        s.fill(11, 1, 17, 13, 1, 17, smoothStoneSlab); // conference table
        s.set(11, 1, 16, oakStairsSouth);
        s.set(12, 1, 16, oakStairsSouth);
        s.set(13, 1, 16, oakStairsSouth);
        s.set(11, 1, 18, oakStairsNorth);
        s.set(12, 1, 18, oakStairsNorth);
        s.set(13, 1, 18, oakStairsNorth);
        s.set(10, 1, 16, cauldron); // coffee station
        s.set(12, 4, 17, hangingLantern);

        // Baños (Restrooms, X = 16 to 22, Z = 16 to 18)
        s.set(16, 1, 18, waterCauldron);
        s.set(16, 2, 18, tripwireHook);
        s.set(21, 1, 18, waterCauldron);
        s.set(21, 2, 18, tripwireHook);
        s.set(17, 1, 16, birchDoorLower);
        s.set(17, 2, 16, birchDoorUpper);
        s.set(20, 1, 16, birchDoorLower);
        s.set(20, 2, 16, birchDoorUpper);

        // Preceptoría & Enfermería (X = 25 to 32, Z = 16 to 18)
        s.set(26, 1, 17, whiteWool); // nursing bed
        s.set(26, 1, 18, whiteWool);
        s.set(30, 1, 17, oakPlanks); // desk
        s.set(30, 1, 18, oakStairsNorth);
        s.set(29, 4, 17, hangingLantern);

        // Depósito Educación Física (PE Storage, X = 34 to 43, Z = 16 to 18)
        s.set(35, 1, 17, chestNorth);
        s.set(36, 1, 17, barrel);
        s.set(37, 1, 17, barrel);
        s.set(41, 1, 17, chestNorth);
        s.set(39, 4, 17, hangingLantern);

        // Central Hallway Lighting & Benches (Z = 12 to 14, X = 2 to 71)
        for (int x = 6; x <= 68; x += 6) {
            s.set(x, 4, 13, hangingLantern);
            s.set(x, 1, 12, oakStairsSouth); // Hallway benches
            s.set(x + 1, 1, 12, oakStairsSouth);
        }

        // ==========================================
        // 7. ROOF & CEILING (Y = 5 and Y = 6)
        // ==========================================
        // Flat institutional school roof over the classrooms, hallway, admin, and SUM
        // Main wing: X = 2 to 71, Z = 6 to 19
        s.fill(2, 5, 6, 71, 5, 19, quartzBlock);
        // SUM roof: X = 54 to 71, Z = 19 to 38
        s.fill(54, 5, 19, 71, 5, 38, quartzBlock);

        // Roof parapet / edge trim (Y = 6)
        for (int x = 2; x <= 71; x++) {
            s.set(x, 6, 6, smoothStoneSlab);
            s.set(x, 6, 19, smoothStoneSlab);
        }
        for (int z = 6; z <= 19; z++) {
            s.set(2, 6, z, smoothStoneSlab);
        }
        for (int z = 19; z <= 38; z++) {
            s.set(54, 6, z, smoothStoneSlab);
            s.set(71, 6, z, smoothStoneSlab);
        }
        for (int x = 54; x <= 71; x++) {
            s.set(x, 6, 38, smoothStoneSlab);
        }

        // Rotunda Entrance Glass Dome Roof (X = 46 to 53, Z = 1 to 5)
        for (int x = 46; x <= 53; x++) {
            for (int z = 1; z <= 5; z++) {
                s.set(x, 5, z, tintedGlass);
            }
        }
        // Center skylight lantern
        s.set(49, 4, 3, hangingLantern);
        s.set(50, 4, 3, hangingLantern);

        // ==========================================
        // 8. SPORTS COURT GOALS & DETAILS
        // ==========================================
        // West Goal (at X = 17, Z = 28 to 30)
        s.fill(17, 1, 28, 17, 2, 28, ironBars);
        s.fill(17, 1, 30, 17, 2, 30, ironBars);
        s.fill(17, 2, 28, 17, 2, 30, ironBars); // Crossbar
        s.fill(16, 1, 28, 16, 2, 30, cobweb);   // Netting
        // Basketball hoop on west goal
        s.set(17, 3, 29, quartzBlock);
        s.set(18, 3, 29, ironBars);

        // East Goal (at X = 53, Z = 28 to 30)
        s.fill(53, 1, 28, 53, 2, 28, ironBars);
        s.fill(53, 1, 30, 53, 2, 30, ironBars);
        s.fill(53, 2, 28, 53, 2, 30, ironBars); // Crossbar
        s.fill(54, 1, 28, 54, 2, 30, cobweb);   // Netting
        // Basketball hoop on east goal
        s.set(53, 3, 29, quartzBlock);
        s.set(52, 3, 29, ironBars);

        // ==========================================
        // 9. COURTYARD, FLAGPOLES & PERIMETER
        // ==========================================
        // Flagpoles in West Patio (Mástiles de la Bandera Argentina)
        // Mástil 1 at (8, 27)
        s.set(8, 1, 27, quartzBlock);
        for (int y = 2; y <= 5; y++) s.set(8, y, 27, ironBars);
        s.set(8, 5, 28, bannerArg); // Argentine Flag banner

        // Mástil 2 at (8, 31)
        s.set(8, 1, 31, quartzBlock);
        for (int y = 2; y <= 5; y++) s.set(8, y, 31, ironBars);
        s.set(8, 5, 32, bannerArg);

        // Benches along the west patio wall (X = 3, Z = 22 to 36)
        for (int z = 23; z <= 35; z += 3) {
            s.set(3, 1, z, oakStairsEast);
            s.set(3, 1, z + 1, oakStairsEast);
        }

        // Security Booth / Portería (X = 59 to 66, Z = 48 to 52)
        // Walls (Y = 1 to 3)
        s.fill(59, 1, 48, 66, 3, 48, pared);
        s.fill(59, 1, 52, 66, 3, 52, pared);
        s.fill(59, 1, 48, 59, 3, 52, pared);
        s.fill(66, 1, 48, 66, 3, 52, pared);
        // Windows
        s.set(62, 2, 48, glassPane); s.set(63, 2, 48, glassPane);
        s.set(62, 2, 52, glassPane); s.set(63, 2, 52, glassPane);
        s.set(59, 2, 50, glassPane);
        // Door
        s.set(66, 1, 50, oakDoorLowerWest);
        s.set(66, 2, 50, oakDoorUpperWest);
        // Interior: desk, chair, lantern
        s.set(61, 1, 50, oakPlanks);
        s.set(61, 2, 50, lantern);
        s.set(62, 1, 50, oakStairsWest);
        // Roof of portería
        s.fill(59, 4, 48, 66, 4, 52, smoothStoneSlab);

        // Main Entrance Gate on South Perimeter (Z = 53, X = 53 to 58)
        s.set(52, 1, 53, stoneBricks); s.set(52, 2, 53, stoneBricks); s.set(52, 3, 53, lantern);
        s.set(59, 1, 53, stoneBricks); s.set(59, 2, 53, stoneBricks); s.set(59, 3, 53, lantern);
        for (int x = 53; x <= 58; x++) {
            s.set(x, 1, 53, ironBars);
            s.set(x, 2, 53, ironBars);
        }

        // Perimeter Walls:
        // North Wall (along street Z = 0)
        for (int x = 0; x <= width - 1; x++) {
            if (x < 45 || x > 54) { // Don't block entrance rotunda
                s.set(x, 1, 0, stoneBrickWall);
                s.set(x, 2, 0, ironBars);
            }
        }
        // West Perimeter Wall (X = 0, Z = 0 to 53)
        for (int z = 0; z <= depth - 1; z++) {
            s.set(0, 1, z, stoneBrickWall);
            s.set(0, 2, z, ironBars);
        }
        // South Perimeter Wall (Z = 53, X = 0 to 72)
        for (int x = 0; x <= width - 1; x++) {
            if (x < 53 || x > 58) { // Don't block main gate
                s.set(x, 1, 53, stoneBrickWall);
                s.set(x, 2, 53, ironBars);
            }
        }
        // East Perimeter Wall (X = 72, Z = 0 to 53)
        for (int z = 0; z <= depth - 1; z++) {
            s.set(72, 1, z, stoneBrickWall);
            s.set(72, 2, z, ironBars);
        }

        // ==========================================
        // 10. TREES & NATURAL LANDSCAPING
        // ==========================================
        // North Setback Trees (around Z = 3)
        int[] treeNorthX = {5, 12, 19, 26, 33, 40, 60, 67};
        for (int tx : treeNorthX) {
            plantTree(s, tx, 3, oakLog, oakLeaves);
        }

        // South Park Trees (around Z = 45 to 50)
        int[][] southTrees = {
            {6, 47}, {14, 46}, {22, 48}, {30, 47}, {38, 48}, {46, 46}, {69, 46}
        };
        for (int[] st : southTrees) {
            plantTree(s, st[0], st[1], oakLog, oakLeaves);
        }

        // East Sidewalk Trees (around X = 71, Z = 4, 18, 44)
        plantTree(s, 70, 3, oakLog, oakLeaves);
        plantTree(s, 70, 44, oakLog, oakLeaves);

        // Flower beds in south park
        int[] flowerX = {10, 18, 26, 34, 42, 50};
        for (int fx : flowerX) {
            s.set(fx, 1, 44, flowerPot);
            s.set(fx + 2, 1, 44, flowerPot);
        }

        // Save structure NBT file
        String targetPath = args.length > 0 ? args[0] : "src/main/resources/data/escuela/structures/escuela.nbt";
        File outFile = new File(targetPath);
        s.save(outFile);

        System.out.printf("School structure successfully built! Total blocks placed: %d\nSaved to: %s\n",
                s.blockGrid.size(), outFile.getAbsolutePath());
    }

    private static void plantTree(NbtStructure s, int x, int z, int log, int leaves) {
        // Trunk
        for (int y = 1; y <= 4; y++) {
            s.set(x, y, z, log);
        }
        // Leaf canopy at y = 3, 4, 5
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    s.set(x + dx, 3, z + dz, leaves);
                    s.set(x + dx, 4, z + dz, leaves);
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                s.set(x + dx, 5, z + dz, leaves);
            }
        }
    }
}
