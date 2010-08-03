package com.glgames.game.ui;

import java.awt.Color;
import com.glgames.game.Renderer;

public class ListBox extends UIComponent {
	protected String[] items;
	protected String selected = "";
	
	public void drawComponent(Renderer r) {
		r.setColor(Color.gray);
		r.fill3DRect(x, y, width, height, true);
		int cy = this.y + 10;
		for(int i = 0; i < items.length; i++) {
			String serv = items[i];
			if(serv == null)
				continue;
			if(serv.equals(selected)) {
				r.setColor(Color.green);
				r.fill3DRect(x + 10, cy, 15, 15, false);
			} else {
				r.setColor(Color.gray);
				r.fill3DRect(x + 10, cy, 15, 15, true);
			}
			
			r.setColor(Color.white);
			r.drawString(serv, x + 30, cy + 15);
			cy += 25;
		}
	}
	
	public String getSelected() {
		return selected;
	}
}
