package net.threesided.server;

import net.threesided.server.physics2d.*;
import net.threesided.shared.PacketBuffer;
import static net.threesided.shared.Constants.*;

import java.util.ArrayList;
import java.net.Socket;
import java.io.IOException;
import java.awt.geom.Path2D;

public class Player extends RigidBody {
	public int id;
	public int lives;
	public int type;
	public String username;
	public boolean connected;
	public int nameLen;
	public boolean readVer;
	public boolean readName;
	//public boolean loginComplete;
	public boolean isDead = false;
	public int timeDead = 0;
	
	PacketBuffer pbuf;

	public boolean logOut;
	String chatMessage;

	// Length of arrays can be adjusted for more precision
	public static final float[] sines = new float[256];
	public static final float[] cosines = new float[256];
	static {
		double d = (2.0 * Math.PI) / 256;
		for(int k = 0; k < 256; k++) {
			sines[k] = (float) Math.sin(k * d);
			cosines[k] = (float) Math.cos(k * d);
		}
	}

	public Player(PacketBuffer pb) throws IOException {
		r = 20;			// Radius
		mass = 5;
		pbuf = pb;
		id = -1;
	}

	// Sends this player the login information for newly logged in player p
	public void notifyLogin(Player p) {
		pbuf.beginPacket(NEW_PLAYER);
		pbuf.writeShort(p.id);
		pbuf.writeByte(p.type);
		pbuf.writeString(p.username);
		pbuf.writeByte(p.lives);
		pbuf.endPacket();
	}

	// Notifies this player that player p has moved
	public void handleMove(Player p) {
		pbuf.beginPacket(PLAYER_MOVED);
		pbuf.writeShort(p.id);
		pbuf.writeShort((int)p.position.getX());
		pbuf.writeShort((int)p.position.getY());
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
		boolean good;
		while(true) {
			good = true;

			do {
				position.set(Math.random() * 744, Math.random() * 422);
			} while(!path.contains(position.getX(), position.getY()));

			for(Player p : players) {
				if(p == null || p == this) {
					continue;
				}

				double dx = position.getX() - p.position.getX();
				double dy = position.getY() - p.position.getY();

				double sum = r + p.r;

				double dist = dx*dx + dy*dy;

				if(dist < sum*sum) good = false;
				//if (good) p.handleMove(this);
			}

			if(good)
				break;
		}
		
		velocity.set(0, 0);
		acceleration.set(0, 0);
		
		for(Player p : players) {
			if(p == null) {
				continue;
			}
			p.handleMove(this);
		}
	}

	// Notify this player of how much time is remaining in the current round
	public void updateRoundTime(int time) {
		pbuf.beginPacket(UPDATE_TIME);
		pbuf.writeShort(time);
		pbuf.endPacket();
	}

    public void updateDeathTime(int time) {
        pbuf.beginPacket(UPDATE_TIME);
        pbuf.writeShort(time);
        pbuf.endPacket();
    }

    public boolean processIncomingPackets() {
		if (!pbuf.synch()) return false;
		PacketMapper.handlePackets(pbuf, this);		// TODO: Figure out whether this dependency is appropriate or not
		return true;
	}

	public void MOVE_REQUEST(int moveDir) {
		setBit(moveDir);
		//if(Serv er.DEBUG) System.out.println("GOT MOVE REQUEST - DIR: " + moveDir + " - ID = " + moveId + " , TIME: " + System.currentTimeMillis());
	}

	public void END_MOVE() {
		clearBit();
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

	public void updateLives(Player p) {			// Notify this player how many lives p has remaining
		pbuf.beginPacket(PLAYER_DIED);
		pbuf.writeShort(p.id);
		pbuf.writeByte(p.lives);
		pbuf.endPacket();
	}

	// Tells this player to reset deaths for player p to 0
	/*public void resetDeaths(Player p) {
		pbuf.beginPacket(PLAYER_DIED);
		pbuf.writeShort(p.id);
		pbuf.writeByte(0);					// Number of times died
		pbuf.endPacket();
	} */

	private void setBit(int bit) {
		// scale down by 2 so that the values are between 
		// -0.5 and +0.5, like the original version
	    acceleration.set(sines[bit & 0xff] / 2, cosines[bit & 0xff] / 2);
	}

	private void clearBit() {
        acceleration.set(0, 0);
	}
}
