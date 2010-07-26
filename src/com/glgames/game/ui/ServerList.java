package com.glgames.game.ui;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Map;

import com.glgames.game.GameObjects;
import com.glgames.game.IcePush;
import com.glgames.game.NetworkHandler;

public class ServerList extends ListBox implements Runnable {
	private int fontheight;
	
	public static Action<ServerList> clickedAction = new Action<ServerList>() {
		public void action(ServerList s, int x, int y) {
			if (IcePush.state != IcePush.WELCOME 
					|| GameObjects.serverMode != GameObjects.LIST_FROM_SERVER)
				return;
			int index = y / s.fontheight;
			if(IcePush.DEBUG)
				System.out.println(index);
			if(index < 0 || index > s.items.length - 1)
				return;
			s.selected = s.items[index];
		}
	};
	
	public void run() {
		while(IcePush.running) {		// TODO: THERE MIGHT BE A POTENTIAL THREAD RACE HERE, I AM NOT SURE!!
			if(IcePush.state == IcePush.WELCOME) {
				Map<String, Integer> map = NetworkHandler.getWorlds();
				items = new String[map.size()];
				int i = 0;
				for (String ser : map.keySet()) {
					int num = map.get(ser);
					items[i] = ser +  " - "	+ (num == 255 ? "offline" : num + (num != 1 ? " players" : " player"));
					i++;
				}
				try {
					Thread.sleep(20000);
				} catch(InterruptedException e) {  } 	// Logged out/disconnected before 20 seconds since last world update
			} else synchronized(this) {
				try {
					wait();
				} catch (InterruptedException z) {	}	// Logged out/disconnected after 20 seconds since last world update
			}
		}
	}
	
	public void drawComponent(Graphics g) {
		if(items == null)
			return;
		if(fontheight == 0)
			fontheight = g.getFontMetrics().getHeight();
		if(width == 0 || height == 0) {
			width = getLongestStringWidth(g) + 50;
			height = items.length * fontheight + 15;
		}
		x = IcePush.WIDTH / 2 - getLongestStringWidth(g) / 2;
		super.drawComponent(g);
	}
	
	private int getLongestStringWidth(Graphics g) {
		if(items == null)
			return 0;
		FontMetrics m = g.getFontMetrics();
		int max = -1;
		for(int i = 0; i < items.length; i++) {
			if(items[i] == null)
				continue;
			String serv = items[i];
			if(m.stringWidth(serv) > max)
				max = m.stringWidth(serv);
		}
		return max;
	}
}
