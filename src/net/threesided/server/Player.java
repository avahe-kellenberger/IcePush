package net.threesided.server;

import static net.threesided.shared.Constants.*;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import net.threesided.server.physics2d.Circle;
import net.threesided.server.physics2d.Projectile;
import net.threesided.shared.PacketBuffer;
import net.threesided.shared.Vector2D;

public class Player extends Circle {

    public int lives;
    public int type;
    public String username;
    public boolean connected;
    public boolean readVer;
    public boolean readName;
    public boolean isDead = false;
    public int timeDead = 0;

    PacketBuffer pbuf;
    public Projectile projectile = null;

    public boolean logOut;
    String chatMessage;
    private long lastProjectile;
    private long PR_DELAY = 250;

    // Length of arrays can be adjusted for more precision
    public static final float[] sines = new float[256];
    public static final float[] cosines = new float[256];

    static {
        double d = (2.0 * Math.PI) / 256;
        for (int k = 0; k < 256; k++) {
            sines[k] = (float) Math.sin(k * d);
            cosines[k] = (float) Math.cos(k * d);
        }
    }

    public Player(PacketBuffer pb) {
        super(20);
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

    public void notifyNewObject(Projectile proj) {
        pbuf.beginPacket(NEW_OBJECT);
        pbuf.writeShort(proj.id);
        pbuf.writeByte(2); // objtype 2 = present sprite
        pbuf.writeShort((int)proj.position.getX());
        pbuf.writeShort((int)proj.position.getY());
    }

    // Notifies this player that player p has moved
    public void handleMove(Circle p) {
        pbuf.beginPacket(PLAYER_MOVED);
        pbuf.writeShort(p.id);
        pbuf.writeShort((int) p.position.getX());
        pbuf.writeShort((int) p.position.getY());
        pbuf.endPacket();
    }

    public void writePendingChats(ArrayList<String> chats) {
        for (String s : chats) {
            pbuf.beginPacket(NEW_CHAT_MESSAGE);
            pbuf.writeString(s);
            pbuf.endPacket();
        }
    }

    /**
     * Resets this players position, making sure it intersects none of the players in the array and
     * is within path.
     *
     * @param players
     * @param path
     */
    public void initPosition(Player[] players, Path2D path) {
        boolean good;
        do {
            good = true;

            do {
                position.set(Math.random() * 744, Math.random() * 422);
            } while (!path.contains(position.getX(), position.getY()));

            for (Player p : players) {
                if (p == null || p == this) {
                    continue;
                }

                double dx = position.getX() - p.position.getX();
                double dy = position.getY() - p.position.getY();

                double sum = this.radius + p.radius;

                double dist = dx * dx + dy * dy;

                if (dist < sum * sum) {
                    good = false;
                }
            }

        } while (!good);

        velocity.set(0, 0);
        acceleration.set(0, 0);

        for (Player p : players) {
            if (p == null) {
                continue;
            }
            p.handleMove(this);
        }
    }

    // Notify this player of how much time is remaining in the current round
    public void notifyNewRound(int time) {
        pbuf.beginPacket(BEGIN_ROUND);
        pbuf.writeShort(time);
        pbuf.endPacket();
    }

    public void notifyVictoryLap(int time) {
        pbuf.beginPacket(BEGIN_VICTORY_LAP);
        pbuf.writeShort(time);
        pbuf.endPacket();
    }

    public void updateWinners(final byte[] winners) {
        pbuf.beginPacket(UPDATE_WINNER);
        pbuf.writeByte(winners.length);
        for (final byte b : winners) {
            pbuf.writeByte(b);
        }
        pbuf.endPacket();
    }

    public boolean processIncomingPackets() {
        if (!pbuf.synch()) return false;
        PacketMapper.handlePackets(
                pbuf, this); // TODO: Figure out whether this dependency is appropriate or not
        return true;
    }

    public void MOVE_REQUEST(int moveDir) {
        setBit(moveDir);
        // if(Serv er.DEBUG) System.out.println("GOT MOVE REQUEST - DIR: " + moveDir + " - ID = " +
        // moveId + " , TIME: " + System.currentTimeMillis());
    }

    public void END_MOVE() {
        clearBit();
        // if(Serv er.DEBUG) System.out.println("END MOVE REQUEST - ID = " + moveId + " - TIME = " +
        // System.currentTimeMillis());
    }

    public void PROJECTILE_REQUEST(int x, int y) {
        //System.out.println("Received projectile request from " + username + ": x=" + x + ", y=" + y);
        long now = System.currentTimeMillis();
        if(now - lastProjectile < PR_DELAY) {
            return;
        } else {
            lastProjectile = now;
        }

        double dx = x - (radius + this.position.getX());
        double dy = y - (radius + this.position.getY());
        double R = dx*dx + dy*dy;
        if(R == 0) return;
        R = Math.sqrt(R);
        Vector2D velocity = new Vector2D();
        velocity.set(6*dx/R, 6*dy/R);
        Projectile bomb = new Projectile(12, 25, velocity);
        bomb.position = new Vector2D();
        bomb.position.set(x, y);

        int index = Server.objectTable.length - 1;
        while(index >= 0) {
            Circle c = Server.objectTable[index];
            if(c == null) {
                break;
            } else if(c instanceof Player) {
                return;
            }
            index--;
        }
        bomb.id = index;
        Server.objectTable[index] = bomb;
        //System.out.println("projectile index = " + index);
	this.projectile = bomb;
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

    public void loggedOut(Circle p) { // Tell this player that player p logged out
        pbuf.beginPacket(PLAYER_LOGGED_OUT);
        pbuf.writeShort(p.id);
        pbuf.endPacket();
    }

    public void updateLives(Player p) { // Notify this player how many lives p has remaining
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
