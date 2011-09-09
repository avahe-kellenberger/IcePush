package net.threesided.ui;

import java.awt.Graphics;
import java.awt.Color;

public class ListBox extends UIComponent {
	protected String[] items;
	protected int selectedIndex;
	
	ListBox (int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	ListBox (int width, int height) {
		super(width, height);
	}

	public void drawComponent(Graphics g) {
		g.setColor(Color.gray);
		g.fill3DRect(abs_x, abs_y, width, height, true);
		int cy = this.abs_y + 10;
		for(int i = 0; i < items.length; i++) {
			String serv = items[i];
			if(serv == null)
				continue;
			if(i == selectedIndex) {
				g.setColor(Color.green);
				g.fill3DRect(abs_x + 10, cy, 15, 15, false);
			} else {
				g.setColor(Color.gray);
				g.fill3DRect(abs_x + 10, cy, 15, 15, true);
			}
			
			g.setColor(Color.white);
			g.drawString(serv, abs_x + 30, cy + 15);
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
