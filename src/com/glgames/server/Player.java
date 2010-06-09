package com.glgames.server;

import static com.glgames.shared.Opcodes.*;

import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;

import com.glgames.shared.PacketBuffer;

public class Player {
	public int id;
	public int type;
	public int dx, dy, moveDir;
	public int deaths;

	public Rectangle area;
	public PacketBuffer pbuf;

	public String username;
	public boolean connected;
	public boolean canMove = true;

	public Player() {

	}

	private void setBit(int flag) {
		moveDir |= flag;
	}

	private void clearBit(int flag) {
		moveDir &= ~flag;
	}

	private boolean isSet(int flag) {
		return (moveDir & flag) != 0;
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
		moveDir = 0;
		dx = dy = 0;
	}

	public void handleMove() {
		if(!canMove)
			return;			
		
		if(isSet(UP))
			dy--;
		if(isSet(DOWN))
			dy++;
		if(isSet(LEFT))
			dx--;
		if(isSet(RIGHT))
			dx++;
		
		dx = dx > 4 ? 4 : dx;
		dy = dy > 4 ? 4 : dy;
		dx = dx < -4 ? -4 : dx;
		dy = dy < -4 ? -4 : dy;

		Player o;
		if((o = getPlayerInWay()) != null) {
			o.moveDir = moveDir;
			return;
		}
		
		area.x += dx;
		area.y += dy;
		
		if(area.x < 0 || area.y < 0 || area.x > 400 || area.y > 400)
			playerDied();
		
		for(Player p : Server.players) if(p != null) {
			p.pbuf.beginPacket(PLAYER_MOVED);
			p.pbuf.writeShort(id);
			p.pbuf.writeShort(area.x);
			p.pbuf.writeShort(area.y);
			p.pbuf.endPacket();
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
