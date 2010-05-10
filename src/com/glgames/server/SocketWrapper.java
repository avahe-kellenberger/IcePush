package com.glgames.server;

import java.net.Socket;

final class SocketWrapper {

	static void push(Socket s) {
		SocketWrapper nh = new SocketWrapper();
		nh.sock = s;

		head.prev = nh;

		head = nh;
	}

	private SocketWrapper() {

	}

	static Socket pull() {
		SocketWrapper tt = tail.prev;
		if (tt == null)
			return null; // If this is non-null, we know that tail != head
		tail = tt;
		return tt.sock;
	}

	private static SocketWrapper head, tail;
	private SocketWrapper prev;
	private Socket sock;

	static {
		head = tail = new SocketWrapper();
	}
}
