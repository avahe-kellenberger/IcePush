package com.glgames.game.ui;

import java.awt.Color;
import java.awt.Graphics;

public class Label extends UIComponent {
	String caption;
	Color color;
	
	protected void drawComponent(Graphics g) {
		g.setColor(color);
		g.drawString(caption, x, y);
	}
}
