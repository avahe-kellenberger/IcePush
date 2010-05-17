package com.glgames.server;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class WorldServer implements Runnable {
	private static final String[] LIST = { "strictfp.com", "quirlion.com" };
	private int port;
	private Map<String, Integer> servers;

	public WorldServer(int p) {
		port = p;
		servers = new HashMap<String, Integer>();
		
		// every 10 seconds, refresh numbers
		new Timer().schedule(new TimerTask() {
			public void run() {
				for (String s : LIST) {
					int num;
					try {
						Socket check = new Socket(s, 2345);
						check.getOutputStream().write(1); // get num players
						num = check.getInputStream().read();
					} catch (Exception e) {
						num = -1;
					}
					servers.put(s, num);
				}
			}
		}, 0, 10000);
		new Thread(this).start();
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(port);
			Socket s;
			while ((s = ss.accept()) != null) {
				try {
					System.out.println("Worlds requested from " + s.toString());
					OutputStream out = s.getOutputStream();
					out.write(servers.size());
					for (String server : servers.keySet()) {
						out.write(server.length());
						out.write(server.getBytes());
						out.write(servers.get(server));
						out.flush();
					}
					out.flush();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					s.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new WorldServer(2346);
	}
}
