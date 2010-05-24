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
	public double dx, dy;
	public int deaths;

	public Rectangle area;
	public PacketBuffer pbuf;

	public String username;
	public boolean connected;
	public boolean canMove = true;
	private int moveDir = 0;
	private int rotDir = 0;
	
	public int rotation;

	public Player() {

	}
	
	private void setBit(int flag, boolean rot) {
		if (rot)
			rotDir |= flag;
		else
			moveDir |= flag;
	}

	private void clearBit(int flag, boolean rot) {
		if (rot)
			rotDir &= ~flag;
		else
			moveDir &= ~flag;
	}

	private boolean isSet(int var, int flag) {
		return (var & flag) != 0;
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

		dx = dy = moveDir = 0;
	}

	public void handleMove() {
		if(rotDir != 0) {
			if(isSet(rotDir, LEFT))
				rotation = (rotation + 3) % 360;
			if(isSet(rotDir, RIGHT))
				rotation = (rotation - 3) % 360;

			for (Player plr : Server.players) {
				if (plr == null)
					continue;
				if (Server.DEBUG)
					System.out.println("SENDING ROTATE - " + id + " : "
							+ rotation);

				plr.pbuf.beginPacket(PLAYER_ROTATED); // player moved
				plr.pbuf.writeShort(id);
				plr.pbuf.writeShort(rotation);
				plr.pbuf.endPacket();
			}
		}
		
		if (moveDir != 0) {
			if(!canMove)
				return;
			double rad;
			if(isSet(moveDir, FORWARD)) {
				rad = rotation * Math.PI / 180;
				dx += Math.sin(rad);
				dy += Math.cos(rad);
			}
			if(isSet(moveDir, BACKWARD)) {
				rad = rotation * Math.PI / 180;
				dx -= Math.sin(rad);
				dy -= Math.cos(rad);
			}
			if(isSet(moveDir, UP))
				dy--;
			if(isSet(moveDir, DOWN))
				dy++;
			if(isSet(moveDir, LEFT))
				dx--;
			if(isSet(moveDir, RIGHT))
				dx++;
			
			Player p = getPlayerInWay();
			if (p != null && p.canMove) {
				p.moveDir = moveDir;
				//dx = dy = 0;
				//moveDir = -1;
				// TODO make better
				p.handleMove();
				return;
			} else if(p != null && !p.canMove) {
				dx = dy = moveDir = 0;
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
				dx = 0;
			if (dy != 0)
				dy = 0;
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
						setBit(pbuf.readByte(), false);
						int moveid = pbuf.readByte();
						if (Server.DEBUG)
							System.out.println("GOT MOVE REQUEST - DIR: "
									+ moveDir + " - ID = " + moveid
									+ " , TIME: " + System.currentTimeMillis());
						break;
					case END_MOVE:
						clearBit(pbuf.readByte(), false);
						if (Server.DEBUG)
							System.out.println("END MOVE REQUEST - ID = "
									+ pbuf.readByte() + " - TIME = "
									+ System.currentTimeMillis());
						break;
					case ROTATE_REQUEST:
						setBit(pbuf.readByte(), true);
						int rotid = pbuf.readByte();
						if (Server.DEBUG)
							System.out.println("GOT ROTATE REQUEST - DIR: "
									+ moveDir + " - ID = " + rotid
									+ " , TIME: " + System.currentTimeMillis());
						break;
					case END_ROTATE:
						clearBit(pbuf.readByte(), true);
						if (Server.DEBUG)
							System.out.println("END ROTATE REQUEST - ID = "
									+ pbuf.readByte() + " - TIME = "
									+ System.currentTimeMillis());
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
