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

	// Drawing stuff
	protected Component canvas;
	public Image backbuffer;
	protected Graphics outgfx;
	protected Graphics bg;

	// Fonts
	protected Font titleFont = new Font("Arial", Font.PLAIN, 20);
	protected Font deathsBoxFont = new Font("Arial", Font.PLAIN, 24);
	protected Font debugFont = new Font(Font.DIALOG, Font.PLAIN, 9);
	protected Font namesFont = new Font(Font.DIALOG, Font.PLAIN, 12);

	protected int width, height;

	// Chat stuff
	public static final Color chatsBoxColor = new Color(0, 0, 0, 150);
	public static boolean chats_visible;
	public static String curChat = "";
	public static ArrayList<String> chats = new ArrayList<String>();
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

	public Renderer(Component c, int w, int h) {
		canvas = c;
		width = w;
		height = h;
	}

	public void initGraphics() {
		buffer = canvas.createImage(width, height);
		bg = buffer.getGraphics();
		canvas.requestFocus();
	}

	public Component getCanvas() {
		return canvas;
	}

	public void clearScreen() {
		bg.setColor(Color.BLACK);
		bg.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	public Graphics getBufferGraphics() {
		return bg;
	}

	// THESE STUBS WILL ALL BE REMOVED AS MORE OF THE 2D GRAPHICS LIBRARY IS WRITTEN AND USAGE IS STANDARDIZED WITHIN THE COMPONENT CLASSES

	public void setColor(Color c) {
		bg.setColor(c);
	}

	public void fill3DRect(int a, int b, int c, int d, boolean e) {
		bg.fill3DRect(a, b, c, d, e);
	}

	public void setFont(Font font) {
		bg.setFont(font);
	}

	public void drawString(String a, int b, int c) {
		bg.drawString(a, b, c);
	}

	public int getFontHeight() {
		return bg.getFontMetrics().getHeight();
	}

	public int getFontDescent() {
		return bg.getFontMetrics().getDescent();
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
