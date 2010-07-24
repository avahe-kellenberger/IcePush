package com.glgames.test;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.awt.event.*;
import static java.awt.AWTEvent.*;

import com.glgames.game.Bitmap;

public class BitmapTest extends Frame {

	int insx, insy;

	int pixels[];
	int width, height;

	Image bbuf;
	MemoryImageSource imgsrc;

	public static void main(String args[]) throws InterruptedException {
		new BitmapTest();
	}

	private BitmapTest() {
		super("Bitmap Test");
		enableEvents(WINDOW_EVENT_MASK | MOUSE_MOTION_EVENT_MASK);

		width = height = 512;
		pixels = new int[width*height];

		for(int i = 0; i < pixels.length; i++) pixels[i] = i | 0xff000000;

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
		drawBitmap(sprite, mme.getX() - 32, mme.getY() - 32, 0);
		imgsrc.newPixels();
		repaint();
	}

	Bitmap sprite = genSprite();

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

		//System.out.println("bitx = " + bitx + " bity = " + bity + " x = " + x + " y = " + y + " xpix = " + xpix + " ypix= " + ypix);

		for(int j = 0; j < ypix; j++) {
			for(int i = 0; i < xpix; i++) {
				int p = b.pixels[(bitx + i) + (bity + j)*b.width];
				if(p == bgColor) continue;
				pixels[(x + i) + (j + y)*width] = p;
			}
		}
	}
}