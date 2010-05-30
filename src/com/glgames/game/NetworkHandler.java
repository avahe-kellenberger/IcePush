package com.glgames.game;

import static com.glgames.shared.Opcodes.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.glgames.shared.Opcodes;
import com.glgames.shared.PacketBuffer;

public class NetworkHandler {
	// The id of the connected player
	public static int id;

	private static Socket sock;
	private static PacketBuffer pbuf;
	private static long pingTime;

	public static void login(String server, String username) {
		try {
			long start = System.currentTimeMillis();
			sock = new Socket(server, 2345);
			if (IcePush.DEBUG)
				System.out.println("Time to establish socket: "
						+ (System.currentTimeMillis() - start));
			sock.setTcpNoDelay(true);

			OutputStream out = sock.getOutputStream();
			InputStream in = sock.getInputStream();

			out.write(0); // connecting client
			out.write(VERSION);
			out.write(username.length());
			out.write(username.getBytes());
			out.flush();

			int result = in.read();
			if (result == USER_IN_USE) {
				Renderer.message = "That username is in use.";
			} else if (result == BAD_VERSION) {
				Renderer.message = "The game has been updated, refresh the page.";
			} else if (result == TOO_MANY_PL) {
				Renderer.message = "Too many players are logged in.";
			} else if (result == SUCCESS_LOG) {
				// Successful login
				id = in.read();
				pbuf = new PacketBuffer(sock);
				IcePush.moveFlags = 0;
				GameObjects.players = new Player2D[50];
				IcePush.state = IcePush.PLAY;
			} else {
				Renderer.message = "Invalid response from server.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			Renderer.message = "Error connecting to server: " + e.getMessage();
		}
	}

	public static void handlePackets() {
		if (IcePush.state == IcePush.WELCOME)
			return;

		if (!pbuf.synch()) {
			IcePush.state = IcePush.WELCOME;
			Renderer.message = "Connection with server was lost";
			return;
		}

		int opcode;
		int id, type, x, y;
		String username;
		Player2D p2;
		while ((opcode = pbuf.openPacket()) != -1) {
			switch (opcode) {
				case NEW_PLAYER:
					id = pbuf.readShort();
					type = pbuf.readByte(); // snowman or tree??
					username = pbuf.readString();
					x = pbuf.readShort();
					y = pbuf.readShort();
					int deaths = pbuf.readShort();
					p2 = new Player2D(type == TREE ? "images/tree.png"
							: "images/snowman.png");
					p2.x = x;
					p2.y = y;
					p2.username = username;
					p2.deaths = deaths;
					GameObjects.players[id] = p2;
					break;
				case PLAYER_MOVED:
					id = pbuf.readShort();
					p2 = GameObjects.players[id];
					if (p2 == null) { // ???????????????
						System.out.println("null player tried to move??? "
								+ id);
						break;
					}
					p2.setBit(pbuf.readShort());
					break;
				case PLAYER_STOPPED_MOVING:
					id = pbuf.readShort();
					p2 = GameObjects.players[id];
					if (p2 == null) { // ???????????????
						System.out.println("null player tried to move??? "
								+ id);
						break;
					}
					p2.clearBit(pbuf.readShort());
					p2.x = pbuf.readShort();
					p2.y = pbuf.readShort();
					break;
				case PLAYER_DIED:
					id = pbuf.readShort();
					p2 = GameObjects.players[id];
					p2.bubbleAlpha = 1.0f;
					p2.deaths = pbuf.readByte();
					p2.x = pbuf.readShort();
					p2.y = pbuf.readShort();
					if (id == NetworkHandler.id)
						IcePush.state = IcePush.DIED;
					break;
				case SET_CAN_MOVE:
					id = pbuf.readShort();
					p2 = GameObjects.players[id];
					if (p2 == null) { // ???????????????
						System.out
								.println("null player tried to set can move ??? "
										+ id);
						break;
					}
					p2.bubbleAlpha = pbuf.readByte() > 0 ? 0.0f : 1.0f;
					break;
				case PLAYER_LOGGED_OUT:
					id = pbuf.readShort();
					GameObjects.players[id] = null;
				case KEEP_ALIVE:
					break;
				case PING:
					System.out.println("Ping response recieved: "
							+ (System.currentTimeMillis() - pingTime));
					break;
			}
			pbuf.closePacket();
		}

	}

	public static int moveID;

	public static void sendMoveRequest(int dir) {
		if (IcePush.state != IcePush.PLAY)
			return;
		try {
			if (IcePush.DEBUG)
				System.out.println("SENDING MOVE REQUEST - ID: " + moveID
						+ " - DIR: " + dir + ", TIME: "
						+ System.currentTimeMillis());
			pbuf.beginPacket(MOVE_REQUEST);
			pbuf.writeByte(dir);
			pbuf.writeByte(moveID);
			pbuf.endPacket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Integer> getWorlds() {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		try {
			Socket s = new Socket(Opcodes.WORLDSERVER, 2346);
			InputStream in = s.getInputStream();
			int numWorlds = in.read();
			for (int i = 0; i < numWorlds; i++) {
				int strlen = in.read();
				byte[] strb = new byte[strlen];
				in.read(strb);
				String server = new String(strb);
				int num = in.read(); // num players
				ret.put(server, num);
			}
			GameObjects.serverMode = GameObjects.LIST_FROM_SERVER;
		} catch (Exception e) {
			GameObjects.loadingMessage = "Error getting server list";
			GameObjects.serverMode = GameObjects.TYPE_IN_BOX;
		}
		ret.put("localhost", 0);
		return ret;
	}

	public static void keepAlive() {
		if (IcePush.state == IcePush.WELCOME)
			return;
		if (pbuf == null)
			return;
		pbuf.beginPacket(KEEP_ALIVE);
		pbuf.endPacket();
	}

	public static void ping() {
		pingTime = System.currentTimeMillis();
		pbuf.beginPacket(PING);
		pbuf.endPacket();
	}

	public static void endMoveRequest(int moveDir) {
		if (IcePush.state != IcePush.PLAY)
			return;
		try {
			if (IcePush.DEBUG)
				System.out.println("ENDING MOVE REQUEST - DIR: " + moveDir + ", ID: " + moveID
						+ " - TIME: " + System.currentTimeMillis());
			pbuf.beginPacket(END_MOVE);
			pbuf.writeByte(moveDir);
			pbuf.writeByte(moveID);
			pbuf.endPacket();
			moveID = (moveID + 1) & 255;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void logOut() {
		try {
			if (pbuf == null)
				return;
			pbuf.beginPacket(LOGOUT);
			pbuf.closePacket();
			pbuf.synch();
			IcePush.state = IcePush.WELCOME;
			Renderer.message = "Select a username.";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
