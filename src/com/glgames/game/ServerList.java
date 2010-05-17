package com.glgames.game;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerList {
	private int x, y;
	private List<String> servers;
	private String selected;
	
	public ServerList(int x, int y, Map<String, Integer> s) {
		this.x = x;
		this.y = y;
		servers = new ArrayList<String>();
		for(String ser : s.keySet()) {
			int num = s.get(ser);
			servers.add(ser + " - " + (num == 255 ? "offline" : num + (num != 1 ? " players" : " player")));
		}
		selected = servers.get(0);
	}
	
	public void draw(Graphics g) {
		int y = this.y;
		for(String serv : servers) {
			g.setColor(Color.white);
			if(serv.contains("offline"))
				g.setColor(Color.red);
			g.drawRect(x, y - 15, 15, 15);
			if(serv.equals(selected)) {
				g.drawLine(x, y - 15, x + 15, y);
				g.drawLine(x + 15, y - 15, x, y);
			}
			g.drawString(serv, x + 20, y);
			y += 25;
		}
	}
	
	public void processClick(int x, int y) {
		x -= this.x;
		y -= this.y;
		int index = y / 15;
		if(index < 0 || index > servers.size() - 1)
			return;
		selected = servers.get(index);
	}
	
	public String getSelected() {
		return selected.substring(0, selected.indexOf(' '));
	}
}
