package com.glgames.server;

import static com.glgames.shared.Opcodes.*;

import java.awt.Rectangle;

import com.glgames.shared.PacketBuffer;

public class Player {
	public int id;
	public int type;
	public int dx, dy;	// **** //
	private int xAccel, yAccel;
	public int deaths;

	public Rectangle area;
	public PacketBuffer pbuf;

	public String username;
	public boolean connected;
	public boolean canMove = true;
	private long timeOfDied = 0;

	public Player() {

	}

	public final static int SCALE = 2 << 6;

	private boolean inHandleMove = false;
	
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
			r = new Rectangle((int) (Math.random() * (744 - 48)), (int) (Math
					.random() * (422 - 48)), 48, 48);
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
		dx = dy = xAccel = yAccel = 0;
	}

	private void setBit(int bit) {
		if(bit == UP) yAccel = -(SCALE / 2);
		if(bit == DOWN) yAccel = SCALE / 2;
		if(bit == LEFT) xAccel = -(SCALE / 2);
		if(bit == RIGHT) xAccel = SCALE / 2;
	}

	private void clearBit(int bit) {
		if(bit == UP) yAccel = 0;
		if(bit == DOWN) yAccel = 0;
		if(bit == LEFT) xAccel = 0;
		if(bit == RIGHT) xAccel = 0;
	}

	public void handleMove() {
		if(timeOfDied != 0) {
			if(System.currentTimeMillis() - timeOfDied > 3000) { // The last cycle of this players died time
				setCanMove(true);
				timeOfDied = 0;
				dx = dy = xAccel = yAccel = 0;		// Fix entropic movement after becoming undead
			} else {
				return;		// This player is currently died
			}
		}

		if(inHandleMove) return;

		inHandleMove = true;

		Player o;
		if((o = getPlayerInWay()) != null) {
			if(o.timeOfDied == 0) {
				o.dx = dx;
				o.dy = dy;
				o.handleMove();	
			} else {
				dx = dy = 0;
			}
		}
		
		dx = ((dx + xAccel) * 23) / 24;
		dy = ((dy + yAccel) * 23) / 24;

		if(area.x < -21 || area.y < -19 || area.x > 772 || area.y > 452) {
			playerDied();
			inHandleMove = false;
			return;
		}
		
		if(dx != 0 || dy != 0) {
			for(Player p : Server.players) if(p != null) {
				p.pbuf.beginPacket(PLAYER_MOVED);
				p.pbuf.writeShort(id);
				p.pbuf.writeShort(area.x);
				p.pbuf.writeShort(area.y);
				p.pbuf.endPacket();
			}
		}

		area.x += (dx / SCALE);
		area.y += (dy / SCALE);

		inHandleMove = false;
	}

	private void playerDied() {
		try {
			deaths++;
			setCanMove(false);
			initPosition();
			timeOfDied = System.currentTimeMillis();
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
			int opcode, moveDir, moveId;
			while ((opcode = pbuf.openPacket()) != -1) {
				switch (opcode) {
					case MOVE_REQUEST:
						moveDir = pbuf.readByte();
						moveId = pbuf.readByte();
						if (Server.DEBUG)
							System.out.println("GOT MOVE REQUEST - DIR: "
									+ moveDir + " - ID = " + moveId
									+ " , TIME: " + System.currentTimeMillis());
						setBit(moveDir);
						break;
					case END_MOVE:
						moveDir = pbuf.readByte();
						moveId = pbuf.readByte();
						if (Server.DEBUG)
							System.out.println("END MOVE REQUEST - ID = "
									+ moveId + " - TIME = "
									+ System.currentTimeMillis());
						clearBit(moveDir);
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
		Server.server.updateWorldServer();
	}

	public Player getPlayerInWay() {
		Rectangle newArea = new Rectangle(area.x + (dx / SCALE), area.y + (dy / SCALE), 48, 48); // OMG HOLY SHIT CRAP WTF I OVERLOOKED THIS FOR ALMOST A DAY
		for(Player pl : Server.players) {
			if(pl == null || pl == this)
				continue;
			
			if(pl.area.intersects(newArea))
				return pl;
		}
		return null;
	}
}
