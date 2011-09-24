package net.threesided.server;

import net.threesided.server.physics2d.*;
import net.threesided.shared.PacketBuffer;
import static net.threesided.shared.Opcodes.*;

import java.util.ArrayList;
import java.net.Socket;
import java.io.IOException;
import java.awt.geom.Path2D;

public class Player extends RigidBody {
	public int id;
	public int deaths;
	public int type;
	public String username;
	public boolean canMove;
	public boolean connected;
	
	private PacketBuffer pbuf;
	private int numSet;

	public boolean logOut;
	String chatMessage;

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

	public Player(Socket s) throws IOException {
		r = 24;			// Radius
		mass = 0.5F;
		pbuf = new PacketBuffer(s);
	}

	public void notifyLogin(Player p) {	// Sends this player the login information for newly logged in player p
		pbuf.beginPacket(NEW_PLAYER);
		pbuf.writeShort(p.id);
		pbuf.writeByte(p.type);
		pbuf.writeString(p.username);
		pbuf.writeShort((int)p.x);
		pbuf.writeShort((int)p.y);
		pbuf.writeShort(p.deaths);
		pbuf.endPacket();
	}

	public void handleMove(Player p) {
		pbuf.beginPacket(PLAYER_MOVED);
		pbuf.writeShort(p.id);
		pbuf.writeShort((int)p.x);
		pbuf.writeShort((int)p.y);
		pbuf.endPacket();
	}

	public void writePendingChats(ArrayList<String> chats) {
		for(String s : chats) {
			pbuf.beginPacket(NEW_CHAT_MESSAGE);
			pbuf.writeString(s);
			pbuf.endPacket();
		}
	}

	public void initPosition(Player[] players, Path2D path) {	// Resets this players position, making sure it intersects none of the players in the array and is within path
		boolean good = false;
		while(true) {
			good = true;

			do {
				x = (float)(Math.random() * 744);
				y = (float)(Math.random() * 422);
			} while(!path.contains(x, y));

			for(Player p : players) {
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
		dx = dy = xa = ya = numSet = 0;
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
		//if(Serv er.DEBUG) System.out.println("GOT MOVE REQUEST - DIR: " + moveDir + " - ID = " + moveId + " , TIME: " + System.currentTimeMillis());
	}

	public void END_MOVE(int moveDir, int moveId) {	
		clearBit(moveDir);
		//if(Serv er.DEBUG) System.out.println("END MOVE REQUEST - ID = " + moveId + " - TIME = " + System.currentTimeMillis());
	}

	public void LOGOUT() {
		logOut = true;
	}

	public void CHAT_REQUEST(String msg) {
		chatMessage = "<" + username + "> " + msg;
	}

	public void PING() {
		pbuf.beginPacket(PING);
		pbuf.endPacket();
	}

	public void loggedOut(Player p) {		// Tell this player that player p logged out
		pbuf.beginPacket(PLAYER_LOGGED_OUT);
		pbuf.writeShort(p.id);
		pbuf.endPacket();
	}

	public void playerDied(Player p) {			// Notify this player that player p has died
		pbuf.beginPacket(PLAYER_DIED);
		pbuf.writeShort(p.id);
		pbuf.writeByte(p.deaths);
		pbuf.writeShort((int)p.x);				// new location
		pbuf.writeShort((int)p.y);
		pbuf.endPacket();
	}

	public void resetDeaths(Player p) {			// Tells this player to reset deaths for player p to 0
		pbuf.beginPacket(PLAYER_DIED);
		pbuf.writeShort(p.id);
		pbuf.writeByte(0);					// Number of times died
		pbuf.writeShort((int)p.x);				// new location
		pbuf.writeShort((int)p.y);
		pbuf.endPacket();
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
