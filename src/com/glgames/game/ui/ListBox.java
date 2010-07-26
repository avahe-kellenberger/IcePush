package com.glgames.game.ui;

import java.awt.Color;
import java.awt.Graphics;

public class ListBox extends UIComponent {
	protected String[] items;
	protected String selected = "";
	
	public void drawComponent(Graphics g) {
		g.setColor(Color.gray);
		g.fill3DRect(x, y, width, height, true);
		int cy = this.y + 10;
		for(int i = 0; i < items.length; i++) {
			String serv = items[i];
			if(serv == null)
				continue;
			if(serv.equals(selected)) {
				g.setColor(Color.green);
				g.fill3DRect(x + 10, cy, 15, 15, false);
			} else {
				g.setColor(Color.gray);
				g.fill3DRect(x + 10, cy, 15, 15, true);
			}
			
			g.setColor(Color.white);
			g.drawString(serv, x + 30, cy + 15);
			cy += 25;
		}
	}
	
	public String getSelected() {
		return selected;
	}
}
