package com.glgames.game;

import static com.glgames.shared.Opcodes.BAD_VERSION;
import static com.glgames.shared.Opcodes.END_MOVE;
import static com.glgames.shared.Opcodes.KEEP_ALIVE;
import static com.glgames.shared.Opcodes.LOGOUT;
import static com.glgames.shared.Opcodes.MOVE_REQUEST;
import static com.glgames.shared.Opcodes.NEW_PLAYER;
import static com.glgames.shared.Opcodes.PLAYER_DIED;
import static com.glgames.shared.Opcodes.PLAYER_LOGGED_OUT;
import static com.glgames.shared.Opcodes.PLAYER_MOVED;
import static com.glgames.shared.Opcodes.SET_CAN_MOVE;
import static com.glgames.shared.Opcodes.SUCCESS_LOG;
import static com.glgames.shared.Opcodes.TOO_MANY_PL;
import static com.glgames.shared.Opcodes.TREE;
import static com.glgames.shared.Opcodes.USER_IN_USE;
import static com.glgames.shared.Opcodes.VERSION;

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

	public static void login(String server, String username) {
		try {
			sock = new Socket(server, 2345);
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
				GraphicsMethods.message = "That username is in use.";
			} else if (result == BAD_VERSION) {
				GraphicsMethods.message = "The game has been updated, refresh the page.";
			} else if (result == TOO_MANY_PL) {
				GraphicsMethods.message = "Too many players are logged in.";
			} else if (result == SUCCESS_LOG) {
				// Successful login
				id = in.read();
				pbuf = new PacketBuffer(sock);
				KeyHandler.isMoving = false;
				GameObjects.players = new GamePlayer[50];
				GameEngine.state = GameEngine.PLAY;
			} else {
				GraphicsMethods.message = "Invalid response from server.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			GraphicsMethods.message = "Error connecting to server: "
					+ e.getMessage();
		}
	}

	public static void handlePackets() {
		if (GameEngine.state == GameEngine.WELCOME)
			return;

		if(!pbuf.synch()) {
			GameEngine.state = GameEngine.WELCOME;
			GraphicsMethods.message = "Connection with server was lost";
			return;
		}

		int opcode;
		int id, type, x, y;
		String username;
		GamePlayer plr;
		while ((opcode = pbuf.openPacket()) != -1) {
			switch (opcode) {
				case NEW_PLAYER:
					id = pbuf.readShort();
					type = pbuf.readByte(); // snowman or tree??
					username = pbuf.readString();
					x = pbuf.readShort();
					y = pbuf.readShort();

					GameObjects.players[id] = new GamePlayer(
							type == TREE ? "images/tree.png"
									: "images/snowman.png");
					GameObjects.players[id].area.x = x;
					GameObjects.players[id].area.y = y;
					GameObjects.players[id].username = username;
					break;
				case PLAYER_MOVED:
					id = pbuf.readShort();
					x = pbuf.readShort();
					y = pbuf.readShort();

					plr = GameObjects.players[id];
					if(plr == null) {	// ???????????????
						System.out.println("null player tried to move??? " + id);
						break;
					}
					if(!plr.canMove) // just in case packet was dropped
						plr.canMove = true;
					plr.area.x = x;
					plr.area.y = y;
					break;
				case PLAYER_DIED:
					id = pbuf.readShort();
					plr = GameObjects.players[id];
					plr.canMove = false;
					plr.deaths = pbuf.readByte();
					plr.area.x = pbuf.readShort();
					plr.area.y = pbuf.readShort();
					if(id == NetworkHandler.id)
						GameEngine.state = GameEngine.DIED;
					break;
				case SET_CAN_MOVE:
					id = pbuf.readShort();
					plr = GameObjects.players[id];
					if(plr == null) {	// ???????????????
						System.out.println("null player tried to set can move ??? " + id);
						break;
					}
					plr.canMove = pbuf.readByte() > 0 ? true : false;
					break;
				case PLAYER_LOGGED_OUT:
					id = pbuf.readShort();
					GameObjects.players[id] = null;
				case KEEP_ALIVE:
					break;
			}
			pbuf.closePacket();
		}

	}

	public static int moveID;

	public static void sendMoveRequest(int dir) {
		if (GameEngine.state != GameEngine.PLAY)
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
	
	public static Map<String, Integer> getWorlds() throws Exception {
		Map<String, Integer> ret = new HashMap<String, Integer>();
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
		ret.put("localhost", 0);
		return ret;
	}

	public static void keepAlive() {
		if (GameEngine.state == GameEngine.WELCOME)
			return;
		if (pbuf == null)
			return;
		pbuf.beginPacket(KEEP_ALIVE);
		pbuf.endPacket();
	}

	public static void endMoveRequest() {
		if (GameEngine.state != GameEngine.PLAY)
			return;
		try {
			if (IcePush.DEBUG)
				System.out.println("ENDING MOVE REQUEST - ID: " + moveID
						+ " - TIME: " + System.currentTimeMillis());
			pbuf.beginPacket(END_MOVE);
			pbuf.writeByte(moveID);
			pbuf.endPacket();
			moveID = (moveID + 1) & 255;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void logOut() {
		try {
			if(pbuf == null)
				return;
			pbuf.beginPacket(LOGOUT);
			pbuf.endPacket();
			pbuf.synch();
			GameEngine.state = GameEngine.WELCOME;
			GraphicsMethods.message = "Select a server and username.";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
