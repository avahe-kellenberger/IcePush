package com.glgames.server;

import static com.glgames.shared.Opcodes.END_MOVE;
import static com.glgames.shared.Opcodes.KEEP_ALIVE;
import static com.glgames.shared.Opcodes.LOGOUT;
import static com.glgames.shared.Opcodes.MOVE_REQUEST;
import static com.glgames.shared.Opcodes.NEW_PLAYER;
import static com.glgames.shared.Opcodes.PLAYER_DIED;
import static com.glgames.shared.Opcodes.PLAYER_LOGGED_OUT;
import static com.glgames.shared.Opcodes.PLAYER_MOVED;
import static com.glgames.shared.Opcodes.SET_CAN_MOVE;

import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;

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

	public String username;
	public boolean connected;
	public boolean canMove = true;
	private int moveDir = -1;

	public Player() {

	}
	
	public void notifyLogin() {
		for (Player plr : Server.players) {
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
		}
	}

	public void keepAlive() {
		pbuf.beginPacket(KEEP_ALIVE);
		pbuf.endPacket();
	}

	void initPosition() {
		area = new Rectangle(0, 0, 48, 48);
		area.x = (int) (Math.random() * (400 - 48));
		area.y = (int) (Math.random() * (400 - 48));

		dx = dy = 0;
		moveDir = -1;
	}

	public void handleMove() {
		if (moveDir != -1) {
			if(!canMove)
				return;
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
			
			Player p = getPlayerInWay();
			if (p != null && p.canMove) {
				p.moveDir = moveDir;
				//dx = dy = 0;
				//moveDir = -1;
				// TODO make better
				p.handleMove();
				return;
			}
			
			if(dx > 4)
				dx = 4;
			if(dy > 4)
				dy = 4;
			if(dx < -4)
				dx = -4;
			if(dy < -4)
				dy = -4;
		} else {
			// moveDir == -1
			if (dx != 0)
				dx += (dx < 0) ? 1 : -1;
			if (dy != 0)
				dy += (dy < 0) ? 1 : -1;
		}
		if(dx != 0 || dy != 0) {
			area.x += dx;
			area.y += dy;

			if (area.x < 0 - 10 || area.x > 400 + 10 || area.y < 0 - 10
					|| area.y > 400 + 10) {
				playerDied();
				return;
			}
			
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
	}

	private void playerDied() {
		try {
			deaths++;
			initPosition();
			delayCanMove();
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
	
	private void delayCanMove() {
		setCanMove(false);
		final Timer t = new Timer();
		t.schedule(new TimerTask() {
			public void run() {
				setCanMove(true);
				t.cancel();
			}
		}, 3000, 1000);
	}

	private void setCanMove(boolean can) {
		canMove = can;
		for (Player plr : Server.players) {
			if (plr == null)
				continue;
			plr.pbuf.beginPacket(SET_CAN_MOVE);
			plr.pbuf.writeShort(id);
			plr.pbuf.writeByte(canMove ? 1 : 0);
			plr.pbuf.endPacket();
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
					case KEEP_ALIVE:
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
