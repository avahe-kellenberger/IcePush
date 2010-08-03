package com.glgames.test;

import static java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK;
import static java.awt.AWTEvent.WINDOW_EVENT_MASK;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;
import java.io.File;

import javax.imageio.ImageIO;
import java.io.FileInputStream;

import com.glgames.game.Bitmap;

public class BitmapTest extends Frame {

	int insx, insy;

	int pixels[];
	int width, height;

	Image bbuf;
	MemoryImageSource imgsrc;
	Bitmap background;
	Bitmap sprite;
	MonospaceFont font;

	public static void main(String args[]) throws Exception {
		new BitmapTest();
	}

	private BitmapTest() throws Exception {
		super("Bitmap Test");
		enableEvents(WINDOW_EVENT_MASK | MOUSE_MOTION_EVENT_MASK);

		width = 800;
		height = 480;
		pixels = new int[width*height];

		for(int i = 0; i < pixels.length; i++) pixels[i] = 0xffff00ff;

		sprite = genSprite();//new Bitmap(ImageIO.read(new FileInputStream("images/tree.png")));
		background = new Bitmap(ImageIO.read(new FileInputStream("images/icepush.png")));
		font = new MonospaceFont(ImageIO.read(new FileInputStream("data/font.png")));
		
		//drawBitmap(background, 0, 0, 0);


		setVisible(true);
		setResizable(false);
		validate();					// THIS MAKES IT COMPUTE PROPER INSETS
		Insets ins = getInsets();
		insx = ins.left;
		insy = ins.top;
		setSize(insx + width, insy + ins.bottom + height);
		imgsrc = new MemoryImageSource(width, height, pixels, 0, width);
		imgsrc.setAnimated(true);
		bbuf = createImage(imgsrc);
		drawBitmap(sprite, 200, 200, 0);
		imgsrc.newPixels();

		repaint();

   	}

	public void processWindowEvent(WindowEvent we) {
		if(we.getID() == we.WINDOW_CLOSING) dispose();
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
		String str = "tHiS Is a TeSt StRiNg";
		//int w = stringWidth(str);
		//int h = stringHeight();
		//drawString(str, x - w / 2, y - h / 2);
		g.drawImage(bbuf, insx, insy, null);
	}

	Bitmap genSprite() {
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
	}


	public void drawBitmap(Bitmap b, int x, int y, int bgColor) {
		int xpix = b.width;			// Number of pixels to be drawn in the X direction
		int ypix = b.height;			// Number of pixels in Y

		int bitx = 0;				// start index
		int bity = 0;

		if(x < 0) {
			bitx = -x;
			xpix += x;
			x = 0;
		}
		if(y < 0) {
			bity = -y;
			ypix += y;
			y = 0;
		}

		if(x + b.width > width) {
			xpix = width - x;
		}

		if(y + b.height > height) {
			ypix = height - y;
		}

		int bitPos = bitx + bity * b.width;
		int pixPos = x + y * width;

		int bitStep = b.width - xpix;
		int pixStep = width - xpix;

		for(int j = 0; j < ypix; j++) {
			for(int i = 0; i < xpix; i++) {
				int p = b.pixels[bitPos++];
				pixPos++;
				if(p == bgColor) continue;
				pixels[pixPos - 1] = p;
			}
			bitPos += bitStep;
			pixPos += pixStep;
		}
	}

	// Note: all src coords are assumed to be properly bounded!
	public void copy(Bitmap src, Bitmap dest, int src_x, int src_y, int dest_x, int dest_y, int count_x, int count_y, int bg) {

		if(dest_x < 0) {
			src_x -= dest_x;
			count_x += dest_x;
			dest_x = 0;
		}

		if(dest_y < 0) {
			src_y -= dest_y;
			count_x += dest_y;
			dest_y = 0;
		}

		if(dest_x + count_x > dest.width) {
			count_x = dest.width - dest_x;
		}

		if(dest_y + count_y > dest.height) {
			count_y = dest.height - dest_y;
		}

		int src_index = src_x + src_y * src.width;
		int dest_index = dest_x + dest_y * dest.width;

		int src_step = src.width - count_x;
		int dest_step = dest.width - count_x;

		for(int j = 0; j < count_y; j++) {
			for(int i = 0; i < count_x; i++) {
				int pixel = src.pixels[src_index++];
				dest_index++;
				if(pixel != bg) dest.pixels[dest_index - 1] = pixel;
			}
			src_index += src_step;
			dest_index += dest_step;
		}
	}
}