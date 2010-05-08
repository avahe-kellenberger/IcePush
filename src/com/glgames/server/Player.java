package com.glgames.server;

import static com.glgames.shared.Opcodes.*;

import java.awt.Rectangle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Player implements Runnable {
	public static final int UP = 1 << 0;
	public static final int DOWN = 1 << 1;
	public static final int LEFT = 1 << 2;
	public static final int RIGHT = 1 << 3;

	public int id;
	public int type;
	public int dx, dy;
	public int deaths;

	public Rectangle area;

	private DataInputStream in;
	private DataOutputStream out;
	private String username;
	private boolean connected;
	private int moveDir = -1;

	public Player(Socket s) {
		try {
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			int version = in.readShort();
			if (version != Server.VERSION) {
				out.writeByte(BAD_VERSION); // bad version
				out.flush();
				return;
			}
			username = in.readUTF();
			for (int k = 0; k < Server.players.length; k++)
				if (Server.players[k] != null) {
					String user = Server.players[k].username;
					if (user != null && user.equals(username)) {
						out.writeByte(USER_IN_USE); // name in use
						out.flush();
						return;
					}
				}
			int index = -1;
			for (int k = 0; k < Server.players.length; k++)
				if (Server.players[k] == null) {
					index = k;
					break;
				}
			if (index == -1) {
				out.writeByte(TOO_MANY_PL);
				out.flush();
				return;
			}

			id = index;
			type = index % 2;

			initPosition();
			out.writeByte(SUCCESS_LOG); // success
			out.writeShort(id);
			out.flush();

			Server.players[id] = this;
			connected = true;
			System.out.println("Player logged in: " + username + ", id: " + id);

			for (Player plr : Server.players) {
				try {
					if (plr == null)
						continue;
					// Tell this client about that player...
					out.writeByte(NEW_PLAYER);
					out.writeShort(plr.id);
					out.writeByte(plr.type);
					out.writeUTF(plr.username);
					out.writeShort(plr.area.x);
					out.writeShort(plr.area.y);
					out.flush();

					// Tell that client about this player
					plr.out.writeByte(NEW_PLAYER); // new player has entered
					plr.out.writeShort(id);
					plr.out.writeByte(type);
					plr.out.writeUTF(username);
					plr.out.writeShort(area.x); // x
					plr.out.writeShort(area.y); // y
					plr.out.flush();
				} catch (Exception e) {
					// TODO better error handling
					continue;
				}
			}

			while (connected) {
				processIncomingPackets();
				handleMove();
				try {
					Thread.sleep(20);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logout();
		}
	}

	private void initPosition() {
		area = new Rectangle(0, 0, 48, 48);
		area.x = (int) (Math.random() * (400 - 48));
		area.y = (int) (Math.random() * (400 - 48));

		dx = dy = 0;
		moveDir = -1;
	}

	private void handleMove() throws Exception {
		if (moveDir != -1) {
			switch (moveDir) {
			case UP: // up
				dy--;
				break;
			case DOWN: // down
				dy++;
				break;
			case LEFT: // left
				dx--;
				break;
			case RIGHT: // right
				dx++;
				break;
			}

			if (dx > 5)
				dx = 5;
			if (dy > 5)
				dy = 5;
			if (dx < -5)
				dx = -5;
			if (dy < -5)
				dy = -5;
		}

		if (dx != 0 || dy != 0) {
			if (moveDir == -1) {
				if (dx > 0)
					dx--;
				if (dy > 0)
					dy--;
				if (dx < 0)
					dx++;
				if (dy < 0)
					dy++;
			}

			Player p = getPlayerInWay();
			if (p != null) {
				p.moveDir = moveDir;
				dx = dy = 0;
				moveDir = -1;
				// TODO make better
				p.handleMove();
				return;
			}

			area.x += dx;
			area.y += dy;

			if (area.x < 0 - 10 || area.x > 400 + 10 || area.y < 0 - 10
					|| area.y > 400 + 10)
				playerDied();

			for (Player plr : Server.players) {
				if (plr == null)
					continue;

				plr.out.writeByte(PLAYER_MOVED); // player moved
				plr.out.writeShort(id);
				plr.out.writeShort(area.x);
				plr.out.writeShort(area.y);
				plr.out.flush();
			}
		}
	}

	private void playerDied() throws Exception {
		deaths++;
		initPosition();
		for (Player plr : Server.players) {
			if (plr == null)
				continue;
			plr.out.writeByte(PLAYER_DIED); // died
			plr.out.writeShort(id);
			plr.out.writeByte(deaths);
			plr.out.writeShort(area.x); // new location
			plr.out.writeShort(area.y);
			plr.out.flush();
		}
	}

	private void processIncomingPackets() throws Exception {
		if (in.available() == 0)
			return;

		int opcode = in.readByte();
		switch (opcode) {
		case MOVE_REQUEST:
			moveDir = in.readByte();
			break;
		case END_MOVE:
			moveDir = -1;
			break;
		case LOGOUT:
			logout();
			break;
		}
	}

	private void logout() {
		try {
			connected = false;
			Server.players[id] = null;

			for (Player plr : Server.players) {
				if (plr == null || plr == this)
					continue;

				plr.out.writeByte(PLAYER_LOGGED_OUT);
				plr.out.writeShort(id);
				plr.out.flush();
			}
			System.out.println("Logged out: " + id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Player getPlayerInWay() {
		Rectangle newArea = new Rectangle(area.x + dx, area.y + dy, 48, 48);
		for (Player pl : Server.players) {
			if (pl == null || pl == this)
				continue;

			if (pl.area.intersects(newArea))
				return pl;
		}
		return null;
	}
}
