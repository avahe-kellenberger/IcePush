package net.threesided.bot;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.threesided.shared.PacketBuffer;

public class NetworkHandler extends Thread {

    private PacketBuffer buffer;
    private IcePushBot bot;

    // server constants
    public static final String DEFAULT_SERVER = "localhost";
    public static final int DEFAULT_PORT = 2345;
    public static final int VERSION = 105;

    // in opcodes
    private static final int NEW_PLAYER = 5;
    private static final int PLAYER_MOVED = 6;
    private static final int KEEP_ALIVE = 7;
    private static final int PLAYER_LOGGED_OUT = 11;
    private static final int PLAYER_DIED = 12;
    private static final int NEW_CHAT_MESSAGE = 17;
    private static final int TIME_REMAINING = 18;
    // out opcodes
    private static final int MOVE_REQUEST = 8;
    private static final int END_MOVE = 9;
    private static final int LOGOUT = 10;
    private static final int PING = 14;
    private static final int CHAT_REQUEST = 16;

    public NetworkHandler(IcePushBot bot) {
        this.bot = bot;
    }

    public int login(String serverHost, String username) {
        try {
            Socket sock = new Socket(serverHost, DEFAULT_PORT);
            sock.setTcpNoDelay(true);

            OutputStream outStream = sock.getOutputStream();
            InputStream inStream = sock.getInputStream();

            outStream.write(0); // unknown
            outStream.write(VERSION); // version
            outStream.write(username.length()); // username len
            outStream.write(username.getBytes()); // username
            outStream.flush();

            int retu = inStream.read();
            if (retu == 2) {
                System.out.println("username in use");
            } else if (retu == 1) {
                System.out.println("updated");
            } else if (retu == 3) {
                System.out.println("too many players logged in");
            } else if (retu == 4) {
                int id = inStream.read();
                buffer = new PacketBuffer(sock);
                return id;
            } else {
                System.out.println("invalid response");
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return -1;
    }

    public void run() {
        while (true) {
            if(!handlePackets()) {
                bot.stopThread();
                break;
            }
        }
    }

    public boolean handlePackets() {
        if (!buffer.synch()) {
            return false;
        }
        int i;
        while ((i = buffer.openPacket()) != -1) {
            int charId;
            int charX;
            int charY;
            switch (i) {
                case NEW_PLAYER:
                    charId = buffer.readShort();
                    buffer.readByte(); // char type
                    String username = buffer.readString();
                    int deaths = buffer.readShort();
                    bot.onNewPlayer(charId, username, deaths);
                    break;
                case PLAYER_MOVED:
                    charId = buffer.readShort();
                    charX = buffer.readShort();
                    charY = buffer.readShort();
                    bot.onMove(charId, charX, charY);
                    break;
                case PLAYER_DIED:
                    charId = buffer.readShort();
                    deaths = buffer.readByte();
                    bot.onDied(charId, deaths);
                    break;
                case PLAYER_LOGGED_OUT:
                    charId = buffer.readShort();
                    bot.onLogout(charId);
                case KEEP_ALIVE:
                    break;
                case PING:
                    break;
                case NEW_CHAT_MESSAGE:
                    String chatMsg = buffer.readString();
                    bot.onChat(chatMsg);
                    break;
                case TIME_REMAINING:
                    break;
                default:
                    System.out.println("Unhandled opcode: " + i);
            }
            buffer.closePacket();
        }
        return true;
    }

    public void sendChatMessage(String message) {
        try {
            buffer.beginPacket(CHAT_REQUEST);
            buffer.writeString(message);
            buffer.endPacket();
        } catch (Exception localException) {
            localException.printStackTrace();
        }

    }

    public void sendMoveRequest(int moveType) {
        try {
            buffer.beginPacket(MOVE_REQUEST);
            buffer.writeByte(moveType);
            buffer.writeByte(0);
            buffer.endPacket();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void endMoveRequest(int moveType) {
        try {
            buffer.beginPacket(END_MOVE);
            buffer.writeByte(moveType);
            buffer.writeByte(0);
            buffer.endPacket();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void ping() {
        buffer.beginPacket(PING);
        buffer.endPacket();
    }

    public void logout() {
        try {
            if (buffer == null) return;
            buffer.beginPacket(LOGOUT);
            buffer.endPacket();
            buffer.synch();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
}