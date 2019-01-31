package net.threesided.graphics2d;

import java.awt.image.*;

public class Bitmap {

    public int pixels[];
    public final int width;
    public final int height;

    public Bitmap(BufferedImage b) {
        width = b.getWidth();
        height = b.getHeight();
        pixels = b.getRGB(0, 0, width, height, null, 0, width);
    }

    public Bitmap(int w, int h, int p[]) {
        width = w;
        height = h;
        pixels = p;
    }
}
