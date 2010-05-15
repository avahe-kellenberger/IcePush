package com.glgames.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class GraphicsMethods {
	public static String message = "Enter a server and username.";

	public static void drawLoadingBar(Graphics g, String s, int p) {
		int width = 400, height = 30, x = GameFrame.WIDTH / 2 - width / 2;

		g.setColor(Color.cyan);
		g.drawRect(x, GameFrame.HEIGHT / 2 - height / 2, width, height);

		x += 2;
		if(p == -1)
			width -= 2;
		else
			width = (int) (p / 100.0d * width);
		height -= 3;

		if(p == -1)
			g.setColor(new Color(150, 0, 0));
		else
			g.setColor(new Color(0, 0, 150));
		g.fillRect(x, GameFrame.HEIGHT / 2 - height / 2, width, height);
		
		x = GameFrame.WIDTH / 2 - g.getFontMetrics().stringWidth(s) / 2;
		g.setColor(Color.white);
		g.drawString(s, x, GameFrame.HEIGHT / 2 - g.getFontMetrics().getHeight() / 2 + 12);
	}

	public static void drawWelcomeScreen(Graphics g, int cycle) {
		int w;
		double frequency = 0.01d;
		int green = (int) (Math.sin(frequency * cycle % 32 + 2) * 63 + 160);
		int blue = (int) (Math.sin(frequency * cycle % 32 + 4) * 63 + 160);
		
		Color col = new Color(0, green, blue);
		g.setColor(col);
		g.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
		g.drawImage(GameObjects.logo, 0, 0, null);

		g.setColor(Color.white);
		g.setFont(new Font("Arial", Font.PLAIN, 20));

		int y = 190;
		for (String s : GameObjects.instructions) {
			w = g.getFontMetrics().stringWidth(s);
			g.drawString(s, GameFrame.WIDTH / 2 - w / 2, y += 30);
		}

		g.setColor(Color.white);
		w = g.getFontMetrics().stringWidth(message);
		g.drawString(message, GameFrame.WIDTH / 2 - w / 2, 430);

		GameObjects.serverBox.draw(g);
		GameObjects.usernameBox.draw(g);

		Rectangle login = GameObjects.loginButton;
		g.setColor(Color.green);
		g.drawRect(login.x, login.y, login.width, login.height);
		g.setColor(Color.white);
		w = g.getFontMetrics().stringWidth("Login");
		g.drawString("Login", login.x + login.width / 2 - w / 2, login.y + 18);
	}

	public static void drawGameBackground(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;
		Rectangle rect = GameObjects.playingArea;
		g.setPaint(GameObjects.background);
		g.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
		g.setPaint(GameObjects.foreground);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}

	public static void drawGamePlayers(Graphics g) {
		for (int k = 0; k < GameObjects.players.length; k++)
			if (GameObjects.players[k] != null)
				GameObjects.players[k].draw(g);
	}
	
	public static void drawPlayerStats(Graphics g) {
		g.setColor(Color.white);
		g.setFont(new Font("Arial", Font.PLAIN, 24));
		int x = 30, y = 480;
		g.drawString("Deaths", x, y);
		g.drawRect(x, y += 5, 400, 100);
		for(GamePlayer plr : GameObjects.players) {
			if(plr == null)
				continue;
			
			g.drawString(plr.username + " - " + plr.deaths, x + 15, y += 20);
		}
	}

	public static void drawDiedScreen(Graphics g, int l) {
		int alpha = (int) ((l / 100.0d) * 255.0d);
		g.setColor(new Color(0, 0, 0, alpha));
		g.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
		((Graphics2D) g).setPaint(new GradientPaint(200, 200, new Color(0, 255,
				0), 400, 400, new Color(0, 0, 255)));
		g.setFont(new Font("Arial Black", Font.PLAIN, 36));
		g.drawString("TRY AGAIN", GameFrame.WIDTH / 2 - 110,
				GameFrame.HEIGHT / 2);
	}
}
