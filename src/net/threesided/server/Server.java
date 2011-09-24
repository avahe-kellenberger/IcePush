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
	private boolean DEBUG = false;
	private Player[] players;
	private Map<String, String> settings;
	//private Socket worldserver;

	private Physics2D physics;
	//private UpdateServer updates;
	private MapClass mapClass;
	
	private boolean run = true;
	private ServerSocket listener;
	private InterthreadQueue<Socket> incomingConnections;
	private InternetRelayChat irc;
	private ArrayList<String> chats;

	private int blockCount;

	private static final int ROUND_LENGTH = 90000;
	private static int timeRemaining = ROUND_LENGTH;

	public static void main(String[] args) {
		new Server(args);
	}

	private Server(String[] args) {
		if(args.length > 0 && args[0].equalsIgnoreCase("-debug")) DEBUG = true;
		players = new Player[30];
		settings = loadSettings("config");

		//if(Boolean.parseBoolean(settings.get("show-in-list")))
		//	worldserver = connectToWorldServer(settings
		//			.get("worldserver-addr"), Opcodes.WORLDPORT);

		incomingConnections = new InterthreadQueue<Socket>();

		irc = new InternetRelayChat(settings.get("irc-server"), Integer.parseInt(settings.get("irc-port")),
				settings.get("irc-channel"), settings.get("irc-nick"));
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
		//updates = new UpdateServer(new File(settings.get("update-path")));
		//updates.start();
		mapClass = new MapClass();

		try {
			PacketMapper.load();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		while (run) {
			Socket s = incomingConnections.pull();
			if (s != null) processIncomingConnection(s);
			updateIrc();
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

	public void run() {
		while (run)
			try {
				incomingConnections.push(listener.accept());
				Thread.sleep(30);
			} catch (Exception ioe) {
				System.out.println("Error accepting connections!");
				ioe.printStackTrace();
				run = false;
				//updates.run = false;
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
					//updates.incomingConnections.push(s);	// THIS SERVER IS HELD TOGETHER WITH DUCT TAPE
				}
			} catch(IOException ioe) {
				System.out.println("Error processing connection!");
				ioe.printStackTrace();
			}
		}
	}

	private void loginPlayer(Socket s) {
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
			Player p = new Player(s);
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

			p.initPosition(players, mapClass.currentPath);
			out.write(SUCCESS_LOG); // success
			out.write(p.id);
			out.flush();

			p.connected = true;
			players[p.id] = p;
			for(Player plr : players)
				if(plr != null) {
					p.notifyLogin(plr);		// Tell p about all players already logged in
					plr.notifyLogin(p);		// Tell all already logged in players about p
				}
			System.out.println("Player logged in: " + p.username + ", id: " + p.id);
			syncNumPlayers();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void syncNumPlayers() {
	/*	if((worldserver == null) || !worldserver.isConnected())
			return;
		try {
			OutputStream out = worldserver.getOutputStream();
			out.write(Opcodes.NUM_PLAYERS_NOTIFY);
			out.write(getNumPlayers());
			out.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}*/
	}

	private void updateIrc() {
		irc.processInput();
		chats = new ArrayList<String>();
		String msg;
		while((msg = InternetRelayChat.msgs.pull()) != null) chats.add(msg);
		String kick;
		while((kick = InternetRelayChat.kicks.pull()) != null) {
			for (Player p : players) {
				if (p != null) {
					if (p.username.toLowerCase().equals(kick)) {
						logoutPlayer(p);
						InternetRelayChat.sendMessage("Player " + kick + " has been kicked.");
						continue;
					}
				}
			}
		}
	}

	private void updatePlayers() {
		for (Player p : players) {
			if (p == null || !p.connected)
				continue;
			if(!p.processIncomingPackets() || p.logOut) {
				logoutPlayer(p);
			} else {
				p.writePendingChats(chats);
				if(p.chatMessage != null) {
					InternetRelayChat.sendMessage(p.chatMessage);
					InternetRelayChat.msgs.push(p.chatMessage);
					p.chatMessage = null;
				}

				if(!mapClass.currentPath.contains(p.x, p.y)) {
					System.out.println("PLAYER " + p.username + " IS OUT OF RANGE!");
					p.deaths++;
					p.initPosition(players, mapClass.currentPath);
					for(Player plr : players) if(plr != null) plr.playerDied(p);	// plr cycles through every player; p is the player who just died
					continue;
				}

				if(p.hasMoved()) {
					for(Player plr : players) if(plr != null) plr.handleMove(p);
				}
			}

			if(getNumPlayers() > 1 && timeRemaining % 1000 == 0) p.updateRoundTime(timeRemaining / 1000);
		}
	}

	private void logoutPlayer(Player p) {
		try {
			for (Player plr : players) {
				if (plr == null || plr == p)
					continue;

				plr.loggedOut(p);
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

	private int getNumPlayers() {
		int count = 0;
		for(Player p : players) if(p != null)
			count++;
		return count;
	}

	private void resetDeaths() {
		for(Player p : players) if(p != null) for(Player plr : players) if(plr != null) p.resetDeaths(plr);
	}

	private static Map<String, String> defaults;

	static {
		defaults = new HashMap<String, String>();

		defaults.put("bind-port", "2345");

		defaults.put("irc-server", "irc.strictfp.com");
		defaults.put("irc-channel", "#icepush");
		defaults.put("irc-port", "6667");
		defaults.put("irc-nick", "TestServer");

		/* Worldserver and updateserver temporarily disabled for the time being */
		//defaults.put("worldserver-addr", "99.198.122.53");
		//defaults.put("show-in-list", "true");
		//defaults.put("update-path", "/home/icepush/data");
	}
}
