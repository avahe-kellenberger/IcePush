package net.threesided.test;

import static java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK;
import static java.awt.AWTEvent.WINDOW_EVENT_MASK;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.io.FileInputStream;
import javax.imageio.ImageIO;
import net.threesided.graphics2d.Bitmap;

public class BitmapTest extends Frame {

    // public static final int TEST_BLT = 0;
    // public static final int TEST_FONT = 1;

    int insx, insy;

    int pixels[];
    int width, height;

    Image bbuf;
    MemoryImageSource imgsrc;
    Bitmap background;
    Bitmap sprite;
    Bitmap font;

    int foreGround = 0xffff00;
    // int backGround;

    public static void main(String args[]) throws Exception {
        new BitmapTest();
    }

    private BitmapTest() throws Exception {
        super("Bitmap Test");
        enableEvents(WINDOW_EVENT_MASK | MOUSE_MOTION_EVENT_MASK);

        width = 800;
        height = 480;
        pixels = new int[width * height];

        for (int i = 0; i < pixels.length; i++) pixels[i] = 0xffff00ff;

        sprite = new Bitmap(ImageIO.read(new FileInputStream("src/images/snowman.png")));
        background = new Bitmap(ImageIO.read(new FileInputStream("src/images/icepush.png")));
        font = new Bitmap(ImageIO.read(new FileInputStream("src/data/font.png")));

        trimSprite(sprite, 0xff00ff);

        // drawBitmap(background, 0, 0, 0);

        setVisible(true);
        setResizable(false);
        validate(); // THIS MAKES IT COMPUTE PROPER INSETS
        Insets ins = getInsets();
        insx = ins.left;
        insy = ins.top;
        setSize(insx + width, insy + ins.bottom + height);
        imgsrc =
                new MemoryImageSource(
                        width,
                        height,
                        new DirectColorModel(32, 0xff0000, 0xff00, 0xff),
                        pixels,
                        0,
                        width);
        imgsrc.setAnimated(true);
        bbuf = createImage(imgsrc);
        drawBitmap(sprite, 200, 200, 0);
        imgsrc.newPixels();

        repaint();
    }

    void trimSprite(Bitmap b, int bg) {
        for (int i = 0; i < b.pixels.length; i++)
            if ((b.pixels[i] & 0xff000000) != 0xff000000) b.pixels[i] = bg;
    }

    public void processWindowEvent(WindowEvent we) {
        if (we.getID() == WindowEvent.WINDOW_CLOSING) dispose();
    }

    public void paint(Graphics g) {
        g.drawImage(bbuf, insx, insy, null);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void processMouseMotionEvent(MouseEvent mme) {
        Graphics g = getGraphics();
        int x = mme.getX() - insx, y = mme.getY() - insy;
        drawBitmap(background, 0, 0, 0xff000000);
        drawBitmap(sprite, x - sprite.width / 2, y - sprite.height / 2, 0xff00ff);
        String str = "234567ThisIS A TEST string";
        int w = stringWidth(str, font);
        int h = stringHeight(font);
        drawString(font, x - w / 2, y - h / 2, str);
        fill3DRect(x - 20, y - 20, 40, 40);
        imgsrc.newPixels();
        g.drawImage(bbuf, insx, insy, null);
    }

    /*Bitmap genSprite() {
    	int[] texture = new int[4096];
    	for(int i = 0; i < 64; i++) {
    		for(int j = 0; j < 64; j++) {
    			texture[i + j*64] =

               		//(i << 17) + (j << 9) + 0xf;
            			((i / 8) + (j / 8)) % 2 == 0 ? 0x8fff0000 : 0x8fffffff;   // Comment out the line about and uncomment this one to generate red/white checkerboard pattern

    			//if(i == j) texture[i + j*64] = 0xff;     // Creates blue line of pixels down diagonal to help visually verify correct perspective mapping
    		}
    	}
    	return new Bitmap(64, 64, texture);
    }*/

    public int stringWidth(String str, Bitmap font) {
        return str.length() * font.width / 95;
    }

    public int stringHeight(Bitmap font) {
        return font.height;
    }

    public int getIndex(char c) {
        if (c >= '0' && c <= '9') {
            return c - ('0' - 1);
        } else if (c >= 'A' && c <= 'Z') {
            return c - ('A' - 11);
        } else if (c >= 'a' && c < 'z') {
            return c - ('a' - 37);
        }
        return 0;
    }

    // Bitmap src, Bitmap dest, int src_x, int src_y, int dest_x, int dest_y, int count_x, int
    // count_y, int bg) {

    public void drawString(Bitmap font, int x, int y, String str) {
        int charWidth = font.width / 95;
        Bitmap b = new Bitmap(width, height, pixels);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            copy(
                    font,
                    b,
                    charWidth * getIndex(c),
                    0,
                    x + i * charWidth,
                    y,
                    charWidth,
                    font.height,
                    0);
        }
    }

