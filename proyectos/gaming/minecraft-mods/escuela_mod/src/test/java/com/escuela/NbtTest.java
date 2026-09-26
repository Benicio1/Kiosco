package com.escuela;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NbtTest {
    public static void main(String[] args) throws Exception {
        // Read an existing file to see exact structure
        File f = new File("c:/Users/Benicio/Documents/Antigravity Principal/Minecraft Mods/villa_argentina_mod/src/main/resources/data/villaargentina/structures/villa_potrero.nbt");
        if (f.exists()) {
            try (DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(f)))) {
                byte rootType = in.readByte();
                String rootName = in.readUTF();
                System.out.println("Root type: " + rootType + ", name: '" + rootName + "'");
            }
        }
    }
}
