package net.threesided.server;

import static net.threesided.shared.Opcodes.BAD_VERSION;
import static net.threesided.shared.Opcodes.PLAYER_LOGGED_OUT;
import static net.threesided.shared.Opcodes.SUCCESS_LOG;
import static net.threesided.shared.Opcodes.TOO_MANY_PL;
import static net.threesided.shared.Opcodes.USER_IN_USE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

import net.threesided.server.physics2d.Physics2D;

import net.threesided.shared.InterthreadQueue;
import net.threesided.shared.Opcodes;

import net.threesided.shared.PacketBuffer;

public class Server implements Runnable {
	public static boolean DEBUG = false;
	public static Player[] players;
	public static Map<String, String> settings;
	private static Socket worldserver;

	private static Physics2D physics;
	private static UpdateServer updates;
	static MapClass mapClass;
	
	static boolean run = true;
	private ServerSocket listener;
	private InterthreadQueue<Socket> incomingConnections;
	private InternetRelayChat irc;

	int blockCount;

	public static final int ROUND_LENGTH = 90000;

	private static int timeRemaining = ROUND_LENGTH;

	public Server() {
		settings = loadSettings("config");

		if(Boolean.parseBoolean(settings.get("show-in-list")))
			worldserver = connectToWorldServer(settings
					.get("worldserver-addr"), Opcodes.WORLDPORT);

		incomingConnections = new InterthreadQueue<Socket>();

		irc = new InternetRelayChat(settings.get("irc-server"), Integer.parseInt(settings.get("port")),
				settings.get("channel"), settings.get("host").replace(".", "-"));
		Thread t = new Thread(irc);
		t.setDaemon(true);
		t.start();

		int port = Integer.parseInt(settings.get("bind-port"));
		try {
			listener = new ServerSocket(port);
		} catch (IOException ioe) {
			System.out.println("Could not bind to port!");
			ioe.printStackTrace();
		}
		System.out.println("Client listener started on port " + port);
		new Thread(this).start();

		physics = new Physics2D(players);
		updates = new UpdateServer(new File(settings.get("update-path")));
		updates.start();
		mapClass = new MapClass();

		try {
			PacketMapper.load();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		while (run) {
			Socket s = incomingConnections.pull();
			if (s != null) processIncomingConnection(s);
			physics.update();
			updatePlayers();
			try {
				Thread.sleep(20);
			} catch (Exception e) {

			}
			if(getNumPlayers() > 1) {
				timeRemaining -= 20;
				if(timeRemaining <= 0) {
				//	resetDeaths();
					timeRemaining = ROUND_LENGTH;
				}
			}
		}
	}

	private void resetDeaths() {
		for(Player plr : players) {
			if(plr == null) continue;
			plr.resetDeaths();
		}
	}

	private void processIncomingConnection(Socket s) {
		System.out.println("Connection accepted: " + s.toString());
		String host = s.getInetAddress().getHostName();
		if(host.endsWith("mia.bellsouth.net") || host.endsWith("anchorfree.com")) {
			blockCount++;
			if((blockCount % 10) == 1)
				System.out.println("Blocked: " + blockCount + " times");
			try {
				s.close();
			} catch (Exception e) {
			}
			return;
		} else {
			try {
				s.setTcpNoDelay(true);
				int type = s.getInputStream().read();
				if (type == 0) { // connecting client
					loginPlayer(s);
				} else if (type == 2) {
					s.getOutputStream().write(getNumPlayers());
				} else if(type == 3) {
					updates.incomingConnections.push(s);	// THIS SERVER IS HELD TOGETHER WITH DUCT TAPE
				}
			} catch(IOException ioe) {
				System.out.println("Error processing connection!");
				ioe.printStackTrace();
			}
		}
	}
	
	private Map<String, String> loadSettings(String fn) {
		try {
			Map<String, String> ret = new HashMap<String, String>();
			BufferedReader br = new BufferedReader(new FileReader(fn));
			String line;
			while((line = br.readLine()) != null) {
				String[] parts = line.split(":");
				ret.put(parts[0], parts[1]);
			}
			for(String def : defaults.keySet()) {
				if(!ret.containsKey(def))
					ret.put(def, defaults.get(def));
			}
			return ret;
		} catch(Exception e) {
			e.printStackTrace();
			return defaults;
		}
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
		if((worldserver == null) || !worldserver.isConnected())
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

			for (int k = 0; k < players.length; k++) {
				if (players[k] != null) {
					String user = players[k].username;
					if (user != null && user.equals(p.username)) {
						out.write(USER_IN_USE); // name in use
						out.flush();
						return;
					}
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

			p.pbuf = new PacketBuffer(s); //net.threesided.test.DebugPacketBuffer(s);
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
			if(getNumPlayers() < 2) {
				timeRemaining = -1;
				//System.out.println(getNumPlayers());
			}
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
	//	int focusX = 0, focusZ = 0;
		
		ArrayList<String> chats = new ArrayList<String>();
		String msg;
		while((msg = InternetRelayChat.msgs.pull()) != null)
			chats.add(msg);
		
		for (Player p : players) {
			if (p == null || !p.connected)
				continue;
			p.processIncomingPackets();
			p.handleMove();
			p.writePendingChats(chats);
			if(getNumPlayers() > 1 && timeRemaining % 1000 == 0) p.updateRoundTime(timeRemaining / 1000);
			
	//		focusX += p.area.x - 422; // Distance from center X
	//		focusZ += p.area.y - 211; // Distance from center Y
		}

	//	int nP = getNumPlayers();
	//	if (nP == 0)
	//		return;
	//	focusX /= nP;
	//	focusZ /= nP;
	// For tilting ice but not sure how to do this yet
	//	System.out.println(focusX + " " + focusZ);
	}

	public static void main(String[] args) {
		if(args.length > 0 && args[0].equalsIgnoreCase("-debug")) DEBUG = true;
		players = new Player[30];
		new Server();
	}
	
	private static final Map<String, String> defaults;
	static {
		defaults = new HashMap<String, String>();
		try {
			defaults.put("host", InetAddress.getLocalHost().getHostName());
			defaults.put("bind-port", "2345");
			defaults.put("worldserver-addr", "99.198.122.53");
			defaults.put("show-in-list", "true");
			defaults.put("update-path", "/home/icepush/data");
			defaults.put("irc-server", "irc.quirlion.com");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}