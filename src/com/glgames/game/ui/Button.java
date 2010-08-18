package com.glgames.game.ui;

import java.awt.Color;
import java.awt.Rectangle;

import com.glgames.game.Renderer;

public class Button extends UIComponent {
	protected Color bgColor = Color.gray;
    protected Color fgColor = Color.white;
	protected String caption;
	
    Button (int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void setFG(Color fgColor) {
        this.fgColor = fgColor;
    }

    public void setBG(Color bgColor) {
        this.bgColor = bgColor;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public String getCaption() {
        return caption;
    }

	protected void drawComponent(Renderer r) {
		r.setColor(bgColor);
		r.fill3DRect(abs_x, abs_y, width, height, true);
		r.setColor(fgColor);
		int w = r.stringWidth(caption);
		r.drawString(caption, abs_x + width / 2 - w / 2, abs_y + 18);
	}
}
