package com.glgames.game.ui;

import java.awt.Color;
import java.awt.Graphics;

public class Label extends UserInterface {
	String caption;
	Color color;
	
	public void draw(Graphics g) {
		int sX = this.x;
		int sY = this.y;
		if(parent != null) {
			sX += parent.x;
			sY += parent.y;
		}
		
		g.setColor(color);
		g.drawString(caption, sX, sY);
	}
}
