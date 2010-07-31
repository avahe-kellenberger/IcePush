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

	static final Color selectedCol = new Color(0, 64, 255, 200);
	static final Color deselectedCol = new Color(0, 16, 64, 200);
	protected void drawComponent(Graphics g) {
		if(isFocused)
			g.setColor(selectedCol);
		else
			g.setColor(deselectedCol);
		g.fillRect(x, y, width, height);
		
		g.setColor(Color.white);
		g.drawString(caption, x - g.getFontMetrics().stringWidth(caption) - 5, y + 15);
		g.drawString(value, x + 3, y + 17);
		
		if(isFocused && count++ % 50 > 25) {
			int width = g.getFontMetrics().stringWidth(value) + 5;
			g.drawLine(x + width, y + 1, x + width, y + 17);
		}
	}
}
