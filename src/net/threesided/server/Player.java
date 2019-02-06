package net.threesided.server;

import net.threesided.server.net.PacketMapper;
import net.threesided.server.physics2d.Circle;
import net.threesided.shared.Constants;
import net.threesided.shared.PacketBuffer;
import net.threesided.shared.Vector2D;

import java.util.ArrayList;

import static net.threesided.shared.Constants.*;

public class Player extends Circle {

    private int id;
    private int lives;
    private int type;
    private String username;
    private boolean connected;
    private boolean readVer;
    private boolean readName;
    private boolean isDead = false;
    private int timeDead = 0;

    private PacketBuffer packetBuffer;

    private boolean logOut;
    private String chatMessage;

    // Length of arrays can be adjusted for more precision
    private static final float[] sines = new float[256];
    private static final float[] cosines = new float[256];

    static {
        double d = (2.0 * Math.PI) / 256;
        for (int k = 0; k < 256; k++) {
            Player.sines[k] = (float) Math.sin(k * d);
            Player.cosines[k] = (float) Math.cos(k * d);
        }
    }

    public Player(PacketBuffer packetBuffer) {
        super(20);
        this.setMass(5);
        this.packetBuffer = packetBuffer;
        this.id = -1;
    }

    /**
     * @return The player's username.
     */
    public String getUsername() {
        return this.username;
    }

    // Sends this player the login information for newly logged in player p
    public void notifyLogin(Player p) {
        packetBuffer.beginPacket(NEW_PLAYER);
        packetBuffer.writeShort(p.id);
        packetBuffer.writeByte(p.type);
        packetBuffer.writeString(p.username);
        packetBuffer.writeByte(p.lives);
        packetBuffer.endPacket();
    }

    // Notifies this player that player p has moved
    public void handleMove(Player p) {
        packetBuffer.beginPacket(Constants.PLAYER_MOVED);
        packetBuffer.writeShort(p.id);

        final Vector2D location = p.getLocation();
        packetBuffer.writeShort((int) location.x);
        packetBuffer.writeShort((int) location.y);
        packetBuffer.endPacket();
    }

    public void writePendingChats(ArrayList<String> chats) {
        for (String s : chats) {
            packetBuffer.beginPacket(Constants.NEW_CHAT_MESSAGE);
            packetBuffer.writeString(s);
            packetBuffer.endPacket();
        }
    }

    // Notify this player of how much time is remaining in the current round
    public void notifyNewRound(int time) {
        packetBuffer.beginPacket(BEGIN_ROUND);
        packetBuffer.writeShort(time);
        packetBuffer.endPacket();
    }

    public void notifyVictoryLap(int time) {
        packetBuffer.beginPacket(BEGIN_VICTORY_LAP);
        packetBuffer.writeShort(time);
        packetBuffer.endPacket();
    }

    public void updateWinners(final byte[] winners) {
        packetBuffer.beginPacket(UPDATE_WINNER);
        packetBuffer.writeByte(winners.length);
        for (final byte b : winners) {
            packetBuffer.writeByte(b);
        }
        packetBuffer.endPacket();
    }

    public boolean processIncomingPackets() {
        if (!packetBuffer.sync()) {
            return false;
        }
        // TODO: Figure out whether this dependency is appropriate or not
        PacketMapper.handlePackets(packetBuffer, this);
        return true;
    }

    public void MOVE_REQUEST(int moveDir) {
        setBit(moveDir);
    }

    public void END_MOVE() {
        clearBit();
    }

    public void LOGOUT() {
        logOut = true;
    }

    public void CHAT_REQUEST(String msg) {
        chatMessage = "<" + username + "> " + msg;
    }

    public void PING() {
        packetBuffer.beginPacket(PING);
        packetBuffer.endPacket();
    }

    /**
     * Tell this player that player p logged out
     *
     * @param p
     */
    public void loggedOut(Player p) {
        packetBuffer.beginPacket(PLAYER_LOGGED_OUT);
        packetBuffer.writeShort(p.id);
        packetBuffer.endPacket();
    }

    /**
     * Notify this player how many lives p has remaining
     *
     * @param p
     */
    public void updateLives(Player p) {
        packetBuffer.beginPacket(PLAYER_DIED);
        packetBuffer.writeShort(p.id);
        packetBuffer.writeByte(p.lives);
        packetBuffer.endPacket();
    }

    private void setBit(int bit) {
        // scale down by 2 so that the values are between
        // -0.5 and +0.5, like the original version
        this.setAcceleration(new Vector2D(sines[bit & 0xff] / 2, cosines[bit & 0xff] / 2));
    }

    private void clearBit() {
        this.setAcceleration(Vector2D.ZERO);
    }

}
