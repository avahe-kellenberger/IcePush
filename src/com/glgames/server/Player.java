package com.glgames.server;

import static com.glgames.shared.Opcodes.*;

import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;

import com.glgames.shared.PacketBuffer;

public class Player {
	public static final int FORWARD = 1 << 0;
	public static final int BACKWARD = 1 << 1;
	public static final int LEFT = 1 << 2;
	public static final int RIGHT = 1 << 3;
	public static final int UP = 1 << 4;
	public static final int DOWN = 1 << 5;

	public int id;
	public int type;
	public int dx, dy;
	public int deaths;

	public Rectangle area;
	public PacketBuffer pbuf;

	public String username;
	public boolean connected;
	public boolean canMove = true;

	public Player() {

	}

/*	private void setBit(int flag) {
		moveDir |= flag;
	}

	private void clearBit(int flag) {
		moveDir &= ~flag;
	}

	private boolean isSet(int flag) {
		return (moveDir & flag) != 0;
	}*/

	
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
			pbuf.writeShort(plr.deaths);
			pbuf.endPacket();

			// Tell that client about this player
			plr.pbuf.beginPacket(NEW_PLAYER); // new player has entered
			plr.pbuf.writeShort(id);
			plr.pbuf.writeByte(type);
			plr.pbuf.writeString(username);
			plr.pbuf.writeShort(area.x); // x
			plr.pbuf.writeShort(area.y); // y
			plr.pbuf.writeShort(plr.deaths);
			plr.pbuf.endPacket();
		}
	}

	public void keepAlive() {
		pbuf.beginPacket(KEEP_ALIVE);
		pbuf.endPacket();
	}

	void initPosition() {
		Rectangle r;
		boolean good = false;
		while(true) {
			good = true;
			r = new Rectangle((int) (Math.random() * (400 - 48)), (int) (Math
					.random() * (400 - 48)), 48, 48);
			for(Player p : Server.players) {
				if(p == null)
					continue;
				if(p.area.intersects(r))
					good = false;
			}
			if(good)
				break;
		}
		area = r;

		dx = dy = 0;
	}

	public void handleMove() {
		if(!canMove)
			return;			
		Player p = getPlayerInWay();
		if (p != null) {
			if(p.canMove) {
				p.dx = dx;
				p.dy = dy;
				// TODO make better
				p.handleMove();
				p.updatePos();
			} else {
				dx = dy = 0;
				updatePos();
			}
		}
			
		if(dx > 4)
			dx = 4;
		if(dy > 4)
			dy = 4;
		if(dx < -4)
			dx = -4;
		if(dy < -4)
				dy = -4;

		if((dx | dy) != 0) {
			area.x += dx;
			area.y += dy;

			if (area.x < 0 - 10 || area.x > 400 + 10 || area.y < 0 - 10
					|| area.y > 400 + 10) {
				playerDied();
				return;
			}
		}
	}

	private void updatePos() {
		for(Player plr: Server.players) if(plr != null) {
			if((dx | dy) == 0) {		// dx and dy are both zero
				System.out.println("STOPPING PLAYER AT " + area.x + ", " + area.y);
				plr.pbuf.beginPacket(PLAYER_STOPPED_MOVING);
				plr.pbuf.writeShort(id);
				plr.pbuf.writeShort(area.x);
				plr.pbuf.writeShort(area.y);
				plr.pbuf.endPacket();
			} else {
				int destX = area.x + 300 * dx;
				int destY = area.y + 300 * dy;
				System.out.println("SENDING PLAYER_MOVED: username = " + username + " destX=" + destX + " destY=" + destY);
				plr.pbuf.beginPacket(PLAYER_MOVED);
				plr.pbuf.writeShort(id);
				plr.pbuf.writeShort(destX);
				plr.pbuf.writeShort(destY);
				plr.pbuf.writeShort(9000);
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
						int moveDir = pbuf.readByte();
						int moveid = pbuf.readByte();
						if (Server.DEBUG)
							System.out.println("GOT MOVE REQUEST - DIR: "
									+ moveDir + " - ID = " + moveid
									+ " , TIME: " + System.currentTimeMillis());
						if(moveDir == UP) {
							dy = -4;
						} else if(moveDir == DOWN) {
							dy = 4;
						} else if(moveDir == RIGHT) {
							dx = 4;
						} else if(moveDir == LEFT) {
							dx = -4;
						}
						updatePos();
						break;
					case END_MOVE:
						int moveBit = pbuf.readByte();
						int debugBullshit = pbuf.readByte();
						//if (Server.DEBUG)
							System.out.println("END MOVE REQUEST - ID = "
									+ debugBullshit + " - TIME = "
									+ System.currentTimeMillis());
						if(moveBit == UP || moveBit == DOWN) dy = 0;
						if(moveBit == LEFT || moveBit == RIGHT) dx = 0;
						System.out.println("NEW DX = " + dx + " NEW DY = " + dy);
						updatePos();
						break;
					case LOGOUT:
						logout();
						break;
					case KEEP_ALIVE:
						break;
					case PING:
						pbuf.beginPacket(PING);
						pbuf.endPacket();
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
		Rectangle newArea = new Rectangle(area.x + (int) dx, area.y + (int) dy, 48, 48);
		for(Player pl : Server.players) {
			if(pl == null || pl == this)
				continue;
			
			if(pl.area.intersects(newArea))
				return pl;
		}
		return null;
	}
}
