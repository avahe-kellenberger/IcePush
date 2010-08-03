package com.glgames.game.ui;

import java.awt.Color;
import com.glgames.game.Renderer;

public class Label extends UIComponent {
	String caption;
	Color color;
	
	protected void drawComponent(Renderer r) {
		r.setColor(color);
		r.drawString(caption, x, y);
	}
}
