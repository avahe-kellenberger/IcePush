package com.glgames.game;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

public abstract class Renderer extends Canvas {
	private static final long serialVersionUID = 1L;

	public static String message = "Select a server and username.";

	protected Image backbuffer;
	protected Graphics outgfx;
	protected Graphics bg;

	public void initGraphics() {
		backbuffer = createImage(getWidth(), getHeight());
		outgfx = getGraphics();
		bg = backbuffer.getGraphics();
	}

	public void drawLoadingBar(String s, int p) {
		int width = 400, height = 30, x = GameFrame.WIDTH / 2 - width / 2;

		bg.setColor(Color.cyan);
		bg.drawRect(x, GameFrame.HEIGHT / 2 - height / 2, width, height);

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
		bg.fillRect(x, GameFrame.HEIGHT / 2 - height / 2, width, height);

		x = GameFrame.WIDTH / 2 - bg.getFontMetrics().stringWidth(s) / 2;
		bg.setColor(Color.white);
		bg.drawString(s, x, GameFrame.HEIGHT / 2
				- bg.getFontMetrics().getHeight() / 2 + 12);
	}

	public void drawWelcomeScreen(int cycle) {
		int w;
		double frequency = 0.01d;
		int green = (int) (Math.sin(frequency * cycle % 32 + 2) * 63 + 160);
		int blue = (int) (Math.sin(frequency * cycle % 32 + 4) * 63 + 160);

		Color col = new Color(0, green, blue);
		bg.setColor(col);
		bg.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
		bg.drawImage(GameObjects.logo, 50, 50, null);

		bg.setColor(Color.white);
		bg.setFont(new Font("Arial", Font.PLAIN, 20));

		int y = 190;
		for (String s : GameObjects.instructions) {
			w = bg.getFontMetrics().stringWidth(s);
			bg.drawString(s, GameFrame.WIDTH / 2 - w / 2, y += 30);
		}

		bg.setColor(Color.white);
		w = bg.getFontMetrics().stringWidth(message);
		bg.drawString(message, GameFrame.WIDTH / 2 - w / 2, 310);

		if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX)
			GameObjects.serverBox.draw(bg);
		else
			GameObjects.serverList.draw(bg);
		GameObjects.usernameBox.draw(bg);

		Rectangle login = GameObjects.loginButton;
		bg.setColor(Color.gray);
		bg.fill3DRect(login.x, login.y, login.width, login.height, true);
		bg.setColor(Color.white);
		w = bg.getFontMetrics().stringWidth("Login");
		bg.drawString("Login", login.x + login.width / 2 - w / 2, login.y + 18);
	}

	public void drawDiedScreen(int l) {
		int alpha = (int) ((l / 50.0d) * 255.0d);
		bg.setColor(new Color(0, 0, 0, alpha));
		bg.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
		((Graphics2D) bg).setPaint(new GradientPaint(200, 200, new Color(0,
				255, 0), 400, 400, new Color(0, 0, 255)));
		bg.setFont(new Font("Arial Black", Font.PLAIN, 36));
		bg.drawString("TRY AGAIN", GameFrame.WIDTH / 2 - 110,
				GameFrame.HEIGHT / 2);
	}

	public void clearScreen() {
		bg.setColor(Color.BLACK);
		bg.fillRect(0, 0, getWidth(), getHeight());
	}

	public void swapBuffers() {
		outgfx.drawImage(backbuffer, 0, 0, null);
	}

	public Graphics getBufferGraphics() {
		return bg;
	}

	public abstract void renderScene(Object[] stuff);

	public abstract void drawDebug();
}
