package com.glgames.server;

import com.glgames.shared.Opcodes;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class WorldServer implements Runnable {
	private int port;
	private Map<String, Socket> sockets;
	private Map<String, Integer> servers;

	public WorldServer(int p) {
		port = p;
		servers = new HashMap<String, Integer>();
		new Thread(this).start();
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(port);
			Socket s;
			while ((s = ss.accept()) != null) {
				try {
					InputStream in = s.getInputStream();
					if (in.read() == Opcodes.WORLD_REQUEST) {
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
					} else if (in.read() == Opcodes.WORLD_SEND) {
						
					}
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
		new WorldServer(Opcodes.WORLDPORT);
	}
}
