package com.glgames.game;

import static com.glgames.shared.Opcodes.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class NetworkHandler {
	// The id of the connected player
	public static int id;

	private static Socket sock;

	private static DataInputStream in;
	private static DataOutputStream out;

	public static void login(String server, String username) {
		try {
			sock = new Socket(server, 2345);
			sock.setTcpNoDelay(true);

			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());

			out.writeShort(VERSION);
			out.writeUTF(username);
			out.flush();

			int result = in.readByte();
			if (result == USER_IN_USE) {
				GraphicsMethods.message = "That username is in use.";
			} else if (result == BAD_VERSION) {
				GraphicsMethods.message = "The game has been updated, refresh the page.";
			} else if (result == TOO_MANY_PL) {
				GraphicsMethods.message = "Too many players are logged in.";
			} else if (result == SUCCESS_LOG) {
				// Successful login
				id = in.readShort();
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
		try {
			if (GameEngine.state == GameEngine.WELCOME || in.available() == 0)
				return;

			int opcode = in.readByte();
			int id, type, x, y;
			String username;
			GamePlayer plr;
			switch (opcode) {
			case NEW_PLAYER:
				id = in.readShort();
				type = in.readByte(); // snowman or tree??
				username = in.readUTF();
				x = in.readShort();
				y = in.readShort();

				GameObjects.players[id] = new GamePlayer(
						type == TREE ? "images/tree.png" : "images/snowman.png");
				GameObjects.players[id].area.x = x;
				GameObjects.players[id].area.y = y;
				GameObjects.players[id].username = username;
				break;
			case PLAYER_MOVED:
				id = in.readShort();
				x = in.readShort();
				y = in.readShort();

				plr = GameObjects.players[id];
				plr.area.x = x;
				plr.area.y = y;
				break;
			case PLAYER_DIED:
				id = in.readShort();
				plr = GameObjects.players[id];
				if (id == NetworkHandler.id)
					GameEngine.state = GameEngine.DIED;
				plr.deaths = in.readByte();
				plr.area.x = in.readShort();
				plr.area.y = in.readShort();
				break;
			case PLAYER_LOGGED_OUT:
				id = in.readShort();
				GameObjects.players[id] = null;
			case KEEP_ALIVE:
				break;
			}
		} catch (Exception e) {
			GraphicsMethods.message = e.toString();
			GameEngine.state = GameEngine.WELCOME;
		}
	}

	public static void sendMoveRequest(int dir) {
		if (GameEngine.state != GameEngine.PLAY)
			return;
		try {
			out.writeByte(MOVE_REQUEST);
			out.writeByte(dir);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void endMoveRequest() {
		if (GameEngine.state != GameEngine.PLAY)
			return;
		try {
			out.writeByte(END_MOVE);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void logOut() {
		if (GameEngine.state != GameEngine.PLAY)
			return;
		try {
			out.writeByte(LOGOUT);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
