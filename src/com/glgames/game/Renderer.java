package com.glgames.game;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

public class Renderer {
	private static final long serialVersionUID = 1L;

	public static String message = "Select a server and username.";

	protected Component canvas;
	protected Image backbuffer;
	protected Graphics outgfx;
	protected Graphics bg;

	public Renderer(Component c) {
		canvas = c;
	}

	public void initGraphics() {
		backbuffer = canvas.createImage(IcePush.WIDTH, IcePush.HEIGHT);
		bg = backbuffer.getGraphics();
		outgfx = canvas.getGraphics();
		canvas.requestFocus();
	}

	public Component getCanvas() {
		return canvas;
	}

	public void drawLoadingBar(String s, int p) {
		int width = 400, height = 30, x = IcePush.WIDTH / 2 - width / 2;

		bg.setColor(Color.cyan);
		bg.drawRect(x, IcePush.HEIGHT / 2 - height / 2, width, height);

		x += 2;
		if (p == -1)
			width -= 2;
		else
			width = (int) (p / 100.0d * width);
		height -= 3;

		if (p == -1)
			bg.setColor(new Color(150, 0, 0));
		else
			bg.setColor(new Color(0, 0, 150));
		bg.fillRect(x, IcePush.HEIGHT / 2 - height / 2, width, height);

		x = IcePush.WIDTH / 2 - bg.getFontMetrics().stringWidth(s) / 2;
		bg.setColor(Color.white);
		bg.drawString(s, x, IcePush.HEIGHT / 2
				- bg.getFontMetrics().getHeight() / 2 + 12);
	}
	
	public void background(int cycle) {
		double frequency = 0.01d;
		int green = (int) (Math.sin(frequency * cycle % 32 + 2) * 63 + 160);
		int blue = (int) (Math.sin(frequency * cycle % 32 + 4) * 63 + 160);

		Color col = new Color(0, green, blue);
		bg.setColor(col);
		bg.fillRect(0, 0, IcePush.WIDTH, IcePush.HEIGHT);
		bg.drawImage(GameObjects.logo, 50, 50, null);

	}

	public void drawWelcomeScreen(int cycle) {
		background(cycle);
		int w;
		bg.setColor(Color.white);
		bg.setFont(new Font("Arial", Font.PLAIN, 20));

		int y = 190;
		for (String s : GameObjects.instructions) {
			w = bg.getFontMetrics().stringWidth(s);
			bg.drawString(s, IcePush.WIDTH / 2 - w / 2, y += 30);
		}

		bg.setColor(Color.white);
		w = bg.getFontMetrics().stringWidth(message);
		bg.drawString(message, IcePush.WIDTH / 2 - w / 2, 310);

		if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX) {
			GameObjects.serverBox.draw(bg);
		} else if(GameObjects.serverMode == GameObjects.LIST_FROM_SERVER) {
			GameObjects.serverList.draw(bg);
		}
		GameObjects.usernameBox.draw(bg);

		button(GameObjects.loginButton, "Login");
		button(GameObjects.helpButton, "Help");
	}
	
	private void button(Rectangle r, String text) {
		bg.setColor(Color.gray);
		bg.fill3DRect(r.x, r.y, r.width, r.height, true);
		bg.setColor(Color.white);
		int w = bg.getFontMetrics().stringWidth(text);
		bg.drawString(text, r.x + r.width / 2 - w / 2, r.y + 18);
	}

	public void drawHelpScreen(int cycle) {
		background(cycle);
		int w;
		bg.setColor(Color.white);
		bg.setFont(new Font("Arial", Font.PLAIN, 20));

		int y = 190;
		for (String s : GameObjects.help) {
			w = bg.getFontMetrics().stringWidth(s);
			bg.drawString(s, IcePush.WIDTH / 2 - w / 2, y += 30);
		}

		button(GameObjects.backButton, "Back");
	}
	
	public void renderScene(Object2D[] objects) {
		Graphics2D g = (Graphics2D) bg;
		if(g == null)
			return;
		Rectangle rect = GameObjects.playingArea;
		g.setPaint(GameObjects.background);
		g.fillRect(0, 0, IcePush.WIDTH, IcePush.HEIGHT);
		g.setPaint(GameObjects.foreground);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		for (int k = 0; k < objects.length; k++) {
			Player2D p = (Player2D) objects[k];
			if (p == null)
				continue;
			p.draw(g);
		}

		g.setColor(Color.white);
		g.setFont(new Font("Arial", Font.PLAIN, 24));
		int x = 30, y = 480;
		g.drawString("Deaths", x, y);
		g.drawRect(x, y += 5, 400, 100);
		for (int k = 0; k < objects.length; k++) {
			if (objects[k] == null || !(objects[k] instanceof Player2D))
				continue;
			Player2D plr = (Player2D) objects[k];

			g.drawString(plr.username + " - " + plr.deaths, x + 15, y += 20);
		}
	}


	public void drawDiedScreen(int l) {
		int alpha = (int) ((l / 50.0d) * 255.0d);
		bg.setColor(new Color(255, 255, 255, alpha));
		bg.fillRect(0, 0, IcePush.WIDTH, IcePush.HEIGHT);
		((Graphics2D) bg).setPaint(new GradientPaint(200, 200, new Color(0,
				255, 0), 400, 400, new Color(0, 0, 255)));
		bg.setFont(new Font("Arial Black", Font.PLAIN, 36));
		bg.drawString("TRY AGAIN", IcePush.WIDTH / 2 - 110,
				IcePush.HEIGHT / 2);
	}

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
}
