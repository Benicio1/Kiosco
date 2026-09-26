package com.escuela;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TextureGenerator {
    public static void main(String[] args) throws Exception {
        String baseDir = args.length > 0 ? args[0] : "src/main/resources/assets/escuela/textures";
        File blockDir = new File(baseDir + "/block");
        File itemDir = new File(baseDir + "/item");
        blockDir.mkdirs();
        itemDir.mkdirs();

        genPizarron(new File(blockDir, "pizarron.png"));
        genPupitre(new File(blockDir, "pupitre.png"));
        genBaldosa(new File(blockDir, "baldosa_escolar.png"));
        genPared(new File(blockDir, "pared_escolar.png"));
        genLineaCancha(new File(blockDir, "linea_cancha.png"));
        genGeneradorItem(new File(itemDir, "generador_escuela.png"));

        System.out.println("School textures created successfully!");
    }

    // 1. Pizarrón Escolar (Classic dark green chalkboard with wood frame & chalk writing)
    private static void genPizarron(File out) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Color frame = new Color(0x9a, 0x68, 0x36);
        Color frameDark = new Color(0x6b, 0x43, 0x1f);
        Color board = new Color(0x23, 0x3d, 0x2e);
        Color boardDark = new Color(0x1d, 0x33, 0x26);
        Color chalk = new Color(0xee, 0xf2, 0xeb);
        Color chalkYellow = new Color(0xf6, 0xdd, 0x82);

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (x == 0 || x == 15 || y == 0 || y == 15) {
                    img.setRGB(x, y, (x + y) % 2 == 0 ? frame.getRGB() : frameDark.getRGB());
                } else {
                    int c = ((x * 7 + y * 13) % 5 == 0) ? boardDark.getRGB() : board.getRGB();
                    img.setRGB(x, y, c);
                }
            }
        }
        // Chalk writing: "ABC" and "1+2=3"
        // 'A' at x=2..4, y=3..5
        img.setRGB(3, 3, chalk.getRGB());
        img.setRGB(2, 4, chalk.getRGB()); img.setRGB(4, 4, chalk.getRGB());
        img.setRGB(2, 5, chalk.getRGB()); img.setRGB(3, 5, chalk.getRGB()); img.setRGB(4, 5, chalk.getRGB());
        
        // 'B' at x=6..8, y=3..5
        img.setRGB(6, 3, chalk.getRGB()); img.setRGB(7, 3, chalk.getRGB());
        img.setRGB(6, 4, chalk.getRGB()); img.setRGB(8, 4, chalk.getRGB());
        img.setRGB(6, 5, chalk.getRGB()); img.setRGB(7, 5, chalk.getRGB());

        // 'C' at x=10..12, y=3..5
        img.setRGB(11, 3, chalk.getRGB()); img.setRGB(12, 3, chalk.getRGB());
        img.setRGB(10, 4, chalk.getRGB());
        img.setRGB(11, 5, chalk.getRGB()); img.setRGB(12, 5, chalk.getRGB());

        // Math: "2+2=4" at y=8..10
        img.setRGB(3, 8, chalkYellow.getRGB()); img.setRGB(4, 9, chalkYellow.getRGB()); img.setRGB(3, 10, chalkYellow.getRGB()); // '2'
        img.setRGB(6, 9, chalk.getRGB()); img.setRGB(7, 8, chalk.getRGB()); img.setRGB(7, 9, chalk.getRGB()); img.setRGB(7, 10, chalk.getRGB()); img.setRGB(8, 9, chalk.getRGB()); // '+'
        img.setRGB(10, 8, chalkYellow.getRGB()); img.setRGB(11, 9, chalkYellow.getRGB()); img.setRGB(10, 10, chalkYellow.getRGB()); // '2'
        img.setRGB(13, 8, chalk.getRGB()); img.setRGB(13, 10, chalk.getRGB()); // '='

        // Chalk tray & eraser at bottom frame
        img.setRGB(4, 14, 0xffffffff); img.setRGB(5, 14, 0xffffffff); // piece of chalk
        img.setRGB(9, 14, 0xff5c4033); img.setRGB(10, 14, 0xff8b5a2b); // eraser

        ImageIO.write(img, "png", out);
    }

    // 2. Pupitre Escolar (School desk)
    private static void genPupitre(File out) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Color wood = new Color(0xd4, 0xa3, 0x6a);
        Color woodDark = new Color(0xb8, 0x86, 0x51);
        Color metalGreen = new Color(0x38, 0x5e, 0x47);
        Color metalDark = new Color(0x23, 0x3d, 0x2e);

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (y < 6) {
                    // Wooden tabletop
                    boolean grain = ((x * 3 + y * 5) % 4 == 0);
                    img.setRGB(x, y, grain ? woodDark.getRGB() : wood.getRGB());
                } else if (y == 6) {
                    // Shadow below desk top
                    img.setRGB(x, y, woodDark.getRGB());
                } else {
                    // Metal legs and shelf
                    if (x == 1 || x == 2 || x == 13 || x == 14) {
                        img.setRGB(x, y, (x % 2 == 0) ? metalGreen.getRGB() : metalDark.getRGB());
                    } else if (y == 9 || y == 10) {
                        // Book basket / shelf under desk
                        img.setRGB(x, y, (x % 2 == 0) ? metalDark.getRGB() : metalGreen.getRGB());
                    } else {
                        img.setRGB(x, y, 0x00000000); // transparent between legs
                    }
                }
            }
        }
        // Pencil groove in the desk top
        for (int x = 3; x <= 12; x++) {
            img.setRGB(x, 1, woodDark.getRGB());
        }
        // Pencil in the groove
        img.setRGB(6, 1, 0xffe63946); img.setRGB(7, 1, 0xffe63946); img.setRGB(8, 1, 0xffffbe0b);

        ImageIO.write(img, "png", out);
    }

    // 3. Baldosa Escolar (Classic granite/terrazzo school hallway and classroom floor tile)
    private static void genBaldosa(File out) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Color base = new Color(0xd6, 0xd3, 0xcc);
        Color baseDark = new Color(0xc8, 0xc4, 0xbc);
        Color grout = new Color(0x94, 0x8f, 0x86);
        Color chipDark = new Color(0x52, 0x4e, 0x48);
        Color chipLight = new Color(0xfa, 0xf9, 0xf5);
        Color chipAmber = new Color(0xba, 0x8f, 0x68);

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                // 2x2 large tile grid with grout lines at x=0, x=8, y=0, y=8
                if (x == 0 || x == 8 || y == 0 || y == 8) {
                    img.setRGB(x, y, grout.getRGB());
                } else {
                    int hash = (x * 37 + y * 73) ^ (x * y);
                    if ((hash & 15) == 0) img.setRGB(x, y, chipDark.getRGB());
                    else if ((hash & 15) == 1) img.setRGB(x, y, chipLight.getRGB());
                    else if ((hash & 15) == 2) img.setRGB(x, y, chipAmber.getRGB());
                    else if ((hash & 3) == 0) img.setRGB(x, y, baseDark.getRGB());
                    else img.setRGB(x, y, base.getRGB());
                }
            }
        }
        ImageIO.write(img, "png", out);
    }

    // 4. Pared Escolar (Institutional two-tone wall: cream upper plaster, dark wood trim, institutional green wainscot)
    private static void genPared(File out) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Color plasterLight = new Color(0xf0, 0xee, 0xe4);
        Color plasterDark = new Color(0xe2, 0xde, 0xd2);
        Color woodTrimLight = new Color(0x8a, 0x5a, 0x30);
        Color woodTrimDark = new Color(0x5c, 0x39, 0x1b);
        Color wainscotLight = new Color(0x3e, 0x6b, 0x54);
        Color wainscotDark = new Color(0x2d, 0x52, 0x40);

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (y < 9) {
                    // Upper cream plaster with subtle plaster grain
                    boolean noise = ((x * 5 + y * 11) % 3 == 0);
                    img.setRGB(x, y, noise ? plasterDark.getRGB() : plasterLight.getRGB());
                } else if (y == 9) {
                    // Wooden chair rail molding
                    img.setRGB(x, y, ((x + y) % 2 == 0) ? woodTrimLight.getRGB() : woodTrimDark.getRGB());
                } else {
                    // Lower institutional green zócalo (washable paint)
                    boolean noise = ((x * 7 + y * 13) % 4 == 0);
                    img.setRGB(x, y, noise ? wainscotDark.getRGB() : wainscotLight.getRGB());
                }
            }
        }
        ImageIO.write(img, "png", out);
    }

    // 5. Línea de Cancha (Crisp sports line marking for the court)
    private static void genLineaCancha(File out) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Color surface = new Color(0x35, 0x6e, 0x52); // green athletic court coating
        Color surfaceNoise = new Color(0x2e, 0x61, 0x48);
        Color lineWhite = new Color(0xf4, 0xf6, 0xf0);
        Color lineEdge = new Color(0xcc, 0xd0, 0xc5);

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                // Line across middle (x=6..9)
                if (x == 7 || x == 8) {
                    img.setRGB(x, y, lineWhite.getRGB());
                } else if (x == 6 || x == 9) {
                    img.setRGB(x, y, lineEdge.getRGB());
                } else {
                    boolean n = ((x * 9 + y * 17) % 3 == 0);
                    img.setRGB(x, y, n ? surfaceNoise.getRGB() : surface.getRGB());
                }
            }
        }
        ImageIO.write(img, "png", out);
    }

    // 6. Generador de Escuela (Architectural blueprint item)
    private static void genGeneradorItem(File out) throws Exception {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Color blueDark = new Color(0x1a, 0x36, 0x5d);
        Color blue = new Color(0x2b, 0x6c, 0xb0);
        Color blueLight = new Color(0x42, 0x99, 0xe1);
        Color line = new Color(0xeb, 0xf8, 0xff);
        Color compassGold = new Color(0xf6, 0xe0, 0x5e);

        // Blueprint sheet rotated slightly or straight 14x14
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (x >= 1 && x <= 14 && y >= 1 && y <= 14) {
                    img.setRGB(x, y, ((x + y) % 5 == 0) ? blueLight.getRGB() : blue.getRGB());
                } else {
                    img.setRGB(x, y, 0x00000000);
                }
            }
        }
        // Border
        for (int x = 1; x <= 14; x++) {
            img.setRGB(x, 1, blueDark.getRGB());
            img.setRGB(x, 14, blueDark.getRGB());
        }
        for (int y = 1; y <= 14; y++) {
            img.setRGB(1, y, blueDark.getRGB());
            img.setRGB(14, y, blueDark.getRGB());
        }

        // Architectural school sketch in white lines:
        // Entrance rotunda dome:
        img.setRGB(7, 3, line.getRGB()); img.setRGB(8, 3, line.getRGB());
        img.setRGB(6, 4, line.getRGB()); img.setRGB(9, 4, line.getRGB());
        // School roof line:
        for (int x = 3; x <= 12; x++) img.setRGB(x, 5, line.getRGB());
        // School wall line:
        for (int x = 3; x <= 12; x++) img.setRGB(x, 9, line.getRGB());
        // Columns / doors:
        img.setRGB(3, 6, line.getRGB()); img.setRGB(3, 7, line.getRGB()); img.setRGB(3, 8, line.getRGB());
        img.setRGB(12, 6, line.getRGB()); img.setRGB(12, 7, line.getRGB()); img.setRGB(12, 8, line.getRGB());
        img.setRGB(7, 7, line.getRGB()); img.setRGB(8, 7, line.getRGB()); // front door
        // Sports court markings in blueprint:
        img.setRGB(4, 11, line.getRGB()); img.setRGB(5, 11, line.getRGB()); img.setRGB(6, 11, line.getRGB());
        img.setRGB(5, 12, line.getRGB());

        // Drafting pencil / compass corner:
        img.setRGB(12, 12, compassGold.getRGB());
        img.setRGB(13, 11, compassGold.getRGB());
        img.setRGB(11, 13, compassGold.getRGB());

        ImageIO.write(img, "png", out);
    }
}
