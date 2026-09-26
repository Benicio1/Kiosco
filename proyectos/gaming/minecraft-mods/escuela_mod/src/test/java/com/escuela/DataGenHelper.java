package com.escuela;

import java.io.File;
import java.io.FileWriter;

public class DataGenHelper {
    public static void main(String[] args) throws Exception {
        String[] blocks = {"pizarron", "pupitre", "baldosa_escolar", "pared_escolar", "linea_cancha"};
        File lootDir = new File("src/main/resources/data/escuela/loot_tables/blocks");
        lootDir.mkdirs();

        for (String b : blocks) {
            String json = "{\n" +
                    "  \"type\": \"minecraft:block\",\n" +
                    "  \"pools\": [\n" +
                    "    {\n" +
                    "      \"rolls\": 1.0,\n" +
                    "      \"bonus_rolls\": 0.0,\n" +
                    "      \"entries\": [\n" +
                    "        {\n" +
                    "          \"type\": \"minecraft:item\",\n" +
                    "          \"name\": \"escuela:" + b + "\"\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"conditions\": [\n" +
                    "        {\n" +
                    "          \"condition\": \"minecraft:survives_explosion\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            try (FileWriter fw = new FileWriter(new File(lootDir, b + ".json"))) {
                fw.write(json);
            }
        }

        // Tags
        File tagsDir = new File("src/main/resources/data/minecraft/tags/blocks");
        tagsDir.mkdirs();
        
        String pickaxeTag = "{\n" +
                "  \"replace\": false,\n" +
                "  \"values\": [\n" +
                "    \"escuela:baldosa_escolar\",\n" +
                "    \"escuela:pared_escolar\",\n" +
                "    \"escuela:linea_cancha\"\n" +
                "  ]\n" +
                "}";
        File pickaxeFile = new File(tagsDir, "mineable/pickaxe.json");
        pickaxeFile.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(pickaxeFile)) {
            fw.write(pickaxeTag);
        }

        String axeTag = "{\n" +
                "  \"replace\": false,\n" +
                "  \"values\": [\n" +
                "    \"escuela:pizarron\",\n" +
                "    \"escuela:pupitre\"\n" +
                "  ]\n" +
                "}";
        File axeFile = new File(tagsDir, "mineable/axe.json");
        axeFile.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(axeFile)) {
            fw.write(axeTag);
        }

        System.out.println("Data files generated successfully!");
    }
}
