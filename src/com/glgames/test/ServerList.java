package com.glgames.game;

import java.awt.FontMetrics;
import java.util.Map;

import com.glgames.graphics2d.Renderer;
import com.glgames.ui.ListBox;
import com.glgames.ui.Action;

public class ServerList extends ListBox implements Runnable {
	private int fontHeight;
	private String serverIPs[];

	ServerList (int x, int y) {
		super(x, y, 0, 0);
	}

	public String getSelected() {
		return serverIPs[selectedIndex];
	}
	
	public int getFontHeight() {
		return fontHeight;
	}

	public static Action<ServerList> onServerListClick = new Action<ServerList>() {
		public void doAction(ServerList component, int x, int y) {
			if (IcePush.state != IcePush.WELCOME 
					|| GameObjects.serverMode != GameObjects.LIST_FROM_SERVER)
				return;
			int index = y / component.fontHeight;
			if(IcePush.DEBUG)
				System.out.println(index);
			synchronized(component) {
				if(index < 0 || index > component.items.length - 1)
					return;
				component.selectedIndex = index;
			}
		}
	};
	
	public void run() {
		while(IcePush.running) {
			if(IcePush.state == IcePush.WELCOME) {
				Map<String, Integer> map = NetworkHandler.getWorlds();
				String _items[] = new String[map.size()];
				String _serverIPs[] = new String[map.size()];
				int i = 0;
				for (String ser : map.keySet()) {
					int num = map.get(ser);
					String parts[] = ser.split("@");
					String name = parts[0];
					String ip = parts[1];
					//System.out.println("name = " + name + " ip=" + ip);
					_items[i] = name +  " - " + (num == 255 ? "offline" : num + (num != 1 ? " players" : " player"));
					_serverIPs[i] = ip;
					i++;
				}

				// There is a potential thread race if the ServerList thread reloads the worlds while the client thread is rendering them
				// The serverlist thread can resize the arrays (potentially making them smaller) while the rendering process reads them
				// Minimal synchronization is required around all accesses to items and server IPs

				synchronized(this) {
					items = _items;
					serverIPs = _serverIPs;
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
	
	public synchronized void drawComponent(Renderer r) {
		if(items == null)
			return;
		if(fontHeight == 0)
			fontHeight = r.getFontHeight();
		if(width == 0 || height == 0) {
			width = getLongestStringWidth(r) + 50;
			height = items.length * fontHeight + 15;
		}
		abs_x = IcePush.WIDTH / 2 - getLongestStringWidth(r) / 2;
		super.drawComponent(r);
	}
	
	private int getLongestStringWidth(Renderer r) {
		if(items == null)
			return 0;
		int max = -1;
		for(int i = 0; i < items.length; i++) {
			if(items[i] == null)
				continue;
			String serv = items[i];
			int w = r.stringWidth(serv);	
			if(w > max)
				max = w;
		}
		return max;
	}
}
