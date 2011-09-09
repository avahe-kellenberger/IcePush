package net.threesided.graphics2d;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Renderer {
	private static final long serialVersionUID = 1L;

	// Drawing stuff
	protected Component canvas;
	protected Image buffer;
	protected Graphics bg;
	protected int width, height;

	// Fonts
	protected Font titleFont = new Font("Arial", Font.PLAIN, 20);
	protected Font debugFont = new Font(Font.DIALOG, Font.PLAIN, 9);
	protected Font namesFont = new Font(Font.DIALOG, Font.PLAIN, 12);


	// Chat stuff
	public static final Color chatsBoxColor = new Color(0, 0, 0, 150);
	public static boolean chats_visible;
	public static String curChat = "";
	public static ArrayList<String> chats = new ArrayList<String>();
	public static Font chatsFont;
	public static BufferedImage font;
	// Set the font
	static {
		try {
			font = SpriteLoader.getSprite("data/font.png");
			chatsFont = Font.createFont(Font.TRUETYPE_FONT,
					Renderer.class.getResourceAsStream("/data/dina.ttf")).deriveFont(15.5f);
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

	public Image getBuffer() {
		return buffer;
	}

	public Graphics getBufferGraphics() {
		return bg;
	}
}
