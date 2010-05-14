package com.glgames.server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
	public static final int VERSION = 100;
	public static boolean DEBUG = false;
	public static Player[] players;
	boolean run = true;
	ServerSocket listener;

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
					new Player(s); // ETC ETC
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

			p.processIncomingPackets();
			p.handleMove();
		}
	}

	public static void main(String[] args) {
		players = new Player[50];
		new Server(2345);
	}
}
