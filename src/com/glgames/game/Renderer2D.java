package com.glgames.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Renderer2D extends Renderer {
	private static final long serialVersionUID = 1L;

	public void drawDebug() {
		// TODO Auto-generated method stub

	}
	
	public void renderScene(Object[] objects) {
		Graphics2D g = (Graphics2D) bg;
		Rectangle rect = GameObjects.playingArea;
		g.setPaint(GameObjects.background);
		g.fillRect(0, 0, GameFrame.WIDTH, GameFrame.HEIGHT);
		g.setPaint(GameObjects.foreground);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		
		for (int k = 0; k < objects.length; k++) {
			Object2D p = (Object2D) objects[k];
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

}
