package com.glgames.graphics2d;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;

public class Renderer {
	private static final long serialVersionUID = 1L;

	public static final int SOFTWARE_2D = 0;
	public static final int SOFTWARE_3D = 1;
	public static final int HARDWARE_3D = 2;
	public static int GRAPHICS_MODE = SOFTWARE_2D;

	public static String message = "Select a server and username.";

	public int selectedButton;
	public int mouseOverButton;

	protected Component canvas;
	public Image backbuffer;
	protected Graphics outgfx;
	protected Graphics bg;
	protected MemoryImageSource memsrc;
	protected Image memimg;

	protected Font titleFont = new Font("Arial", Font.PLAIN, 20);
	protected Font deathsBoxFont = new Font("Arial", Font.PLAIN, 24);
	protected Font debugFont = new Font(Font.DIALOG, Font.PLAIN, 9);
	protected Font diedScreenFont = new Font("Arial Black", Font.PLAIN, 36);
	protected Font namesFont = new Font(Font.DIALOG, Font.PLAIN, 12);

	protected int width, height, pixels[];
	int scaledWidth;

	public Renderer(Component c, int w, int h) {
		canvas = c;
		//System.out.println("width: " + c.getWidth() + " height: " + c.getHeight());

		width = w;
		height = h;
		scaledWidth = width << 12;
	}

	public void initGraphics() {
		pixels = new int[width * height];
		backbuffer = canvas.createImage(width, height);
		memsrc = new MemoryImageSource(width, height, pixels, 0, width);
		memsrc.setAnimated(true);
		memimg = canvas.createImage(memsrc);
		bg = backbuffer.getGraphics();
		outgfx = canvas.getGraphics();
		canvas.requestFocus();
	}

	public Component getCanvas() {
		return canvas;
	}
	
	public static boolean chats_visible;
	public static String curChat = "";
	public static ArrayList<String> chats = new ArrayList<String>();
	//private int count;
	
	/*private void drawChats() {
		String chat;

		bg.setFont(debugFont);
		for(int k = 0; k < chats.size(); k++) {
			chat = chats.get(k);
			bg.drawString(chat, 35, 430 - (chats.size() - k) * 15);
		}
		
		bg.drawString(curChat, 35 + 3, 440);
		if(count++ % 50 > 25) {
			int width = bg.getFontMetrics().stringWidth(curChat) + 5;
			bg.drawLine(35 + width, 440 - 8, 35 + width, 440);
		}
	}*/
	
	public static Font chatsFont;
	public static BufferedImage font;

	static {
		try {
			font = SpriteLoader.getSprite("data/font.png");
			chatsFont = Font.createFont(Font.TRUETYPE_FONT,
					Renderer.class.getResourceAsStream("/data/dina.ttf")).deriveFont(15.0f);
		} catch (Exception e) {
			e.printStackTrace();
			chatsFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		}
	}
	public static final Color chatsBoxColor = new Color(0, 0, 0, 150);
	
	/* these are Bad and Use lots of cpu due to bufferedImage */
	/*private void drawString(String s, int x, int y) {
		for(int k = 0; k < s.length(); k++) {
			char c = s.charAt(k);
			drawCharacter(c, x, y);
			x += 7;
		}
	}
	
	private void drawCharacter(char c, int x, int y) {
		int index = 0;
		if(c < 32)
			return;
		if(c >= '0' && c <= '9')
			index = c - '0' + 1;
		else if(c >= 'A' && c <= 'Z')
			index = c - 'A' + 11;
		else if(c >= 'a' && c <= 'z')
			index = c - 'a' + 37;
		int srcx = index * 7;
		BufferedImage ch = font.getSubimage(srcx, 0, 7, 12);
		bg.drawImage(ch, x, y, null);
	}*/
	
	/*private void focusCamera() {
		Player p = GameObjects.players[NetworkHandler.id];
		if(p == null)
			return;		
		focusX = focusY = focusZ = 0;
	}*/
	
	public static final int HALF_GAME_FIELD_WIDTH = (744 / 2);
	public static final int HALF_GAME_FIELD_HEIGHT = (422 / 2);

