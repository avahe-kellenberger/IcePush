package com.glgames.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.glgames.shared.Opcodes;

public class WorldServer implements Runnable {
	private int port;
	private Map<String, Socket> sockets;
	private Map<String, Integer> servers;

	public WorldServer(int p) {
		port = p;
		sockets = new HashMap<String, Socket>();
		servers = new HashMap<String, Integer>();
		new Thread(this).start();
		while(true) {
			Iterator<String> it = sockets.keySet().iterator();
			while(it.hasNext()) {
				String srv = it.next();
				Socket sock = sockets.get(srv);
				if(sock.isClosed() || !sock.isConnected()) {
					it.remove();
					servers.remove(srv);
				}
				try {
					InputStream in = sock.getInputStream();
					if (in.available() == 0)
						continue;
					int req = in.read();
					if (req == Opcodes.NUM_PLAYERS_NOTIFY) {
						int amt = in.read();
						servers.put(srv, amt);
						System.out.println(":: " + srv + " updated to " + amt);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(port);
			Socket s;
			while ((s = ss.accept()) != null) {
				System.out.print("Connection accepted from "
						+ s.getInetAddress().getHostAddress() + ", type: ");
				try {
					InputStream in = s.getInputStream();
					OutputStream out = s.getOutputStream();
					int type = in.read();
					System.out.println(type);
					if(type == Opcodes.NEW_SERVER) {
						// omg new server connecting
						int len = in.read();
						byte[] strb = new byte[len];
						in.read(strb);
						String name = new String(strb);
						int count = in.read();
						name += '@' + s.getInetAddress().getHostAddress();
						sockets.put(name, s);
						servers.put(name, count);
						System.out.println("-- " + name + " connected with "
								+ count + " players");
					} else if(type == Opcodes.NUM_PLAYERS_REQUEST) {
						// client is requesting number of people
						System.out.println(":: World list requested");
						out.write(servers.size());
						for (String server : servers.keySet()) {
							out.write(server.length());
							out.write(server.getBytes());
							out.write(servers.get(server));
							out.flush();
						}
						out.flush();
						out.close();
						s.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
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