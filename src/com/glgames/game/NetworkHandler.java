package com.glgames.game;

import static com.glgames.shared.Opcodes.BAD_VERSION;
import static com.glgames.shared.Opcodes.CHAT_REQUEST;
import static com.glgames.shared.Opcodes.END_MOVE;
import static com.glgames.shared.Opcodes.KEEP_ALIVE;
import static com.glgames.shared.Opcodes.LOGOUT;
import static com.glgames.shared.Opcodes.MOVE_REQUEST;
import static com.glgames.shared.Opcodes.NEW_CHAT_MESSAGE;
import static com.glgames.shared.Opcodes.NEW_PLAYER;
import static com.glgames.shared.Opcodes.PING;
import static com.glgames.shared.Opcodes.PLAYER_DIED;
import static com.glgames.shared.Opcodes.PLAYER_LOGGED_OUT;
import static com.glgames.shared.Opcodes.PLAYER_MOVED;
import static com.glgames.shared.Opcodes.SUCCESS_LOG;
import static com.glgames.shared.Opcodes.TOO_MANY_PL;
import static com.glgames.shared.Opcodes.UPDATE;
import static com.glgames.shared.Opcodes.USER_IN_USE;
import static com.glgames.shared.Opcodes.VERSION;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.glgames.game.ui.*;
import com.glgames.shared.ILoader;
import com.glgames.shared.Opcodes;
import com.glgames.shared.PacketBuffer;

public class NetworkHandler {
	static String DEFAULT_SERVER = "strictfp.com";
	// The id of the connected player
	public static int id;

	private static Socket sock;
	private static PacketBuffer pbuf;
	private static long pingTime;

	public static Action<Button> onLoginButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			String server = "";
			if (GameObjects.serverMode == GameObjects.LIST_FROM_SERVER) {
				server = GameObjects.ui.serverList.getSelected();
			} else if (GameObjects.serverMode == GameObjects.TYPE_IN_BOX) {
				server = GameObjects.ui.serverTextBox.getText();
			} else {
				server = DEFAULT_SERVER;
			}
			if (!server.isEmpty()) {
				Renderer.message = "Logging in...";
				NetworkHandler.login(server, GameObjects.ui.usernameTextBox.getText());
				GameObjects.ui.setVisibleRecursive(false);
				GameObjects.ui.setVisible(true);
				GameObjects.ui.logoutButton.setVisible(true);
			}
		}
	};

	public static Action<Button> onLogoutButtonClick = new Action<Button>() {
		public void doAction(Button component, int x, int y) {
			NetworkHandler.logOut();
			GameObjects.ui.setVisibleRecursive(true);
			GameObjects.ui.backButton.setVisible(false);
			GameObjects.ui.logoutButton.setVisible(false);

		}
	};

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
				GameObjects.players = new Player[50];
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
		Player plr;
		while ((opcode = pbuf.openPacket()) != -1) {
			switch (opcode) {
				case NEW_PLAYER:
					id = pbuf.readShort();
					type = pbuf.readByte(); // snowman or tree??
					username = pbuf.readString();
					x = pbuf.readShort();
					y = pbuf.readShort();
					int deaths = pbuf.readShort();
					plr = new Player(type, username);
					plr.setPos(x, y);
					plr.username = username;
					plr.deaths = deaths;
					GameObjects.players[id] = plr;
					break;
				case PLAYER_MOVED:
					id = pbuf.readShort(); // player ID
					x = pbuf.readShort(); // x player will stop at
					y = pbuf.readShort(); // y player will stop at
					plr = GameObjects.players[id];
					if (plr == null) {
						System.out
								.println("null player tried to move??? " + id);
						break;
					}
					plr.setPos(x, y);
					break;
				case PLAYER_DIED:
					id = pbuf.readShort();
					plr = GameObjects.players[id];
					if (plr == null)
						break;
					plr.bubbleAlpha = 1.0f;
					plr.deaths = pbuf.readByte();
					x = pbuf.readShort();
					y = pbuf.readShort();
					plr.setPos(x, y);
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
				case NEW_CHAT_MESSAGE:
					String msg = pbuf.readString();
					Renderer.chats.add(msg);
					break;

				case UPDATE:
					try {
						Class<?> clazz = NetworkHandler.class.getClassLoader()
								.loadClass("loader");
						ILoader loader = (ILoader) clazz.newInstance();
						loader.restart();
					} catch (Exception e) {
						e.printStackTrace();
					}
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

	public static void sendChatMessage(String msg) {
		try {
			pbuf.beginPacket(CHAT_REQUEST);
			pbuf.writeString(msg);
			pbuf.endPacket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Integer> getWorlds() {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		try {
			Socket s = new Socket(IcePush.DEBUG ? "localhost"
					: Opcodes.WORLDSERVER, 2346);
			s.getOutputStream().write(Opcodes.NUM_PLAYERS_REQUEST);
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
			ret.put("localhost@127.0.0.1", 0);
		} catch (Exception e) {
			e.printStackTrace();
			GameObjects.serverMode = GameObjects.TYPE_IN_BOX;
		}
		return ret;
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
				System.out.println("ENDING MOVE REQUEST - DIR: " + moveDir
						+ ", ID: " + moveID + " - TIME: "
						+ System.currentTimeMillis());
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
			pbuf.endPacket();
			pbuf.synch();
			IcePush.state = IcePush.WELCOME;
			Renderer.message = "Select a username.";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
