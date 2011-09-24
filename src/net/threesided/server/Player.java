package net.threesided.server;

import net.threesided.server.physics2d.*;
import net.threesided.shared.PacketBuffer;
import static net.threesided.shared.Opcodes.*;

import java.util.ArrayList;

public class Player extends RigidBody {
	public int id;
	public int deaths;
	public int type;
	public String username;
	public boolean canMove;
	public boolean connected;
	
	public PacketBuffer pbuf;
	private int numSet;

	// Length of arrays can be adjusted for more precision
	public static final float[] sines = new float[256];
	public static final float[] cosines = new float[256];
	static {
		double d = (2.0d * Math.PI) / 256.0d;
		for(int k = 0; k < 256; k++) {
			sines[k] = (float) Math.sin(k * d);
			cosines[k] = (float) Math.cos(k * d);
		}
	}

	public Player() {
		r = 24;			// Radius
		mass = 0.5F;
	}

	public void notifyLogin(Player players[]) {	// Sends this player the login information of the players given in the array
		for (Player plr : players) {
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

			/*
			// Tell that client about this player
			plr.pbuf.beginPacket(NEW_PLAYER); // new player has entered
			plr.pbuf.writeShort(id);
			plr.pbuf.writeByte(type);
			plr.pbuf.writeString(username);
			plr.pbuf.writeShort((int)x); // x
			plr.pbuf.writeShort((int)y); // y
			plr.pbuf.writeShort(deaths);
			plr.pbuf.endPacket();	*/
		}
	}

	public void handleMove() {
		//		if(x < 4 || y < 5 || x > 752 || y > 428) {
		if(!Server.mapClass.currentPath.contains(x, y)) {
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

	public void updateRoundTime(int time) {
		pbuf.beginPacket(UPDATE_ROUNDTIME);
		pbuf.writeShort(time);
		pbuf.endPacket();
	}		

	public boolean processIncomingPackets() {
		if (!pbuf.synch()) return false;
		PacketMapper.handlePackets(pbuf, this);		// TODO: Figure out whether this dependency is appropriate or not
		return true;
	}

	public void MOVE_REQUEST(int moveDir, int moveId) {
		setBit(moveDir);
		if(Server.DEBUG) System.out.println("GOT MOVE REQUEST - DIR: " + moveDir + " - ID = " + moveId + " , TIME: " + System.currentTimeMillis());
	}

	public void END_MOVE(int moveDir, int moveId) {	
		clearBit(moveDir);
		if(Server.DEBUG) System.out.println("END MOVE REQUEST - ID = " + moveId + " - TIME = " + System.currentTimeMillis());
	}

	public void LOGOUT() {
		Server.logoutPlayer(this);
	}

	public void CHAT_REQUEST(String msg) {
		String full = "<" + username + "> " + msg;
		InternetRelayChat.sendMessage(full);
		InternetRelayChat.msgs.push(full);
	}

	public void PING() {
		pbuf.beginPacket(PING);
		pbuf.endPacket();
	}

	private void playerDied() {
		numSet = 0;
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

	public void resetDeaths() {
		for (Player plr : Server.players) {
			if (plr == null)
				continue;
			pbuf.beginPacket(PLAYER_DIED);
			pbuf.writeShort(plr.id);
			pbuf.writeByte(0);		// Number of times died
			pbuf.writeShort((int)plr.x); // new location
			pbuf.writeShort((int)plr.y);
			pbuf.endPacket();
		}
	}

	private void setBit(int bit) {
		numSet++;
		// scale down by 2 so that the values are between 
		// -0.5 and +0.5, like the original version
		xa += sines[bit & 0xff] / 2f;
		ya += cosines[bit & 0xff] / 2f;
	}

	private void clearBit(int bit) {
		if(numSet == 0) return;
		numSet--;
		xa -= sines[bit & 0xff] / 2f;
		ya -= cosines[bit & 0xff] / 2f;
		if(numSet == 0) xa = ya = 0;
	}
}