    public void drawBitmap(Bitmap b, int x, int y, int bgColor) {
        int xpix = b.width; // Number of pixels to be drawn in the X direction
        int ypix = b.height; // Number of pixels in Y

        int bitx = 0; // start index
        int bity = 0;

        if (x < 0) {
            bitx = -x;
            xpix += x;
            x = 0;
        }
        if (y < 0) {
            bity = -y;
            ypix += y;
            y = 0;
        }

        if (x + b.width > width) {
            xpix = width - x;
        }

        if (y + b.height > height) {
            ypix = height - y;
        }

        int bitPos = bitx + bity * b.width;
        int pixPos = x + y * width;

        int bitStep = b.width - xpix;
        int pixStep = width - xpix;

        for (int j = 0; j < ypix; j++) {
            for (int i = 0; i < xpix; i++) {
                int p = b.pixels[bitPos++];
                pixPos++;
                if (p == bgColor) continue;
                pixels[pixPos - 1] = p;
            }
            bitPos += bitStep;
            pixPos += pixStep;
        }
    }

    // Note: all src coords are assumed to be properly bounded!
    public void copy(
            Bitmap src,
            Bitmap dest,
            int src_x,
            int src_y,
            int dest_x,
            int dest_y,
            int count_x,
            int count_y,
            int bg) {
        // System.out.println("1 src_x=" + src_x + " src_y=" + src_y);
        // System.out.println("2 " + src.pixels.length);

        if (dest_x < 0) {
            src_x -= dest_x;
            count_x += dest_x;
            dest_x = 0;
        }

        if (dest_y < 0) {
            src_y -= dest_y;
            count_y += dest_y;
            dest_y = 0;
        }

        if (dest_x + count_x > dest.width) {
            count_x = dest.width - dest_x;
        }

        if (dest_y + count_y > dest.height) {
            count_y = dest.height - dest_y;
        }

        int src_index = src_x + src_y * src.width;
        int dest_index = dest_x + dest_y * dest.width;

        int src_step = src.width - count_x;
        int dest_step = dest.width - count_x;

        // System.out.println("3 src_index=" + src_index + " src_x=" + src_x + " src_y="+src_y + "
        // src.width="+src.width);
        // System.out.println("4 count_x=" + count_x + " count_y=" + count_y);
        // System.out.println("5 src_step=" + src_step + " dest_step=" + dest_step);

        for (int j = 0; j < count_y; j++) {
            for (int i = 0; i < count_x; i++) {
                int pixel = src.pixels[src_index++];
                dest_index++;
                if (pixel != bg) dest.pixels[dest_index - 1] = foreGround;
            }
            src_index += src_step;
            dest_index += dest_step;
        }
    }

    public void fillRect(int x, int y, int w, int h) {
        if (x >= width || y >= height) return;
        if (x < 0) {
            w += x;
            x = 0;
        }
        if (y < 0) {
            h += y;
            y = 0;
        }

        if (x + w > width) {
            w = width - x;
        }

        if (y + h > height) {
            h = height - y;
        }

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                pixels[(i + x) + (j + y) * width] = foreGround;
            }
        }
    }

    public void fill3DRect(int x, int y, int w, int h) {
        if (w < 3 || h < 3) return;
        int r = (foreGround >> 16) & 0xff;
        int g = (foreGround >> 8) & 0xff;
        int b = foreGround & 0xff;

        int bright = (((r + 255) >> 1) << 16) + (((g + 255) >> 1) << 8) + ((b + 255) >> 1);
        int darker = ((r >> 1) << 16) + ((g >> 1) << 8) + (b >> 1);

        int fg = foreGround;
        foreGround = bright;
        horzLine(x, x + w, y); // Top
        vertLine(x, y, y + h); // Left
        foreGround = darker;
        horzLine(x, x + w, y + h); // Bottom
        vertLine(x + w, y, y + h); // Right
        foreGround = fg;
        fillRect(x + 1, y + 1, w - 1, h - 1);
    }

    public void horzLine(int x1, int x2, int y) {
        if (y < 0 || y >= height) return;
        int left = x1 < x2 ? x1 : x2;
        int right = x1 < x2 ? x2 : x1;
        if (left < 0) left = 0;
        if (right > width) right = width;

        int pos = left + y * width;

        for (int i = left; i < right; i++) pixels[pos++] = foreGround;
    }

    public void vertLine(int x, int y1, int y2) {
        if (x < 0 || x >= width) return;
        int top = y1 < y2 ? y1 : y2;
        int bottom = y1 < y2 ? y2 : y1;
        if (top < 0) top = 0;
        if (bottom > height) bottom = height;

        int pos = x + top * width;

        for (int i = top; i < bottom; i++) {
            pixels[pos] = foreGround;
            pos += width;
        }
    }
}
