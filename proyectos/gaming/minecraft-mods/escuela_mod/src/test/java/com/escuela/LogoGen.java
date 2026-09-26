package com.escuela;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class LogoGen {
    public static void main(String[] args) throws Exception {
        int w = 256, h = 128;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background gradient
        g.setColor(new Color(0x1e, 0x3a, 0x5f));
        g.fillRoundRect(8, 8, w - 16, h - 16, 24, 24);

        // Border
        g.setColor(new Color(0xf6, 0xad, 0x55));
        g.setStroke(new java.awt.BasicStroke(4));
        g.drawRoundRect(8, 8, w - 16, h - 16, 24, 24);

        // Text
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        g.drawString("ESCUELA", 46, 56);

        g.setColor(new Color(0x63, 0xb3, 0xed));
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.drawString("Estructura 72m & SUM", 42, 85);

        g.setColor(new Color(0x68, 0xd3, 0x91));
        g.setFont(new Font("SansSerif", Font.ITALIC, 14));
        g.drawString("Forge 1.20.1", 88, 108);

        g.dispose();
        new File("src/main/resources").mkdirs();
        ImageIO.write(img, "png", new File("src/main/resources/logo.png"));
        System.out.println("Logo generated!");
    }
}
