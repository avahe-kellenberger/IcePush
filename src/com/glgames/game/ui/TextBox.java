package com.glgames.game.ui;

import java.awt.Color;
import com.glgames.game.Renderer;

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
	protected void drawComponent(Renderer r) {
		if(isFocused)
			r.setColor(selectedCol);
		else
			r.setColor(deselectedCol);
		r.fillRect(x, y, width, height);
		
		r.setColor(Color.white);
		r.drawString(caption, x - r.stringWidth(caption) - 5, y + 15);
		r.drawString(value, x + 3, y + 17);
		
		if(isFocused && count++ % 50 > 25) {
			int width = r.stringWidth(value) + 5;
			r.drawLine(x + width, y + 1, x + width, y + 17);
		}
	}
}
