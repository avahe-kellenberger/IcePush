package com.glgames.server;

import static com.glgames.shared.Opcodes.BAD_VERSION;
import static com.glgames.shared.Opcodes.PLAYER_LOGGED_OUT;
import static com.glgames.shared.Opcodes.SUCCESS_LOG;
import static com.glgames.shared.Opcodes.TOO_MANY_PL;
import static com.glgames.shared.Opcodes.USER_IN_USE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.glgames.shared.InterthreadQueue;
import com.glgames.shared.Opcodes;
import com.glgames.shared.PacketBuffer;

public class Server implements Runnable {
	public static boolean DEBUG = false;
	public static Player[] players;
	public static Map<String, String> settings;
	private static Socket worldserver;
	
	private boolean run = true;
	private ServerSocket listener;
	private InterthreadQueue<Socket> incomingConnections;

	public Server(int port) {
		try {
			settings = loadSettings("config");
			worldserver = connectToWorldServer(Opcodes.WORLDSERVER, Opcodes.WORLDPORT);
			incomingConnections = new InterthreadQueue<Socket>();
			listener = new ServerSocket(port);
			System.out.println("Client listener started on port " + port);
			new Thread(this).start();
			
			while (run) {
				Socket s = incomingConnections.pull();
				if (s != null) {
					System.out.println("Client accepted, socket: "
							+ s.toString());
					s.setTcpNoDelay(true);
					int type = s.getInputStream().read();
					if(type == 0) // connecting client
						loginPlayer(s);
				}
				updatePlayers();
				try {
					Thread.sleep(30);
				} catch (Exception e) {

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Map<String, String> loadSettings(String fn) throws Exception {
		Map<String, String> ret = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(fn));
		String line;
		while((line = br.readLine()) != null) {
			String[] parts = line.split(":");
			ret.put(parts[0], parts[1]);
		}
		return ret;
	}

	private Socket connectToWorldServer(String server, int port) {
		System.out.print("Connecting to worldserver: " + server + ":" + port + "...");
		try {
			Socket sock = new Socket(server, port);
			System.out.print("connected...");
			OutputStream out = sock.getOutputStream();
			out.write(Opcodes.NEW_SERVER);
			System.out.print("Opcodes.NEW_SERVER sent...");
			String host = settings.get("host");
			System.out.print("host (" + host + ") sent...");
			out.write(host.length());
			out.write(host.getBytes());
			out.write(getNumPlayers());
			out.flush();
			System.out.println("stream flushed.");
			return sock;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static void syncNumPlayers() {
		if(worldserver == null)
			return;
		try {
			OutputStream out = worldserver.getOutputStream();
			out.write(Opcodes.NUM_PLAYERS_NOTIFY);
			out.write(getNumPlayers());
			out.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static int getNumPlayers() {
		int count = 0;
		for(Player p : players) if(p != null)
			count++;
		return count;
	}

	private void loginPlayer(Socket s) {
		Player p = new Player();
		try {
			InputStream in = s.getInputStream();
			OutputStream out = s.getOutputStream();
			int version = in.read();
			if (version != Opcodes.VERSION) {
				out.write(BAD_VERSION); // bad version
				out.flush();
				return;
			}
			int len = in.read();
			byte[] strb = new byte[len];
			in.read(strb);

			p.username = new String(strb);

			for (int k = 0; k < players.length; k++)
				if (players[k] != null) {
					String user = players[k].username;
					if (user != null && user.equals(p.username)) {
						out.write(USER_IN_USE); // name in use
						out.flush();
						return;
					}
				}
			int index = -1;
			for (int k = 0; k < players.length; k++)
				if (players[k] == null) {
					index = k;
					break;
				}
			if (index == -1) {
				out.write(TOO_MANY_PL);
				out.flush();
				return;
			}

			p.id = index;
			p.type = index % 2;

			p.initPosition();
			out.write(SUCCESS_LOG); // success
			out.write(p.id);
			out.flush();

			p.pbuf = new PacketBuffer(s);
			p.connected = true;
			players[p.id] = p;
			p.notifyLogin();
			System.out.println("Player logged in: " + p.username + ", id: " + p.id);
			syncNumPlayers();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void logoutPlayer(Player p) {
		try {
			for (Player plr : Server.players) {
				if (plr == null || plr == p)
					continue;

				plr.pbuf.beginPacket(PLAYER_LOGGED_OUT);
				plr.pbuf.writeShort(p.id);
				plr.pbuf.endPacket();
			}
			
			p.connected = false;
			players[p.id] = null;
			System.out.println("Logged out: " + p.id);
			
			syncNumPlayers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (run)
			try {
				incomingConnections.push(listener.accept());
				Thread.sleep(30);
			} catch (Exception ioe) {
				System.out.println("Error accepting connections!");
				ioe.printStackTrace();
				run = false;
			}
	}

	private void updatePlayers() {
		for (Player p : players) {
			if (p == null || !p.connected)
				continue;
			p.keepAlive();
			p.processIncomingPackets();
			p.handleMove();
		}
	}

	public static void main(String[] args) {
		if(args.length > 0 && args[0].equalsIgnoreCase("-debug")) DEBUG = true;
		players = new Player[50];
		new Server(2345);
	}
}
