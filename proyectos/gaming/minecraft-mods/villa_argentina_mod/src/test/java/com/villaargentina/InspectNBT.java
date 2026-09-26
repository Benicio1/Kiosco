package com.villaargentina;
import net.minecraft.nbt.*;
import java.io.*;
public class InspectNBT {
    public static void main(String[] args) throws Exception {
        CompoundTag tag = NbtIo.readCompressed(new File("data/minecraft/structures/village/plains/houses/plains_small_house_1.nbt"));
        ListTag palette = tag.getList("palette", 10);
        ListTag blocks = tag.getList("blocks", 10);
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag b = blocks.getCompound(i);
            CompoundTag p = palette.getCompound(b.getInt("state"));
            if (p.getString("Name").contains("bed")) {
                System.out.println("BED: " + p + " pos=" + b.get("pos") + " nbt=" + b.get("nbt"));
            }
        }
    }
}
