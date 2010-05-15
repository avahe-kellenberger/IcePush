package com.glgames.server;

import java.net.Socket;

final class SocketWrapper {
	private static SocketWrapper head, tail;
	private SocketWrapper prev;
	private Socket sock;

	private SocketWrapper() {

	}

	public static Socket pull() {
		SocketWrapper tt = tail.prev;
		if (tt == null)
			return null; // If this is non-null, we know that tail != head
		tail = tt;
		return tt.sock;
	}
	
	public static void push(Socket s) {
		SocketWrapper nh = new SocketWrapper();
		nh.sock = s;
		head.prev = nh;
		head = nh;
	}
	
	static {
		head = tail = new SocketWrapper();
	}
}
