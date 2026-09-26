package com.villaargentina;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TextureGen {
    public static void main(String[] args) throws Exception {
        String base = args.length > 0 ? args[0] : "src/main/resources/assets/villaargentina/textures";
        new File(base + "/block").mkdirs();
        new File(base + "/item").mkdirs();

        genLadrilloHueco(base + "/block/ladrillo_hueco.png");
        genChapaZinc(base + "/block/chapa_zinc.png");
        genChapaOxidada(base + "/block/chapa_oxidada.png");
        genTanqueAgua(base + "/block/tanque_agua.png");
        genParrillaTambor(base + "/block/parrilla_tambor.png");

        genMate(base + "/item/mate.png");
        genTermo(base + "/item/termo.png");
        genChoripan(base + "/item/choripan.png");
        genBotellaFernet(base + "/item/botella_fernet.png");
        genDiscoCumbia(base + "/item/disco_cumbia.png");

        System.out.println("All textures generated successfully!");
    }

    private static void genLadrilloHueco(String path) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Color baseOrange = new Color(208, 92, 42);
        Color darkOrange = new Color(175, 70, 30);
        Color mortar = new Color(160, 160, 155);
        Color hole = new Color(75, 30, 15);

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                // Mortar lines
                if (y == 7 || y == 15 || (y < 7 && x == 8) || (y > 7 && y < 15 && (x == 4 || x == 12))) {
                    img.setRGB(x, y, mortar.getRGB());
                } else {
                    img.setRGB(x, y, ((x + y) % 3 == 0 ? darkOrange : baseOrange).getRGB());
                }
            }
        }
        // Hollow brick holes
        int[][] holes = {{2, 2}, {5, 2}, {10, 2}, {13, 2}, {2, 5}, {5, 5}, {10, 5}, {13, 5},
                         {1, 10}, {2, 10}, {6, 10}, {7, 10}, {9, 10}, {10, 10}, {14, 10},
                         {1, 13}, {2, 13}, {6, 13}, {7, 13}, {9, 13}, {10, 13}, {14, 13}};
        for (int[] h : holes) {
            if (h[0] < 16 && h[1] < 16) img.setRGB(h[0], h[1], hole.getRGB());
        }
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void genChapaZinc(String path) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int[] shades = {0xFFD2D7DC, 0xFFB8BFC6, 0xFF9EA7B0, 0xFF838C96};
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int wave = Math.abs((x % 4) - 2);
                int rgb = shades[wave];
                if ((x + y * 7) % 19 == 0) rgb = 0xFF707A84; // nail or rivet
                img.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void genChapaOxidada(String path) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int[] zincShades = {0xFFB8BFC6, 0xFFA0A8B0, 0xFF889098, 0xFF707880};
        int[] rustShades = {0xFFB55225, 0xFF8E3B16, 0xFF6B2B0F, 0xFFD86B30};

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int wave = Math.abs((x % 4) - 2);
                boolean isRust = ((x * 3 + y * 5) % 7 < 3) || (y > 9 && (x + y) % 3 != 0);
                int rgb = isRust ? rustShades[(x + y) % 4] : zincShades[wave];
                img.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void genTanqueAgua(String path) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int blueLight = 0xFF0D6EFD;
        int blueMid = 0xFF0A58CA;
        int blueDark = 0xFF084298;
        int blackLid = 0xFF212529;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (y < 2) {
                    img.setRGB(x, y, blackLid);
                } else if (y == 5 || y == 9 || y == 13) {
                    img.setRGB(x, y, blueDark); // Structural ridges
                } else {
                    int c = (x < 3 || x > 12) ? blueDark : (x > 5 && x < 10 ? blueLight : blueMid);
                    img.setRGB(x, y, c);
                }
            }
        }
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void genParrillaTambor(String path) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int drumMetal = 0xFF2B2D42;
        int grillBar = 0xFF8D99AE;
        int coalRed = 0xFFE63946;
        int coalOrange = 0xFFFF7B00;
        int coalDark = 0xFF1B1B1E;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (y < 3) {
                    // Grill grate on top
                    img.setRGB(x, y, (x % 2 == 0) ? grillBar : coalDark);
                } else if (y < 6) {
                    // Glowing coals
                    int c = ((x + y) % 3 == 0) ? coalOrange : (((x + y) % 2 == 0) ? coalRed : coalDark);
                    img.setRGB(x, y, c);
                } else {
                    // Half drum metal body
                    int c = (x == 0 || x == 15 || y == 15) ? 0xFF15161E : drumMetal;
                    img.setRGB(x, y, c);
                }
            }
        }
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void genMate(String path) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        // Mate shape
        int wood = 0xFF6F4E37;
        int woodDark = 0xFF4A3222;
        int yerba = 0xFF386641;
        int silver = 0xFFD8DBE2;
        int silverDark = 0xFFA9B2C3;

        // Bombilla (diagonal)
        img.setRGB(11, 2, silver);
        img.setRGB(10, 3, silver);
        img.setRGB(9, 4, silver);
        img.setRGB(8, 5, silverDark);
        img.setRGB(7, 6, silverDark);

        // Gourd body
        for (int y = 6; y <= 14; y++) {
            for (int x = 4; x <= 11; x++) {
                if (y == 6) {
                    img.setRGB(x, y, yerba);
                } else if (y == 14 && (x == 4 || x == 11)) {
                    // rounded bottom
                } else if (x == 4 || x == 11 || y == 14) {
                    img.setRGB(x, y, woodDark);
                } else {
                    img.setRGB(x, y, (x > 5 && x < 9) ? wood : woodDark);
                }
            }
        }
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void genTermo(String path) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int metal = 0xFFC0C5C9;
        int metalLight = 0xFFE2E6E9;
        int metalDark = 0xFF8A929A;
        int redCap = 0xFFD90429;
        int blackHandle = 0xFF212529;

        // Red cap
        img.setRGB(6, 1, redCap);
        img.setRGB(7, 1, redCap);
        img.setRGB(6, 2, redCap);
        img.setRGB(7, 2, redCap);

        // Termo cylinder
        for (int y = 3; y <= 14; y++) {
            for (int x = 5; x <= 9; x++) {
                if (x == 5) img.setRGB(x, y, metalDark);
                else if (x == 6) img.setRGB(x, y, metal);
                else if (x == 7) img.setRGB(x, y, metalLight);
                else img.setRGB(x, y, metalDark);
            }
        }
        // Handle
        img.setRGB(10, 5, blackHandle);
        img.setRGB(11, 6, blackHandle);
        img.setRGB(11, 7, blackHandle);
        img.setRGB(11, 8, blackHandle);
        img.setRGB(10, 9, blackHandle);

        ImageIO.write(img, "PNG", new File(path));
    }

    private static void genChoripan(String path) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int bread = 0xFFE9C46A;
        int breadCrust = 0xFFBC8A38;
        int chori = 0xFF9E2A2B;
        int choriDark = 0xFF6B1D1E;
        int chimi = 0xFF588157;

        // Top bread
        for (int x = 3; x <= 13; x++) {
            img.setRGB(x, 4, breadCrust);
            img.setRGB(x, 5, bread);
        }
        // Chorizo & Chimichurri
        for (int x = 2; x <= 14; x++) {
            img.setRGB(x, 6, (x % 3 == 0) ? chimi : chori);
            img.setRGB(x, 7, (x % 2 == 0) ? choriDark : chori);
        }
        // Bottom bread
        for (int x = 3; x <= 13; x++) {
            img.setRGB(x, 8, bread);
            img.setRGB(x, 9, breadCrust);
        }
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void genBotellaFernet(String path) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int plastic = 0xAA80D0C7;
        int cutEdge = 0xFFFFFFFF;
        int fernet = 0xFF1A0A00;
        int foam = 0xFFE9D8A6;
        int ice = 0xCCD4F1F4;

        // Cut bottle rim
        for (int x = 4; x <= 11; x++) {
            img.setRGB(x, 5, cutEdge);
            img.setRGB(x, 6, foam);
        }
        // Liquid
        for (int y = 7; y <= 14; y++) {
            for (int x = 4; x <= 11; x++) {
                if (x == 4 || x == 11 || y == 14) {
                    img.setRGB(x, y, plastic);
                } else if ((x == 6 || x == 7) && (y == 8 || y == 9)) {
                    img.setRGB(x, y, ice); // Ice cube
                } else {
                    img.setRGB(x, y, fernet);
                }
            }
        }
        ImageIO.write(img, "PNG", new File(path));
    }

    private static void genDiscoCumbia(String path) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int vinyl = 0xFF181818;
        int groove = 0xFF2A2A2A;
        int celeste = 0xFF75AADB;
        int blanco = 0xFFFFFFFF;
        int hole = 0xFF000000;

        for (int y = 1; y <= 14; y++) {
            for (int x = 1; x <= 14; x++) {
                double dist = Math.hypot(x - 7.5, y - 7.5);
                if (dist > 6.8) continue;
                if (dist <= 1.0) {
                    img.setRGB(x, y, hole);
                } else if (dist <= 2.2) {
                    img.setRGB(x, y, (y == 7 || y == 8) ? blanco : celeste); // Argentina label
                } else if (dist <= 3.2) {
                    img.setRGB(x, y, celeste);
                } else {
                    img.setRGB(x, y, ((int)dist % 2 == 0) ? vinyl : groove);
                }
            }
        }
        ImageIO.write(img, "PNG", new File(path));
    }
}
