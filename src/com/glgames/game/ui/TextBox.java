package com.glgames.game.ui;

import java.awt.Color;
import java.awt.Graphics;

public class TextBox extends UIComponent {
	private int count;
	
	boolean isFocused;
	String caption;
	String value = "";

	public TextBox() {
		width = 170;
		height = 20;
	}

	public void append(char c) {
		if (!isFocused)
			return;
		if (c == 8) {
			if (value.length() > 0)
				value = value.substring(0, value.length() - 1);
		} else if ((Character.isLetterOrDigit(c)  || c == '.') && value.length() < 15)
			value += c;
	}

	public String getText() {
		return value;
	}

	public boolean isFocused() {
		return isFocused;
	}

	public void toggleFocused() {
		isFocused = !isFocused;
	}

	protected void drawComponent(Graphics g) {
		if(isFocused)
			g.setColor(Color.gray);
		else
			g.setColor(Color.darkGray);
		g.fill3DRect(x, y, width, height, false);

		g.setColor(Color.white);
		g.drawString(caption, x - g.getFontMetrics().stringWidth(caption) - 5, y + 15);
		g.drawString(value, x + 3, y + 17);
		
		if(isFocused && count++ % 50 > 25) {
			int width = g.getFontMetrics().stringWidth(value) + 5;
			g.drawLine(y + width, y + 1, y + width, y + 17);
		}
	}
}
