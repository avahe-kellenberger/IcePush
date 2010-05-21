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
	private Map<String, Socket> sockets;
	private Map<String, Integer> servers;

	public WorldServer(int p) {
		port = p;
		sockets = new HashMap<String, Socket>();
		servers = new HashMap<String, Integer>();
		for(String s : LIST)
			servers.put(s, -1);
		// every 10 seconds, refresh numbers
		for(String s : LIST) {
			try {
				Socket sock = new Socket(s, 2345);
				sockets.put(s, sock);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		new Timer().schedule(new TimerTask() {
			public void run() {
				for (String svr : sockets.keySet()) {
					int num;
					try {
						Socket check = sockets.get(svr);
						check.getOutputStream().write(1); // get num players
						num = check.getInputStream().read();
					} catch (Exception e) {
						System.out
								.println("Error getting number of players from "
										+ svr + ": " + e.toString());
						num = -1;
					}
					System.out.println("Players on " + svr + ": " + num);
					servers.put(svr, num);
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
