package com.glgames.game.ui;

import java.awt.Color;
import java.awt.Graphics;

public class TextBox extends UserInterface {
	private int count;
	
	boolean isFocused;
	String caption;
	String text = "";

	public TextBox() {

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
		int sX = this.x;
		int sY = this.y;
		if(parent != null) {
			sX += parent.x;
			sY += parent.y;
		}
		if(isFocused)
			g.setColor(Color.gray);
		else
			g.setColor(Color.darkGray);
		g.fill3DRect(sX, sY, 170, 20, false);

		g.setColor(Color.white);
		g.drawString(caption, sX - g.getFontMetrics().stringWidth(caption) - 5, sY + 15);
		g.drawString(text, sX + 3, sY + 17);
		
		if(isFocused && count++ % 50 > 25) {
			int width = g.getFontMetrics().stringWidth(text) + 5;
			g.drawLine(sX + width, sY + 1, sX + width, sY + 17);
		}
	}
}
