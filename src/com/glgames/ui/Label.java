package com.glgames.ui;

import java.awt.Color;

import import com.glgames.graphics2d.Renderer;

public class Label extends UIComponent {
	String caption;
	Color color;
	
	Label (int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	Label (int width, int height) {
		super(width, height);
	}

	protected void drawComponent(Renderer r) {
		r.setColor(color);
		r.drawString(caption, abs_x, abs_y);
	}
}
