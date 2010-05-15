package com.glgames.server;

import static com.glgames.shared.Opcodes.BAD_VERSION;
import static com.glgames.shared.Opcodes.SUCCESS_LOG;
import static com.glgames.shared.Opcodes.TOO_MANY_PL;
import static com.glgames.shared.Opcodes.USER_IN_USE;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.glgames.shared.Opcodes;
import com.glgames.shared.PacketBuffer;

public class Server implements Runnable {
	public static boolean DEBUG = false;
	public static Player[] players;
	
	private boolean run = true;
	private ServerSocket listener;

	public Server(int port) {
		try {
			listener = new ServerSocket(port);
			System.out.println("Client listener started on port " + port);
			(new Thread(this)).start();

			while (run) {
				Socket s = SocketWrapper.pull();
				if (s != null) {
					System.out.println("Client accepted, socket: "
							+ s.toString());
					s.setTcpNoDelay(true);
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
			System.out.println("Player logged in: " + p.username + ", id: " + p.id);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (run)
			try {
				SocketWrapper.push(listener.accept());
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
		players = new Player[50];
		new Server(2345);
	}
}
