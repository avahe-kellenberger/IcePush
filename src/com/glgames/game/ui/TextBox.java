package com.glgames.game.ui;

import java.awt.Color;
import java.awt.Rectangle;

import com.glgames.game.Renderer;

public class TextBox extends UIComponent {
	private int count;
	
    protected boolean focused = false;
	protected String caption;
	String value = "";

    TextBox (int x, int y, int width, int height) {
        super(x, y, width, height);
    }

	public void append(char c) {
		if (!focused)
			return;
		if (c == 8) {
			if (value.length() > 0)
				value = value.substring(0, value.length() - 1);
		} else if ((Character.isLetterOrDigit(c)  || c == '.') && value.length() < 15)
			value += c;
	}

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setText(String value) {
        this.value = value;
    }

	public String getText() {
		return value;
	}

    public void focus() {
        focused = true;
    }

    public void unfocus() {
        focused = false;
    }

	public void toggleFocus() {
		focused = !focused;
	}

	public boolean hasFocus() {
		return focused;
	}

	static final Color selectedCol = new Color(0, 64, 255, 200);
	static final Color deselectedCol = new Color(0, 16, 64, 200);
	protected void drawComponent(Renderer r) {
		if(focused)
			r.setColor(selectedCol);
		else
			r.setColor(deselectedCol);
		r.fillRect(abs_x, abs_y, width, height);
		
		r.setColor(Color.white);
		r.drawString(caption, abs_x - r.stringWidth(caption) - 5, abs_y + 15);
		r.drawString(value, abs_x + 3, abs_y + 17);
		
		if(focused && count++ % 50 > 25) {
			int width = r.stringWidth(value) + 5;
			r.drawLine(abs_x + width, abs_y + 1, abs_x + width, abs_y + 17);
		}
	}
}
