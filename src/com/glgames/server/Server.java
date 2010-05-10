package com.glgames.server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static final int VERSION = 100;
	public static Player[] players;
	
	public Server(int port) {
		try {
			ServerSocket ss = new ServerSocket(port);
			System.out.println("Client listener started on port " + port);
			Socket s;
			while((s = ss.accept()) != null) { // will never exit
				System.out.println("Client accepted, socket: " + s.toString());
				s.setTcpNoDelay(true);
				Player p = new Player(s); // ETC ETC
				p.login();
			}
			
			updatePlayers();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void updatePlayers() {
		for (Player p : players) {
			if (p == null || !p.connected)
				continue;

			p.processIncomingPackets();
			p.handleMove();
		}
	}

	public static void main(String[] args) {
		players = new Player[50];
		new Server(2345);
	}
}
