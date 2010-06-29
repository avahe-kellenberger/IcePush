package com.glgames.game.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Map;

import com.glgames.game.IcePush;
import com.glgames.game.NetworkHandler;

public class ServerList {
	private int y, width, fontheight;
	private String[] servers;
	private String[] counts;
	private String selected = "";
	public int height;
	
	public ServerList(int y) {
		this.y = y;
		new Thread() {
			public void run() {
				Map<String, Integer> map = NetworkHandler.getWorlds();
				servers = new String[map.size()];
				counts = new String[map.size()];
				int i = 0;
				for (String ser : map.keySet()) {
					int num = map.get(ser);
					servers[i] = ser;
					counts[i] = " - "
							+ (num == 255 ? "offline" : num
									+ (num != 1 ? " players" : " player"));
					i++;
				}
				try { Thread.sleep(10000); } catch(Exception e) { }
			}
		}.start();
	}
	
	public void draw(Graphics g) {
		if(width == 0)
			width = getLongestStringWidth(g) + 50;
		if(fontheight == 0)
			fontheight = g.getFontMetrics().getHeight();
		int x = IcePush.WIDTH / 2 - width / 2;
		g.setColor(Color.gray);
		g.fill3DRect(x, y, width, height = servers.length * fontheight + 15, true);
		int y = this.y + 10;
		for(int i = 0; i < servers.length; i++) {
			String serv = servers[i];
			String count = counts[i];
			if(serv == null || count == null)
				continue;
			if(serv.equals(selected)) {
				g.setColor(Color.green);
				g.fill3DRect(x + 10, y, 15, 15, false);
			} else {
				g.setColor(Color.gray);
				g.fill3DRect(x + 10, y, 15, 15, true);
			}
			
			g.setColor(Color.white);
			if(serv.contains("offline"))
				g.setColor(Color.red);
			g.drawString(serv + count, x + 30, y + 15);
			y += 25;
		}
	}
	
	private int getLongestStringWidth(Graphics g) {
		FontMetrics m = g.getFontMetrics();
		int max = -1;
		for(int i = 0; i < servers.length; i++) {
			if(servers[i] == null || counts[i] == null)
				continue;
			String serv = servers[i], count = counts[i];
			if(m.stringWidth(serv) > max)
				max = m.stringWidth(serv + count);
		}
		return max;
	}
	
	public void processClick(int x, int y) {
		int compx = IcePush.WIDTH / 2 - width / 2;
		x -= compx;
		y -= this.y + 10;
		int index = y / fontheight;
		if(IcePush.DEBUG)
			System.out.println(index);
		if(index < 0 || index > servers.length - 1)
			return;
		selected = servers[index];
	}
	
	public String getSelected() {
		return selected;
	}
}
