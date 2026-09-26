package com.villaargentina;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class LogoGen {
    public static void main(String[] args) throws Exception {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Background: Light blue and white Argentine stripes
        g.setColor(new Color(117, 170, 219));
        g.fillRect(0, 0, 64, 21);
        g.setColor(Color.WHITE);
        g.fillRect(0, 21, 64, 22);
        g.setColor(new Color(117, 170, 219));
        g.fillRect(0, 43, 64, 21);

        // Sun of May in center
        g.setColor(new Color(255, 184, 28));
        g.fillOval(26, 26, 12, 12);

        // Draw a casilla silhouette / roof in dark gray/orange at bottom
        g.setColor(new Color(175, 70, 30));
        g.fillRect(8, 48, 20, 16);
        g.setColor(new Color(160, 160, 160));
        g.fillRect(6, 44, 24, 4); // Chapa roof

        // Rotoplas water tank (blue) on roof
        g.setColor(new Color(13, 110, 253));
        g.fillRect(20, 36, 8, 8);

        // Border
        g.setColor(new Color(40, 40, 40));
        g.drawRect(0, 0, 63, 63);

        g.dispose();
        ImageIO.write(img, "PNG", new File("src/main/resources/logo.png"));
        System.out.println("Logo generated!");
    }
}
