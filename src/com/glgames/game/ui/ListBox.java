package com.glgames.game.ui;

import java.awt.Color;
import com.glgames.game.Renderer;

public class ListBox extends UIComponent {
	protected String[] items;
	protected int selectedIndex;
	
	ListBox (int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	ListBox (int width, int height) {
		super(width, height);
	}

	public void drawComponent(Renderer r) {
		r.setColor(Color.gray);
		r.fill3DRect(abs_x, abs_y, width, height, true);
		int cy = this.abs_y + 10;
		for(int i = 0; i < items.length; i++) {
			String serv = items[i];
			if(serv == null)
				continue;
			if(i == selectedIndex) {
				r.setColor(Color.green);
				r.fill3DRect(abs_x + 10, cy, 15, 15, false);
			} else {
				r.setColor(Color.gray);
				r.fill3DRect(abs_x + 10, cy, 15, 15, true);
			}
			
			r.setColor(Color.white);
			r.drawString(serv, abs_x + 30, cy + 15);
			cy += 25;
		}
	}
	
	public String[] getItems() {
		return items;
	}

	public void setSelected(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	public String getSelected() {
		return items[selectedIndex];
	}
}
