package com.glgames.server;

import static com.glgames.shared.Opcodes.BAD_VERSION;
import static com.glgames.shared.Opcodes.END_MOVE;
import static com.glgames.shared.Opcodes.LOGOUT;
import static com.glgames.shared.Opcodes.MOVE_REQUEST;
import static com.glgames.shared.Opcodes.NEW_PLAYER;
import static com.glgames.shared.Opcodes.PLAYER_DIED;
import static com.glgames.shared.Opcodes.PLAYER_LOGGED_OUT;
import static com.glgames.shared.Opcodes.PLAYER_MOVED;
import static com.glgames.shared.Opcodes.SUCCESS_LOG;
import static com.glgames.shared.Opcodes.TOO_MANY_PL;
import static com.glgames.shared.Opcodes.USER_IN_USE;

import java.awt.Rectangle;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.glgames.shared.PacketBuffer;

public class Player {
	public static final int UP = 1 << 0;
	public static final int DOWN = 1 << 1;
	public static final int LEFT = 1 << 2;
	public static final int RIGHT = 1 << 3;

	public int id;
	public int type;
	public int dx, dy;
	public int deaths;

	public Rectangle area;
	public PacketBuffer pbuf;

	private String username;
	public boolean connected;
	private int moveDir = -1;

	public Player(Socket s) {
		try {
			InputStream in = s.getInputStream();
			OutputStream out = s.getOutputStream();
			int version = in.read();
			if (version != Server.VERSION) {
				out.write(BAD_VERSION); // bad version
				out.flush();
				return;
			}
			int len = in.read();
			byte[] strb = new byte[len];
			in.read(strb);

			username = new String(strb);

			for (int k = 0; k < Server.players.length; k++)
				if (Server.players[k] != null) {
					String user = Server.players[k].username;
					if (user != null && user.equals(username)) {
						out.write(USER_IN_USE); // name in use
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
				out.write(TOO_MANY_PL);
				out.flush();
				return;
			}

			id = index;
			type = index % 2;

			initPosition();
			out.write(SUCCESS_LOG); // success
			out.write(id);
			out.flush();

			pbuf = new PacketBuffer(s);
			Server.players[id] = this;
			connected = true;
			System.out.println("Player logged in: " + username + ", id: " + id);

			for (Player plr : Server.players) {
				try {
					if (plr == null)
						continue;
					// Tell this client about that player...
					pbuf.beginPacket(NEW_PLAYER);
					pbuf.writeShort(plr.id);
					pbuf.writeByte(plr.type);
					pbuf.writeString(plr.username);
					pbuf.writeShort(plr.area.x);
					pbuf.writeShort(plr.area.y);
					pbuf.endPacket();

					// Tell that client about this player
					plr.pbuf.beginPacket(NEW_PLAYER); // new player has entered
					plr.pbuf.writeShort(id);
					plr.pbuf.writeByte(type);
					plr.pbuf.writeString(username);
					plr.pbuf.writeShort(area.x); // x
					plr.pbuf.writeShort(area.y); // y
					plr.pbuf.endPacket();
				} catch (Exception e) {
					// TODO better error handling
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initPosition() {
		area = new Rectangle(0, 0, 48, 48);
		area.x = (int) (Math.random() * (400 - 48));
		area.y = (int) (Math.random() * (400 - 48));

		dx = dy = 0;
		moveDir = -1;
	}

	private boolean inHandleCall = false;

	public void handleMove() {
		if(inHandleCall) {
			System.out.println("Deferred infinite recursion in handler for " + username);
			return;
		}
		inHandleCall = true;
		try {
			if (moveDir != -1) {
				switch (moveDir) {
					case UP: // up
						area.y--;
						break;
					case DOWN: // down
						area.y++;
						break;
					case LEFT: // left
						area.x--;
						break;
					case RIGHT: // right
						area.x++;
						break;
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

				if (area.x < 0 - 10 || area.x > 400 + 10 || area.y < 0 - 10
						|| area.y > 400 + 10)
					playerDied();

				for (Player plr : Server.players) {
					if (plr == null)
						continue;
					if (Server.DEBUG)
						System.out.println("SENDING MOVE - " + id + " : "
								+ area.x + ", " + area.y);

					plr.pbuf.beginPacket(PLAYER_MOVED); // player moved
					plr.pbuf.writeShort(id);
					plr.pbuf.writeShort(area.x);
					plr.pbuf.writeShort(area.y);
					plr.pbuf.endPacket();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			inHandleCall = false;
		}
	}

	private void playerDied() {
		try {
			deaths++;
			initPosition();
			for (Player plr : Server.players) {
				if (plr == null)
					continue;
				plr.pbuf.beginPacket(PLAYER_DIED);
				plr.pbuf.writeShort(id);
				plr.pbuf.writeByte(deaths);
				plr.pbuf.writeShort(area.x); // new location
				plr.pbuf.writeShort(area.y);
				plr.pbuf.endPacket();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processIncomingPackets() {
		try {
			if (!pbuf.synch()) {
				logout(); // Log out player if connection has been lost
				return;
			}
			int opcode;
			while ((opcode = pbuf.openPacket()) != -1) {
				switch (opcode) {
					case MOVE_REQUEST:
						moveDir = pbuf.readByte();
						int moveid = pbuf.readByte();
						if (Server.DEBUG)
							System.out.println("GOT MOVE REQUEST - DIR: "
									+ moveDir + " - ID = " + moveid
									+ " , TIME: " + System.currentTimeMillis());
						break;
					case END_MOVE:
						moveDir = -1;
						if (Server.DEBUG)
							System.out.println("END MOVE REQUEST - ID = "
									+ pbuf.readByte() + " - TIME = "
									+ System.currentTimeMillis());
						break;
					case LOGOUT:
						logout();
						break;
				}
				pbuf.closePacket();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void logout() {
		try {
			connected = false;
			Server.players[id] = null;

			for (Player plr : Server.players) {
				if (plr == null || plr == this)
					continue;

				plr.pbuf.beginPacket(PLAYER_LOGGED_OUT);
				plr.pbuf.writeShort(id);
				plr.pbuf.endPacket();
			}
			System.out.println("Logged out: " + id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Player getPlayerInWay() {
		Rectangle newArea = new Rectangle(area.x + dx, area.y + dy, 48, 48);
		for(Player pl : Server.players) {
			if(pl == null || pl == this)
				continue;
			
			if(pl.area.intersects(newArea))
				return pl;
		}
		return null;
	}
}
