package com.glgames.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import com.glgames.graphics2d.Renderer;

public class Label extends UIComponent {
	protected String text;
	protected Color color = Color.white;
	protected Font font = new Font("Arial", Font.PLAIN, 20);
	
	Label () {
		super(0, 0);
	}
	Label (String text) {
		super(0, 0);
		setText(text);
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setFont(Font font) {
		this.font = font;
		setSize(0, 0);
	}

	public Font getFont() {
		return font;
	}

	protected void drawComponent(Renderer r) {
		if ((width == 0) && (height == 0)) {
			r.setFont(font);
			int new_width = r.stringWidth(text);
			int new_height = r.getFontHeight();
			setSize(new_width, new_height);
		}
		r.setColor(color);
		r.setFont(font);
		r.drawString(text, abs_x, abs_y);
	}
}
