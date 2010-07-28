package com.glgames.server;

import com.glgames.server.physics2d.*;
import com.glgames.shared.PacketBuffer;
import static com.glgames.shared.Opcodes.*;

import java.util.ArrayList;

public class Player extends RigidBody {
	public int id;
	public int deaths;
	public int type;
	public String username;
	public boolean canMove;
	public boolean connected;
	private int keepAliveCounter;
	
	public PacketBuffer pbuf;

	public Player() {
		r = 24;
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
			pbuf.writeShort((int)plr.x);
			pbuf.writeShort((int)plr.y);
			pbuf.writeShort(plr.deaths);
			pbuf.endPacket();

			// Tell that client about this player
			plr.pbuf.beginPacket(NEW_PLAYER); // new player has entered
			plr.pbuf.writeShort(id);
			plr.pbuf.writeByte(type);
			plr.pbuf.writeString(username);
			plr.pbuf.writeShort((int)x); // x
			plr.pbuf.writeShort((int)y); // y
			plr.pbuf.writeShort(deaths);
			plr.pbuf.endPacket();
		}
	}

	public void keepAlive() {
		keepAliveCounter = (keepAliveCounter + 1) % 50;
		if(keepAliveCounter == 0) return;
		pbuf.beginPacket(KEEP_ALIVE);
		pbuf.endPacket();
	}

	public void handleMove() {
		if(x < 4 || y < 5 || x > 752 || y > 428) {
			System.out.println("PLAYER " + username + " IS OUT OF RANGE!");
			playerDied();
			return;
		}
		
		if(hasMoved()) {
			for(Player p : Server.players) if(p != null) {
				p.pbuf.beginPacket(PLAYER_MOVED);
				p.pbuf.writeShort(id);
				p.pbuf.writeShort((int)x);
				p.pbuf.writeShort((int)y);
				p.pbuf.endPacket();
			}
		}
	}

	public void writePendingChats(ArrayList<String> chats) {
		for(String s : chats) {
			pbuf.beginPacket(NEW_CHAT_MESSAGE);
			pbuf.writeString(s);
			pbuf.endPacket();
		}
	}

	void initPosition() {
		boolean good = false;
		while(true) {
			good = true;
			x = (float)(Math.random() * 744);
			y = (float)(Math.random() * 422);
			for(Player p : Server.players) {
				if(p == null || p == this) {
					continue;
				}

				float dx = x - p.x;
				float dy = y - p.y;

				float sum = r + p.r;

				float dist = dx*dx + dy*dy;

				if(dist < sum*sum) good = false;
			}
			if(good)
				break;
		}
		dx = dy = xa = ya = 0;
	}

	public void processIncomingPackets() {
		try {
			if (!pbuf.synch()) {
				Server.logoutPlayer(this); // Log out player if connection has been lost
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
						Server.logoutPlayer(this);
						break;
					case KEEP_ALIVE:
						break;
					case CHAT_REQUEST:
						String msg = pbuf.readString();
						String full = "<" + username + "> " + msg;
						InternetRelayChat.sendMessage(full);
						InternetRelayChat.msgs.push(full);
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
				plr.pbuf.writeShort((int)x); // new location
				plr.pbuf.writeShort((int)y);
				plr.pbuf.endPacket();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setBit(int bit) {
		if(bit == UP) ya = -0.5F;
		if(bit == DOWN) ya = 0.5F;
		if(bit == LEFT) xa = -0.5F;
		if(bit == RIGHT) xa = 0.5F;
	}

	private void clearBit(int bit) {
		if(bit == UP) ya = 0;
		if(bit == DOWN) ya = 0;
		if(bit == LEFT) xa = 0;
		if(bit == RIGHT) xa = 0;
	}
}