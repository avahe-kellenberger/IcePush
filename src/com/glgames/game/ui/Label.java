package com.glgames.game.ui;

import java.awt.Color;

import com.glgames.game.Renderer;

public class Label extends UIComponent {
	String caption;
	Color color;
	
	Label (int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	protected void drawComponent(Renderer r) {
		r.setColor(color);
		r.drawString(caption, abs_x, abs_y);
	}
}
