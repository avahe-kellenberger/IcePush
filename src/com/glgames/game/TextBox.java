package com.glgames.game;

import java.awt.Color;
import java.awt.Graphics;

public class TextBox {
	private int x, y;
	private int count;
	private boolean isFocused;
	private String caption, text = "";

	public TextBox(int x, int y, boolean focus, String c, String text) {
		this.x = x;
		this.y = y;
		this.caption = c;
		this.text = text;
		this.isFocused = focus;
	}

	public void append(char c) {
		if (!isFocused)
			return;
		if (c == 8) {
			if (text.length() > 0)
				text = text.substring(0, text.length() - 1);
		} else if ((Character.isLetterOrDigit(c)  || c == '.') && text.length() < 15)
			text += c;
	}

	public String getText() {
		return text;
	}

	public boolean isFocused() {
		return isFocused;
	}

	public void toggleFocused() {
		isFocused = !isFocused;
	}

	public void draw(Graphics g) {
		if(isFocused)
			g.setColor(Color.gray);
		else
			g.setColor(Color.darkGray);
		g.fill3DRect(x, y, 170, 20, false);

		g.setColor(Color.white);
		g.drawString(caption, x - g.getFontMetrics().stringWidth(caption) - 5, y + 15);
		g.drawString(text, x + 3, y + 17);
		
		if(isFocused && count++ % 50 > 25) {
			int width = g.getFontMetrics().stringWidth(text) + 5;
			g.drawLine(x + width, y + 1, x + width, y + 17);
		}
	}
}