	public void clearScreen() {
		bg.setColor(Color.BLACK);
		bg.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	public void swapBuffers() {
		if (outgfx == null || backbuffer == null)
			return;
		outgfx.drawImage(backbuffer, 0, 0, null);
	}

	public Graphics getBufferGraphics() {
		return bg;
	}

	/**
	 * Sorts a trio of vertices by height so that y1 <= y2 <= y3 Implemented as
	 * its own method with global variables so that triangle code can stay small
	 * (Crucial to fitting within CPU code cache for these presumably highly
	 * performance intensive routines.)
	 */

	private static int _x1, _y1, _x2, _y2, _x3, _y3;

	private static void triSort(int x1, int y1, int x2, int y2, int x3, int y3) {
		_x1 = x1;
		_y1 = y1;
		_x2 = x2;
		_y2 = y2;
		_x3 = x3;
		_y3 = y3;
		int exchx = 0, exchy = 0; // two exchange variables are used to take
		// advantage of potential ILP

		if (_y1 > _y2) { // Each exchange block is dependant on the previous
			// one, so crap
			exchx = _x1;
			exchy = _y1;

			_x1 = _x2;
			_y1 = _y2;

			_x2 = exchx;
			_y2 = exchy;
		}

		if (_y1 > _y3) {
			exchx = _x1;
			exchy = _y1;

			_x1 = _x3;
			_y1 = _y3;

			_x3 = exchx;
			_y3 = exchy;
		}

		if (_y2 > _y3) {
			exchx = _x2;
			exchy = _y2;

			_x2 = _x3;
			_y2 = _y3;

			_x3 = exchx;
			_y3 = exchy;
		}

	}

	public void solidTriangle(int X1, int Y1, int X2, int Y2, int X3,
			int Y3, int color) {
		triSort(X1, Y1, X2, Y2, X3, Y3);
		if (_y3 == _y1)
			return;

		int step31 = scaledWidth + ((_x3 - _x1) << 12) / (_y3 - _y1);

		int right = _y1 * scaledWidth, left = right;
		int boundLeft = _y1 * width, boundRight = boundLeft + width;

		if (_y1 != _y2) {
			right += _x1 << 12;
			left = right;
			int step21 = scaledWidth + ((_x2 - _x1) << 12) / (_y2 - _y1);
			// Top part, triangle is broadening. stepLeft < stepRight
			int stepLeft = 0, stepRight = 0;
			if (step21 > step31) {
				stepLeft = step31;
				stepRight = step21;
			} else {
				stepLeft = step21;
				stepRight = step31;
			}
			while (_y1 != _y2) {
				if (_y1 >= 0 && _y1 < height) {
					int l = left >> 12;
					int r = right >> 12;

					if (l < boundLeft)
						l = boundLeft;
					if (r > boundRight)
						r = boundRight;

					while (l < r)
						pixels[l++] = color;
				}
				left += stepLeft;
				right += stepRight;
				boundLeft = boundRight;
				boundRight += width;
				_y1++;
			}
		} else {
			// Triangle is flat topped; adjust left & right accordingly + skip
			// filling top half
			if (_x1 > _x2) {
				right += _x1 << 12;
				left += _x2 << 12;
			} else {
				right += _x2 << 12;
				left += _x1 << 12;
			}
		}

		if (_y2 != _y3) {
			int step23 = scaledWidth + ((_x2 - _x3) << 12) / (_y2 - _y3);
			// Bottom part: Triangle is narrowing. stepLeft > stepRight.
			int stepLeft = 0, stepRight = 0;
			if (step23 > step31) {
				stepLeft = step23;
				stepRight = step31;
			} else {
				stepLeft = step31;
				stepRight = step23;
			}
			while (_y2 != _y3) {
				if (_y2 >= 0 && _y2 < height) {
					int l = left >> 12;
					int r = right >> 12;

					if (l < boundLeft)
						l = boundLeft;
					if (r > boundRight)
						r = boundRight;

					while (l < r)
						pixels[l++] = color;
				}

				left += stepLeft;
				right += stepRight;
				boundLeft = boundRight;
				boundRight += width;
				_y2++;
			}
		}

	//	if(X1 >= 0 && X1 < width && Y1 >= 0 && Y1 < height) pixels[X1 + Y1 * width] = 0xffffffff;
	//	if(X2 >= 0 && X2 < width && Y2 >= 0 && Y2 < height) pixels[X2 + Y2 * width] = 0xffffffff;
	//	if(X3 >= 0 && X3 < width && Y3 >= 0 && Y3 < height) pixels[X3 + Y3 * width] = 0xffffffff;

	}

	public void clear() {
		int i = width * height;
		for (int j = 0; j < i; j++)
			pixels[j] = 0;

	}

	// THESE STUBS WILL ALL BE REMOVED AS MORE OF THE 2D GRAPHICS LIBRARY IS WRITTEN AND USAGE IS STANDARDIZED WITHIN THE COMPONENT CLASSES

	public void setColor(Color c) {
		bg.setColor(c);
	}

	public void fill3DRect(int a, int b, int c, int d, boolean e) {
		bg.fill3DRect(a, b, c, d, e);
	}

	public void drawString(String a, int b, int c) {
		bg.drawString(a, b, c);
	}

	public int getFontHeight() {
		return bg.getFontMetrics().getHeight();
	}

	public int stringWidth(String s) {
		return bg.getFontMetrics().stringWidth(s);
	}

	public void fillRect(int a, int b, int c, int d) {
		bg.fillRect(a, b, c, d);
	}

	public void drawLine(int a, int b, int c, int d) {
		bg.drawLine(a, b, c, d);
	}
}
